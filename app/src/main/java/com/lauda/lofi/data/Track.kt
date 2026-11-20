package com.lauda.lofi.data

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val artwork: String? = null,
    val streamUrl: String? = null,
    val durationMs: Long = 0L,
    val localUri: String? = null
)
