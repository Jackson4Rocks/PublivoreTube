package tv.publivoretube.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class WatchHistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(): List<Video> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val json = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()

        return buildList {
            for (index in 0 until json.length()) {
                val item = json.optJSONObject(index) ?: continue
                add(
                    Video(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        channel = item.optString("channel"),
                        duration = item.optString("duration"),
                        thumbnail = item.optString("thumbnail").takeIf(String::isNotBlank),
                        youtubeUrl = item.optString("youtubeUrl").takeIf(String::isNotBlank),
                        channelId = item.optString("channelId").takeIf(String::isNotBlank),
                    ),
                )
            }
        }
    }

    fun add(video: Video) {
        val current = load().filterNot { it.id == video.id }.toMutableList()
        current.add(0, video)
        save(current.take(MAX_ITEMS))
    }

    fun clear() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun save(videos: List<Video>) {
        val json = JSONArray()
        videos.forEach { video ->
            json.put(
                JSONObject().apply {
                    put("id", video.id)
                    put("title", video.title)
                    put("channel", video.channel)
                    put("duration", video.duration)
                    put("thumbnail", video.thumbnail ?: "")
                    put("youtubeUrl", video.youtubeUrl ?: "")
                    put("channelId", video.channelId ?: "")
                },
            )
        }
        prefs.edit().putString(KEY_HISTORY, json.toString()).apply()
    }

    private companion object {
        const val PREFS = "watch_history"
        const val KEY_HISTORY = "items"
        const val MAX_ITEMS = 40
    }
}
