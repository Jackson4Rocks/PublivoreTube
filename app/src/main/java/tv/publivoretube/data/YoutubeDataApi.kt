package tv.publivoretube.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class YoutubeDataApi(
    private val apiKey: String,
) {
    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    suspend fun mostPopular(regionCode: String = "IN", maxResults: Int = 12): List<Video> =
        withContext(Dispatchers.IO) {
            requireConfigured()

            val url = baseUrl + "/videos?part=snippet,contentDetails,statistics" +
                "&chart=mostPopular" +
                "&regionCode=" + encode(regionCode) +
                "&maxResults=" + maxResults +
                "&key=" + encode(apiKey)

            val json = get(url)
            val items = json.getJSONArray("items")

            buildList {
                for (index in 0 until items.length()) {
                    val item = items.getJSONObject(index)
                    val snippet = item.getJSONObject("snippet")
                    val id = item.getString("id")

                    add(
                        Video(
                            id = id,
                            title = cleanText(snippet.optString("title")),
                            channel = cleanText(snippet.optString("channelTitle")),
                            duration = item.optJSONObject("contentDetails")
                                ?.optString("duration")
                                ?.let(::formatDuration)
                                .orEmpty(),
                            thumbnail = thumbnailUrl(snippet),
                            youtubeUrl = "https://www.youtube.com/watch?v=$id",
                        ),
                    )
                }
            }
        }

    suspend fun search(query: String, maxResults: Int = 18): List<Video> =
        withContext(Dispatchers.IO) {
            requireConfigured()

            if (query.isBlank()) {
                return@withContext emptyList()
            }

            val searchUrl = baseUrl + "/search?part=snippet" +
                "&type=video" +
                "&q=" + encode(query.trim()) +
                "&maxResults=" + maxResults +
                "&safeSearch=moderate" +
                "&regionCode=IN" +
                "&key=" + encode(apiKey)

            val searchJson = get(searchUrl)
            val searchItems = searchJson.getJSONArray("items")

            if (searchItems.length() == 0) {
                return@withContext emptyList()
            }

            val ids = buildList {
                for (index in 0 until searchItems.length()) {
                    val id = searchItems.getJSONObject(index)
                        .getJSONObject("id")
                        .optString("videoId")

                    if (id.isNotBlank()) {
                        add(id)
                    }
                }
            }

            if (ids.isEmpty()) {
                return@withContext emptyList()
            }

            val detailsUrl = baseUrl + "/videos?part=contentDetails,statistics" +
                "&id=" + encode(ids.joinToString(",")) +
                "&key=" + encode(apiKey)

            val detailsJson = get(detailsUrl)
            val detailsById = mutableMapOf<String, JSONObject>()
            val detailItems = detailsJson.getJSONArray("items")

            for (index in 0 until detailItems.length()) {
                val item = detailItems.getJSONObject(index)
                detailsById[item.getString("id")] = item
            }

            buildList {
                for (index in 0 until searchItems.length()) {
                    val item = searchItems.getJSONObject(index)
                    val id = item.getJSONObject("id").optString("videoId")

                    if (id.isBlank()) {
                        continue
                    }

                    val snippet = item.getJSONObject("snippet")
                    val details = detailsById[id]

                    add(
                        Video(
                            id = id,
                            title = cleanText(snippet.optString("title")),
                            channel = cleanText(snippet.optString("channelTitle")),
                            duration = details?.optJSONObject("contentDetails")
                                ?.optString("duration")
                                ?.let(::formatDuration)
                                .orEmpty(),
                            thumbnail = thumbnailUrl(snippet),
                            youtubeUrl = "https://www.youtube.com/watch?v=$id",
                        ),
                    )
                }
            }
        }

    private fun get(urlString: String): JSONObject {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            useCaches = false
        }

        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (responseCode !in 200..299) {
                throw IOException("YouTube API HTTP $responseCode: $body")
            }

            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun requireConfigured() {
        check(isConfigured) {
            "YouTube Data API key is not configured."
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun thumbnailUrl(snippet: JSONObject): String? {
        val thumbnails = snippet.optJSONObject("thumbnails") ?: return null

        return thumbnails.optJSONObject("high")?.optString("url")?.takeIf(String::isNotBlank)
            ?: thumbnails.optJSONObject("medium")?.optString("url")?.takeIf(String::isNotBlank)
            ?: thumbnails.optJSONObject("default")?.optString("url")?.takeIf(String::isNotBlank)
    }

    private fun cleanText(value: String): String =
        value.replace("&#39;", "'")
            .replace("&quot;", """)
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")

    private fun formatDuration(iso: String): String {
        val match = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            .matchEntire(iso)
            ?: return ""

        val hours = match.groupValues[1].toLongOrNull() ?: 0
        val minutes = match.groupValues[2].toLongOrNull() ?: 0
        val seconds = match.groupValues[3].toLongOrNull() ?: 0

        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }

    private companion object {
        const val baseUrl = "https://www.googleapis.com/youtube/v3"
    }
}
