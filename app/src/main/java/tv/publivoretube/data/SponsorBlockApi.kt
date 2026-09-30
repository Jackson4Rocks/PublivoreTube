package tv.publivoretube.data

import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

data class SponsorSegment(
    val category: String,
    val startSeconds: Double,
    val endSeconds: Double,
    val actionType: String,
)

class SponsorBlockApi {
    companion object {
        private const val BASE_URL = "https://sponsor.ajay.app"
        private val DEFAULT_CATEGORIES = listOf(
            "sponsor",
            "selfpromo",
            "interaction",
            "intro",
            "outro",
            "preview",
            "music_offtopic",
            "filler",
        )
        private val DEFAULT_ACTION_TYPES = listOf("skip", "poi", "chapter")
    }

    fun fetchSegments(
        videoId: String,
        categories: List<String> = DEFAULT_CATEGORIES,
    ): List<SponsorSegment> {
        require(videoId.matches(Regex("[A-Za-z0-9_-]{6,32}"))) {
            "Invalid YouTube video ID"
        }

        val hash = sha256(videoId)
        val prefix = hash.take(4)
        val categoryJson = JSONArray(categories).toString()
        val actionTypeJson = JSONArray(DEFAULT_ACTION_TYPES).toString()

        val query =
            "service=YouTube" +
                "&categories=" + URLEncoder.encode(categoryJson, Charsets.UTF_8.name()) +
                "&actionTypes=" + URLEncoder.encode(actionTypeJson, Charsets.UTF_8.name())

        val connection = (URL("$BASE_URL/api/skipSegments/$prefix?$query")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 7_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "PublivoreTube/0.1")
        }

        try {
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return emptyList()
            }

            if (connection.responseCode !in 200..299) {
                throw IllegalStateException(
                    "SponsorBlock HTTP " + connection.responseCode,
                )
            }

            val body = BufferedReader(
                InputStreamReader(connection.inputStream, Charsets.UTF_8),
            ).use { reader -> reader.readText() }

            val matches = JSONArray(body)
            for (index in 0 until matches.length()) {
                val video = matches.optJSONObject(index) ?: continue
                if (video.optString("videoID") != videoId) continue

                val segments = video.optJSONArray("segments") ?: return emptyList()
                return buildList {
                    for (segmentIndex in 0 until segments.length()) {
                        val segment = segments.optJSONObject(segmentIndex) ?: continue
                        val times = segment.optJSONArray("segment") ?: continue
                        if (times.length() < 2) continue

                        val start = times.optDouble(0, Double.NaN)
                        val end = times.optDouble(1, Double.NaN)
                        if (!start.isFinite() || !end.isFinite() || end <= start) continue

                        add(
                            SponsorSegment(
                                category = segment.optString("category", "unknown"),
                                startSeconds = start.coerceAtLeast(0.0),
                                endSeconds = end.coerceAtLeast(0.0),
                                actionType = segment.optString("actionType", "skip"),
                            ),
                        )
                    }
                }.sortedBy { it.startSeconds }
            }

            return emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
