package com.example.finalproject.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.finalproject.data.Habit

/**
 * The Room database itself: the single connection to the SQLite file on the
 * device. It lists its tables (entities) and exposes the DAO(s).
 *
 * We use the classic thread-safe singleton (getInstance) so the whole app
 * shares ONE database instance. Creating multiple Room instances for the same
 * file is wasteful and can cause locking problems.
 */
@Database(entities = [Habit::class, QuoteEntity::class], version = 3, exportSchema = false)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        fun getInstance(context: Context): HabitDatabase {
            // Double-checked locking: fast path if already created, otherwise
            // synchronize so only one thread builds it.
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_database",
                )
                    // Habits are now per-user, so we no longer seed sample data
                    // (a new user simply starts with an empty list).
                    //
                    // We bumped the DB version to add columns/tables over time.
                    // Rather than write migrations for an unreleased app, we let
                    // Room wipe & rebuild on schema change. (A shipped app would
                    // write a real Migration instead.)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
