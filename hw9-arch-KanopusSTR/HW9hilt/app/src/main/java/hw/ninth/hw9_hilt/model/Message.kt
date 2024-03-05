package hw.ninth.hw9_hilt.model

import com.squareup.moshi.Json

data class Message(
    @field:Json(name = "id") val id: String?,
    @field:Json(name = "from") val from: String?,
    @field:Json(name = "to") val to: String?,
    @field:Json(name = "data") val data1: Data1,
    @field:Json(name = "time") val time: String?
)

data class Data1(
    @field:Json(name = "Text") val text: Data2?,
    @field:Json(name = "Image") val image: Data2?
)

data class Data2(
    @field:Json(name = "text") val text: String?,
    @field:Json(name = "link") val link: String?
)
