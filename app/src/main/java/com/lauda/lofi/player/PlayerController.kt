package com.lauda.lofi.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.lauda.lofi.data.Track

class PlayerController(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    fun play(track: Track) {
        val uri = track.streamUrl ?: track.localUri ?: return
        player.setMediaItem(
            MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(uri)
                .build()
        )
        player.prepare()
        player.playWhenReady = true
    }

    fun release() = player.release()
}
