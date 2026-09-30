package tv.publivoretube.player

import androidx.media3.common.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.stream.StreamInfo

data class PlaybackSource(
    val url: String,
    val mimeType: String? = null,
    val qualityLabel: String = "Auto",
)

class YouTubePlaybackResolver {
    suspend fun resolve(youtubeUrl: String): PlaybackSource = withContext(Dispatchers.IO) {
        ExtractorBootstrap.ensureInitialized()

        val info = StreamInfo.getInfo(youtubeUrl)

        info.getDashMpdUrl()
            .takeIf(String::isNotBlank)
            ?.let { dashUrl ->
                return@withContext PlaybackSource(
                    url = dashUrl,
                    mimeType = MimeTypes.APPLICATION_MPD,
                    qualityLabel = "Auto",
                )
            }

        info.getHlsUrl()
            .takeIf(String::isNotBlank)
            ?.let { hlsUrl ->
                return@withContext PlaybackSource(
                    url = hlsUrl,
                    mimeType = MimeTypes.APPLICATION_M3U8,
                    qualityLabel = "Auto",
                )
            }

        val progressive = info.videoStreams
            .asSequence()
            .filter { it.isUrl && !it.isVideoOnly && it.content.isNotBlank() }
            .maxWithOrNull(
                compareBy(
                    { it.height },
                    { it.width },
                    { it.fps },
                ),
            )
            ?: throw IllegalStateException("No playable YouTube video stream was extracted.")

        PlaybackSource(
            url = progressive.content,
            qualityLabel = progressive.resolution.ifBlank { "Auto" },
        )
    }
}
