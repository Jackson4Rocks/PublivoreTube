package tv.publivoretube.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

data class SponsorSegment(
    val startSeconds: Double,
    val endSeconds: Double,
    val category: String,
)

class SponsorBlockRepository {
    suspend fun getSegments(videoId: String): List<SponsorSegment> = withContext(Dispatchers.IO) {
        val prefix = sha256(videoId).take(4)
        val categories = URLEncoder.encode(
            """["sponsor","selfpromo","interaction","intro","outro","preview","music_offtopic"]""",
            Charsets.UTF_8.name(),
        )
        val url = URL(
            "https://sponsor.ajay.app/api/skipSegments/$prefix?categories=$categories",
        )

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 10_000
            useCaches = false
            setRequestProperty("User-Agent", "PublivoreTube/0.1 Android TV")
            setRequestProperty("Accept", "application/json")
        }

        try {
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext emptyList()
            }

            if (connection.responseCode !in 200..299) {
                return@withContext emptyList()
            }

            val body = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            val candidates = JSONArray(body)
            buildList {
                for (index in 0 until candidates.length()) {
                    val candidate = candidates.getJSONObject(index)
                    if (candidate.optString("videoID") != videoId) continue

                    val segments = candidate.optJSONArray("segments") ?: continue
                    for (segmentIndex in 0 until segments.length()) {
                        val item = segments.getJSONObject(segmentIndex)
                        val range = item.optJSONArray("segment") ?: continue
                        if (range.length() < 2) continue

                        val start = range.optDouble(0, -1.0)
                        val end = range.optDouble(1, -1.0)
                        if (start >= 0.0 && end > start) {
                            add(
                                SponsorSegment(
                                    startSeconds = start,
                                    endSeconds = end,
                                    category = item.optString("category", "sponsor"),
                                ),
                            )
                        }
                    }
                }
            }.sortedBy { it.startSeconds }
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
