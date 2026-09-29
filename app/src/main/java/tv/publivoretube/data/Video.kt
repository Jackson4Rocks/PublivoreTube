package tv.publivoretube.data

data class Video(
    val id: String,
    val title: String,
    val channel: String,
    val duration: String,
    val thumbnail: String? = null,
)
