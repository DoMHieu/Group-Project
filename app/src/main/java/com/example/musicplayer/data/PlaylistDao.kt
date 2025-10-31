package com.example.musicplayer.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
data class PlaylistWithSongs(

    @Embedded
    val playlist: UserPlaylist,

    @Relation(
        parentColumn = "playlistId",
        entityColumn = "id",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<FavouriteSong>
)

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: UserPlaylist): Long

    @Query("SELECT * FROM user_playlists WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getPlaylistByName(name: String): UserPlaylist?

    @Transaction
    @Query("SELECT * FROM user_playlists")
    suspend fun getAllPlaylistsWithSongs(): List<PlaylistWithSongs>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)
}