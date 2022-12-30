package com.example.practicaldemo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.practicaldemo.model.AddressesList

@Database(
    entities = [AddressesList::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        fun buildDatabase(context: Context) = Room.databaseBuilder(context, AppDatabase::class.java, "locationdata.db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .setJournalMode(JournalMode.TRUNCATE)
            .build()
    }

}