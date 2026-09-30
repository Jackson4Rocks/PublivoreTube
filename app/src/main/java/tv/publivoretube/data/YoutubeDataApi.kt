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

    suspend fun mostPopular(
        regionCode: String = "IN",
        maxResults: Int = 50,
    ): List<Video> = withContext(Dispatchers.IO) {
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
                        channelId = snippet.optString("channelId").takeIf(String::isNotBlank),
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

    
    suspend fun shorts(
        maxResults: Int = 50,
    ): List<Video> = withContext(Dispatchers.IO) {
        requireConfigured()

        val searchUrl = baseUrl + "/search?part=snippet" +
            "&type=video" +
            "&q=" + encode("#shorts") +
            "&maxResults=" + maxResults +
            "&safeSearch=moderate" +
            "&regionCode=IN" +
            "&order=date" +
            "&key=" + encode(apiKey)

        val searchJson = get(searchUrl)
        val searchItems = searchJson.getJSONArray("items")
        val ids = buildList {
            for (index in 0 until searchItems.length()) {
                val id = searchItems.getJSONObject(index)
                    .getJSONObject("id")
                    .optString("videoId")
                if (id.isNotBlank()) add(id)
            }
        }
        if (ids.isEmpty()) return@withContext emptyList()

        val detailsJson = get(
            baseUrl + "/videos?part=contentDetails,statistics" +
                "&id=" + encode(ids.joinToString(",")) +
                "&key=" + encode(apiKey),
        )
        val detailsById = mutableMapOf<String, JSONObject>()
        val details = detailsJson.getJSONArray("items")
        for (index in 0 until details.length()) {
            val item = details.getJSONObject(index)
            detailsById[item.getString("id")] = item
        }

        buildList {
            for (index in 0 until searchItems.length()) {
                val item = searchItems.getJSONObject(index)
                val id = item.getJSONObject("id").optString("videoId")
                val snippet = item.getJSONObject("snippet")
                val duration = detailsById[id]
                    ?.optJSONObject("contentDetails")
                    ?.optString("duration")
                    ?.let(::formatDuration)
                    .orEmpty()
                val totalSeconds = parseDurationSeconds(duration)

                if (id.isNotBlank() && totalSeconds in 1..180) {
                    add(
                        Video(
                            id = id,
                            title = cleanText(snippet.optString("title")),
                            channel = cleanText(snippet.optString("channelTitle")),
                            channelId = snippet.optString("channelId").takeIf(String::isNotBlank),
                            duration = duration,
                            thumbnail = thumbnailUrl(snippet),
                            youtubeUrl = "https://www.youtube.com/watch?v=$id",
                        ),
                    )
                }
            }
        }
    }

    suspend fun channelInfo(channelId: String): ChannelInfo = withContext(Dispatchers.IO) {
        requireConfigured()
        require(channelId.isNotBlank()) { "Channel ID is required." }

        val json = get(
            baseUrl + "/channels?part=snippet,statistics&id=" +
                encode(channelId) + "&key=" + encode(apiKey),
        )
        val item = json.getJSONArray("items").optJSONObject(0)
            ?: error("Channel not found.")

        val snippet = item.getJSONObject("snippet")
        val statistics = item.optJSONObject("statistics")

        ChannelInfo(
            id = channelId,
            title = cleanText(snippet.optString("title")),
            description = cleanText(snippet.optString("description")),
            thumbnail = thumbnailUrl(snippet),
            subscriberCount = statistics?.optString("subscriberCount").orEmpty(),
            videoCount = statistics?.optString("videoCount").orEmpty(),
        )
    }

    suspend fun channelVideos(
        channelId: String,
        maxResults: Int = 50,
        order: String = "date",
    ): List<Video> = withContext(Dispatchers.IO) {
        requireConfigured()
        require(channelId.isNotBlank()) { "Channel ID is required." }

        val searchUrl = baseUrl + "/search?part=snippet" +
            "&type=video" +
            "&channelId=" + encode(channelId) +
            "&order=" + encode(order) +
            "&maxResults=" + maxResults +
            "&safeSearch=moderate" +
            "&key=" + encode(apiKey)

        val searchJson = get(searchUrl)
        val items = searchJson.getJSONArray("items")
        val ids = buildList {
            for (index in 0 until items.length()) {
                val id = items.getJSONObject(index)
                    .getJSONObject("id")
                    .optString("videoId")
                if (id.isNotBlank()) add(id)
            }
        }
        if (ids.isEmpty()) return@withContext emptyList()

        val detailsJson = get(
            baseUrl + "/videos?part=contentDetails" +
                "&id=" + encode(ids.joinToString(",")) +
                "&key=" + encode(apiKey),
        )
        val detailsById = mutableMapOf<String, String>()
        val detailItems = detailsJson.getJSONArray("items")
        for (index in 0 until detailItems.length()) {
            val item = detailItems.getJSONObject(index)
            detailsById[item.getString("id")] = item
                .optJSONObject("contentDetails")
                ?.optString("duration")
                ?.let(::formatDuration)
                .orEmpty()
        }

        buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val id = item.getJSONObject("id").optString("videoId")
                val snippet = item.getJSONObject("snippet")
                if (id.isBlank()) continue

                add(
                    Video(
                        id = id,
                        title = cleanText(snippet.optString("title")),
                        channel = cleanText(snippet.optString("channelTitle")),
                        channelId = channelId,
                        duration = detailsById[id].orEmpty(),
                        thumbnail = thumbnailUrl(snippet),
                        youtubeUrl = "https://www.youtube.com/watch?v=$id",
                    ),
                )
            }
        }
    }

    suspend fun channelShelves(
        channelId: String,
        maxPlaylistsPerSection: Int = 5,
        maxVideosPerShelf: Int = 14,
    ): List<ChannelShelf> = withContext(Dispatchers.IO) {
        requireConfigured()
        require(channelId.isNotBlank()) { "Channel ID is required." }

        val json = get(
            baseUrl + "/channelSections?part=snippet,contentDetails" +
                "&channelId=" + encode(channelId) +
                "&key=" + encode(apiKey),
        )

        val sections = json.getJSONArray("items")
            .let { items ->
                buildList {
                    for (index in 0 until items.length()) {
                        val item = items.getJSONObject(index)
                        val snippet = item.optJSONObject("snippet") ?: continue
                        add(
                            Triple(
                                snippet.optInt("position", Int.MAX_VALUE),
                                snippet.optString("type"),
                                item.optJSONObject("contentDetails"),
                            ) to snippet.optString("title"),
                        )
                    }
                }
            }
            .sortedBy { it.first.first }

        buildList {
            for ((metadata, sectionTitleRaw) in sections) {
                val (position, type, details) = metadata
                val sectionTitle = cleanText(
                    sectionTitleRaw.ifBlank { defaultSectionTitle(type) },
                )

                when (type) {
                    "recentUploads" -> {
                        val videos = channelVideos(
                            channelId = channelId,
                            maxResults = maxVideosPerShelf,
                            order = "date",
                        )
                        if (videos.isNotEmpty()) {
                            add(
                                ChannelShelf(
                                    position = position,
                                    title = sectionTitle,
                                    type = type,
                                    rows = listOf(
                                        ChannelShelfRow(
                                            title = sectionTitle,
                                            videos = videos,
                                        ),
                                    ),
                                ),
                            )
                        }
                    }

                    "popularUploads" -> {
                        val videos = channelVideos(
                            channelId = channelId,
                            maxResults = maxVideosPerShelf,
                            order = "viewCount",
                        )
                        if (videos.isNotEmpty()) {
                            add(
                                ChannelShelf(
                                    position = position,
                                    title = sectionTitle,
                                    type = type,
                                    rows = listOf(
                                        ChannelShelfRow(
                                            title = sectionTitle,
                                            videos = videos,
                                        ),
                                    ),
                                ),
                            )
                        }
                    }

                    "singlePlaylist", "multiplePlaylists" -> {
                        val playlistIds = details
                            ?.optJSONArray("playlists")
                            ?.let { playlistArray ->
                                buildList {
                                    for (index in 0 until playlistArray.length()) {
                                        val id = playlistArray.optString(index)
                                        if (id.isNotBlank()) add(id)
                                    }
                                }
                            }
                            .orEmpty()
                            .take(maxPlaylistsPerSection)

                        val rows = playlistIds.mapNotNull { playlistId ->
                            val playlistTitle = playlistTitle(playlistId)
                            val videos = playlistVideos(playlistId, maxVideosPerShelf)
                            if (videos.isEmpty()) {
                                null
                            } else {
                                ChannelShelfRow(
                                    title = playlistTitle.ifBlank { sectionTitle },
                                    playlistId = playlistId,
                                    videos = videos,
                                )
                            }
                        }

                        if (rows.isNotEmpty()) {
                            add(
                                ChannelShelf(
                                    position = position,
                                    title = sectionTitle,
                                    type = type,
                                    rows = rows,
                                ),
                            )
                        }
                    }

                    "allPlaylists" -> {
                        val playlistRows = channelPlaylists(channelId, maxPlaylistsPerSection)
                            .mapNotNull { playlist ->
                                val videos = playlistVideos(playlist.first, maxVideosPerShelf)
                                if (videos.isEmpty()) {
                                    null
                                } else {
                                    ChannelShelfRow(
                                        title = playlist.second,
                                        playlistId = playlist.first,
                                        videos = videos,
                                    )
                                }
                            }

                        if (playlistRows.isNotEmpty()) {
                            add(
                                ChannelShelf(
                                    position = position,
                                    title = sectionTitle,
                                    type = type,
                                    rows = playlistRows,
                                ),
                            )
                        }
                    }
                }
            }
        }.sortedBy { it.position }
    }

    private suspend fun playlistTitle(playlistId: String): String {
        val json = get(
            baseUrl + "/playlists?part=snippet&id=" +
                encode(playlistId) +
                "&key=" + encode(apiKey),
        )
        return json.getJSONArray("items")
            .optJSONObject(0)
            ?.optJSONObject("snippet")
            ?.optString("title")
            ?.let(::cleanText)
            .orEmpty()
    }

    private suspend fun playlistVideos(
        playlistId: String,
        maxResults: Int,
    ): List<Video> {
        val json = get(
            baseUrl + "/playlistItems?part=snippet,contentDetails" +
                "&playlistId=" + encode(playlistId) +
                "&maxResults=" + maxResults +
                "&key=" + encode(apiKey),
        )
        val items = json.getJSONArray("items")

        return buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val snippet = item.optJSONObject("snippet") ?: continue
                val resource = snippet.optJSONObject("resourceId") ?: continue
                val id = resource.optString("videoId")
                if (id.isBlank()) continue

                add(
                    Video(
                        id = id,
                        title = cleanText(snippet.optString("title")),
                        channel = cleanText(snippet.optString("videoOwnerChannelTitle"))
                            .ifBlank { cleanText(snippet.optString("channelTitle")) },
                        duration = "",
                        thumbnail = thumbnailUrl(snippet),
                        youtubeUrl = "https://www.youtube.com/watch?v=$id",
                        channelId = snippet.optString("videoOwnerChannelId")
                            .takeIf(String::isNotBlank),
                    ),
                )
            }
        }
    }

    private suspend fun channelPlaylists(
        channelId: String,
        maxResults: Int,
    ): List<Pair<String, String>> {
        val json = get(
            baseUrl + "/playlists?part=snippet" +
                "&channelId=" + encode(channelId) +
                "&maxResults=" + maxResults +
                "&key=" + encode(apiKey),
        )
        val items = json.getJSONArray("items")

        return buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val id = item.optString("id")
                val title = item.optJSONObject("snippet")
                    ?.optString("title")
                    ?.let(::cleanText)
                    .orEmpty()
                if (id.isNotBlank() && title.isNotBlank()) {
                    add(id to title)
                }
            }
        }
    }

    private fun defaultSectionTitle(type: String): String =
        when (type) {
            "recentUploads" -> "Latest videos"
            "popularUploads" -> "Popular uploads"
            "singlePlaylist" -> "Featured playlist"
            "multiplePlaylists" -> "Featured playlists"
            "allPlaylists" -> "Playlists"
            else -> "Featured"
        }

    private fun parseDurationSeconds(value: String): Long {
        val parts = value.split(":").mapNotNull { it.toLongOrNull() }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> 0L
        }
    }

    suspend fun search(
        query: String,
        maxResults: Int = 50,
    ): List<Video> = withContext(Dispatchers.IO) {
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
                val id = searchItems
                    .getJSONObject(index)
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
                val id = item
                    .getJSONObject("id")
                    .optString("videoId")

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
                        channelId = snippet.optString("channelId").takeIf(String::isNotBlank),
                        duration = details
                            ?.optJSONObject("contentDetails")
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

        return thumbnails.optJSONObject("high")
            ?.optString("url")
            ?.takeIf(String::isNotBlank)
            ?: thumbnails.optJSONObject("medium")
                ?.optString("url")
                ?.takeIf(String::isNotBlank)
            ?: thumbnails.optJSONObject("default")
                ?.optString("url")
                ?.takeIf(String::isNotBlank)
    }

    private fun cleanText(value: String): String =
        value
            .replace("&#39;", "'")
            .replace("&quot;", 34.toChar().toString())
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
