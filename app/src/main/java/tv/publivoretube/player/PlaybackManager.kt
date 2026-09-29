package tv.publivoretube.player

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

class PlaybackManager(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    fun release() {
        player.release()
    }
}
