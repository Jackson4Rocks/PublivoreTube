package tv.publivoretube.data

interface VideoRepository {
    suspend fun home(): List<Video>
    suspend fun search(query: String): List<Video>
}

class DemoVideoRepository : VideoRepository {
    override suspend fun home(): List<Video> = homeStatic()

    override suspend fun search(query: String): List<Video> = homeStatic().filter {
        it.title.contains(query, ignoreCase = true) ||
            it.channel.contains(query, ignoreCase = true)
    }

    fun homeStatic(): List<Video> = listOf(
        Video("demo-1", "PublivoreTube — First Look", "PublivoreTube", "08:42"),
        Video("demo-2", "Android TV UI Architecture", "PublivoreTube Labs", "14:18"),
        Video("demo-3", "Building a Leanback Experience", "PublivoreTube Labs", "11:07"),
        Video("demo-4", "Media3 Playback Foundations", "PublivoreTube Labs", "17:33"),
        Video("demo-5", "SponsorBlock Integration Plan", "PublivoreTube", "09:54"),
        Video("demo-6", "Remote-Friendly Navigation", "PublivoreTube Labs", "06:31"),
    )
}
