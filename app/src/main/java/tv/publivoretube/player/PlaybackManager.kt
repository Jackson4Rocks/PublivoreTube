package tv.publivoretube.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class PlaybackManager(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setSeekBackIncrementMs(10_000)
        setSeekForwardIncrementMs(10_000)
        playWhenReady = true
    }

    fun play(source: PlaybackSource) {
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(source.url)
                .apply {
                    source.mimeType?.let { setMimeType(it) }
                }
                .build(),
        )
        player.prepare()
        player.play()
    }

    fun togglePlayback() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekBy(deltaMs: Long) {
        val duration = player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
        player.seekTo((player.currentPosition + deltaMs).coerceIn(0L, duration))
    }

    fun release() {
        player.release()
    }
}
