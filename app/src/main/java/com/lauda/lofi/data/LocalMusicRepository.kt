package com.lauda.lofi.data

import android.content.ContentResolver
import android.provider.MediaStore

class LocalMusicRepository(private val resolver: ContentResolver) {
    fun scan(): List<Track> {
        val result = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )
        resolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { c ->
            val id = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val duration = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val mediaId = c.getLong(id)
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    .buildUpon().appendPath(mediaId.toString()).build().toString()
                result += Track(
                    id = "local:$mediaId",
                    title = c.getString(title) ?: "Unknown",
                    artist = c.getString(artist) ?: "Unknown artist",
                    album = c.getString(album) ?: "",
                    durationMs = c.getLong(duration),
                    localUri = uri,
                    streamUrl = uri
                )
            }
        }
        return result
    }
}
