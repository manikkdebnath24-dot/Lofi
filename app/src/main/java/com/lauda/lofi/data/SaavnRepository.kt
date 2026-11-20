package com.lauda.lofi.data

import com.lauda.lofi.network.NetworkModule
import com.lauda.lofi.network.toTrack

class SaavnRepository {
    private val primary = NetworkModule.create("https://saavn.sumit.co/api/")
    private val fallback = NetworkModule.create("https://saavn.dev/api/")

    suspend fun search(query: String): Result<List<Track>> {
        if (query.isBlank()) return Result.success(emptyList())
        return try {
            Result.success(primary.searchSongs(query).data?.results.orEmpty().map { it.toTrack() })
        } catch (first: Exception) {
            try {
                Result.success(fallback.searchSongs(query).data?.results.orEmpty().map { it.toTrack() })
            } catch (second: Exception) {
                Result.failure(second)
            }
        }
    }
}
