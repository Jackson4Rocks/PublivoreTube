package tv.publivoretube.data

data class ChannelShelf(
    val position: Int,
    val title: String,
    val type: String,
    val rows: List<ChannelShelfRow>,
)

data class ChannelShelfRow(
    val title: String,
    val playlistId: String? = null,
    val videos: List<Video>,
)
