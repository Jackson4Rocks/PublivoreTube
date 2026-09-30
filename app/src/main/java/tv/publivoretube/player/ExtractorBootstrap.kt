package tv.publivoretube.player

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.Localization

object ExtractorBootstrap {
    @Volatile
    private var initialized = false

    fun ensureInitialized() {
        if (initialized) return

        synchronized(this) {
            if (initialized) return

            NewPipe.init(
                ExtractorDownloader(),
                Localization("en", "US"),
            )
            initialized = true
        }
    }
}
