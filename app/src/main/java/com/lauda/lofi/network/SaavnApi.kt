package com.lauda.lofi.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SaavnApi {
    @GET("search/songs")
    suspend fun searchSongs(@Query("query") query: String): SearchResponse

    @GET("songs/{id}")
    suspend fun song(@Path("id") id: String): SongResponse
}

@JsonClass(generateAdapter = true)
data class SearchResponse(
    @Json(name = "data") val data: SearchData?
)

@JsonClass(generateAdapter = true)
data class SearchData(
    @Json(name = "results") val results: List<ApiTrack> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SongResponse(
    @Json(name = "data") val data: List<ApiTrack> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiTrack(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "primaryArtists") val primaryArtists: String? = null,
    @Json(name = "artists") val artists: Artists? = null,
    @Json(name = "album") val album: Album? = null,
    @Json(name = "image") val image: List<ImageItem>? = null,
    @Json(name = "downloadUrl") val downloadUrl: List<DownloadItem>? = null,
    @Json(name = "duration") val duration: Int? = null
)

@JsonClass(generateAdapter = true)
data class Artists(
    @Json(name = "primary") val primary: String? = null
)

@JsonClass(generateAdapter = true)
data class Album(
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class ImageItem(
    @Json(name = "url") val url: String? = null,
    @Json(name = "quality") val quality: String? = null
)

@JsonClass(generateAdapter = true)
data class DownloadItem(
    @Json(name = "url") val url: String? = null,
    @Json(name = "quality") val quality: String? = null
)

fun ApiTrack.toTrack(): com.lauda.lofi.data.Track {
    val bestImage = image?.lastOrNull()?.url
    val bestAudio = downloadUrl?.lastOrNull()?.url
    return com.lauda.lofi.data.Track(
        id = id ?: name.orEmpty(),
        title = title ?: name ?: "Unknown",
        artist = artists?.primary ?: primaryArtists ?: "Unknown artist",
        album = album?.name.orEmpty(),
        artwork = bestImage,
        streamUrl = bestAudio,
        durationMs = (duration ?: 0) * 1000L
    )
}
