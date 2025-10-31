package com.example.musicplayer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// THÊM UserPlaylist::class và PlaylistSongCrossRef::class vào entities
@Database(
    entities = [FavouriteSong::class, UserPlaylist::class, PlaylistSongCrossRef::class],
    version = 1, // (Nếu bạn đã build, bạn cần tăng version lên 2 và thêm .fallbackToDestructiveMigration())
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favouriteDao(): FavouriteDao

    // THÊM DAO MỚI NÀY
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_app_database"
                )
                    // (Nếu bạn tăng version, hãy thêm dòng này vào trước .build())
                    // .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}