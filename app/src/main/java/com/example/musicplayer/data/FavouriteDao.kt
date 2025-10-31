package com.example.musicplayer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourite_songs ORDER BY lastFetchTime DESC")
    suspend fun getAll(): List<FavouriteSong>
    @Query("SELECT * FROM favourite_songs WHERE id = :songId LIMIT 1")
    suspend fun getById(songId: Long): FavouriteSong?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: FavouriteSong)
    @Delete
    suspend fun delete(song: FavouriteSong)
}