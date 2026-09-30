package tv.publivoretube.data

data class ChannelInfo(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String?,
    val subscriberCount: String,
    val videoCount: String,
)
