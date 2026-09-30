package tv.publivoretube.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GoogleDeviceAuth(
    val userCode: String,
    val verificationUrl: String,
    val expiresInSeconds: Long,
    val intervalSeconds: Long,
)

data class GoogleAccount(
    val email: String?,
    val name: String?,
)

class GoogleAuthManager(
    context: Context,
    private val clientId: String,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val isConfigured: Boolean
        get() = clientId.isNotBlank()

    val isSignedIn: Boolean
        get() = !prefs.getString(KEY_REFRESH_TOKEN, null).isNullOrBlank()

    val account: GoogleAccount?
        get() {
            val email = prefs.getString(KEY_EMAIL, null)
            val name = prefs.getString(KEY_NAME, null)
            return if (email.isNullOrBlank() && name.isNullOrBlank()) null else GoogleAccount(email, name)
        }

    suspend fun requestDeviceAuth(): GoogleDeviceAuth = withContext(Dispatchers.IO) {
        check(isConfigured) {
            "Google TV OAuth is not configured. Add GOOGLE_OAUTH_CLIENT_ID to local.properties."
        }

        val json = formPost(
            "https://oauth2.googleapis.com/device/code",
            mapOf(
                "client_id" to clientId,
                "scope" to SCOPE,
            ),
        )

        val result = GoogleDeviceAuth(
            userCode = json.getString("user_code"),
            verificationUrl = json.optString(
                "verification_url",
                json.optString("verification_url_complete", "https://www.google.com/device"),
            ),
            expiresInSeconds = json.optLong("expires_in", 1800L),
            intervalSeconds = json.optLong("interval", 5L),
        )

        prefs.edit()
            .putString(KEY_DEVICE_CODE, json.getString("device_code"))
            .putLong(KEY_DEVICE_EXPIRES_AT, System.currentTimeMillis() + result.expiresInSeconds * 1000L)
            .apply()

        result
    }

    suspend fun completeDeviceAuth(intervalSeconds: Long): GoogleAccount = withContext(Dispatchers.IO) {
        val deviceCode = prefs.getString(KEY_DEVICE_CODE, null)
            ?: error("No Google sign-in is currently pending.")
        var interval = intervalSeconds.coerceAtLeast(2L)

        while (System.currentTimeMillis() < prefs.getLong(KEY_DEVICE_EXPIRES_AT, 0L)) {
            delay(interval * 1000L)

            try {
                val json = formPost(
                    "https://oauth2.googleapis.com/token",
                    mapOf(
                        "client_id" to clientId,
                        "device_code" to deviceCode,
                        "grant_type" to "urn:ietf:params:oauth:grant-type:device_code",
                    ),
                )

                val refreshToken = json.optString("refresh_token")
                val accessToken = json.optString("access_token")
                check(refreshToken.isNotBlank() && accessToken.isNotBlank()) {
                    "Google returned an incomplete authorization response."
                }

                prefs.edit()
                    .putString(KEY_REFRESH_TOKEN, refreshToken)
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putLong(KEY_ACCESS_EXPIRES_AT, System.currentTimeMillis() + json.optLong("expires_in", 3600L) * 1000L)
                    .remove(KEY_DEVICE_CODE)
                    .remove(KEY_DEVICE_EXPIRES_AT)
                    .apply()

                val profile = fetchAccount(accessToken)
                prefs.edit()
                    .putString(KEY_EMAIL, profile.email)
                    .putString(KEY_NAME, profile.name)
                    .apply()

                return@withContext profile
            } catch (error: IOException) {
                val message = error.message.orEmpty()
                when {
                    message.contains("authorization_pending") -> Unit
                    message.contains("slow_down") -> interval += 5L
                    message.contains("access_denied") -> error("Google sign-in was denied.")
                    else -> error(message.ifBlank { "Google sign-in failed." })
                }
            }
        }

        error("Google sign-in timed out. Start again from Settings.")
    }

    suspend fun accessToken(): String? = withContext(Dispatchers.IO) {
        val existing = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_ACCESS_EXPIRES_AT, 0L)
        if (!existing.isNullOrBlank() && System.currentTimeMillis() < expiresAt - 60_000L) {
            return@withContext existing
        }

        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return@withContext null
        val json = formPost(
            "https://oauth2.googleapis.com/token",
            mapOf(
                "client_id" to clientId,
                "refresh_token" to refreshToken,
                "grant_type" to "refresh_token",
            ),
        )
        val token = json.optString("access_token").takeIf(String::isNotBlank) ?: return@withContext null

        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putLong(KEY_ACCESS_EXPIRES_AT, System.currentTimeMillis() + json.optLong("expires_in", 3600L) * 1000L)
            .apply()

        token
    }

    fun signOut() {
        prefs.edit().clear().apply()
    }

    private fun fetchAccount(accessToken: String): GoogleAccount {
        val connection = URL("https://openidconnect.googleapis.com/v1/userinfo").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Authorization", "Bearer $accessToken")

        return try {
            if (connection.responseCode !in 200..299) {
                throw IOException("Google profile request failed with HTTP " + connection.responseCode + ".")
            }
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            GoogleAccount(
                email = json.optString("email").takeIf(String::isNotBlank),
                name = json.optString("name").takeIf(String::isNotBlank),
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun formPost(urlString: String, values: Map<String, String>): JSONObject {
        val body = values.entries.joinToString("&") {
            URLEncoder.encode(it.key, Charsets.UTF_8.name()) + "=" + URLEncoder.encode(it.value, Charsets.UTF_8.name())
        }
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val errorCode = runCatching { JSONObject(text).optString("error") }.getOrDefault("")
                throw IOException(errorCode.ifBlank { "Google OAuth HTTP " + code + ": " + text })
            }
            JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val PREFS = "google_auth"
        const val KEY_DEVICE_CODE = "device_code"
        const val KEY_DEVICE_EXPIRES_AT = "device_expires_at"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_ACCESS_EXPIRES_AT = "access_expires_at"
        const val KEY_EMAIL = "email"
        const val KEY_NAME = "name"
        const val SCOPE = "openid profile email https://www.googleapis.com/auth/youtube"
    }
}