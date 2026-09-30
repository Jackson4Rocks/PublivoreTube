package tv.publivoretube.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class YouTubeAccountApi(
    private val accessToken: String,
) {
    fun subscriptions(maxResults: Int = 25): List<Subscription> {
        val url =
            "https://www.googleapis.com/youtube/v3/subscriptions" +
                "?part=snippet" +
                "&mine=true" +
                "&maxResults=" + maxResults

        val json = get(url)
        val items = json.optJSONArray("items") ?: return emptyList()

        return buildList {
            for (index in 0 until items.length()) {
                val snippet = items.optJSONObject(index)?.optJSONObject("snippet") ?: continue
                val channelId = snippet.optString("resourceId")
                    .let { if (it.isEmpty()) "" else it }

                val resource = snippet.optJSONObject("resourceId")
                val id = resource?.optString("channelId").orEmpty()
                if (id.isBlank()) continue

                val thumbnails = snippet.optJSONObject("thumbnails")
                add(
                    Subscription(
                        channelId = id,
                        channelTitle = snippet.optString("title"),
                        thumbnail =
                            thumbnails?.optJSONObject("medium")?.optString("url")
                                ?.takeIf(String::isNotBlank)
                                ?: thumbnails?.optJSONObject("default")?.optString("url")
                                    ?.takeIf(String::isNotBlank),
                    ),
                )
            }
        }
    }

    private fun get(urlString: String): JSONObject {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Authorization", "Bearer " + accessToken)

        return try {
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (connection.responseCode !in 200..299) {
                throw IOException("YouTube account request failed: " + body)
            }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }
}
