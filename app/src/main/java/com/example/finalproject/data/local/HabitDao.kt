package com.example.finalproject.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.finalproject.data.Habit
import kotlinx.coroutines.flow.Flow

/**
 * DAO = Data Access Object. This interface declares WHAT database operations
 * exist; Room generates the actual SQL-running implementation at build time
 * (that's what the KSP annotation processor does).
 *
 * Two important Room patterns to be able to explain:
 *
 *  - observeHabits() returns Flow<List<Habit>>. Room keeps this Flow alive
 *    and EMITS A NEW LIST every time the habits table changes. So when we
 *    insert or update a habit, the UI updates automatically — we never have
 *    to manually refresh. This is the heart of a reactive, offline-first app.
 *
 *  - insert/update/delete are `suspend` functions. Database I/O must not run
 *    on the main thread (it would freeze the UI). `suspend` forces callers to
 *    run them inside a coroutine, and Room moves the work to a background
 *    thread for us.
 */
@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE ownerId = :ownerId ORDER BY id ASC")
    fun observeHabits(ownerId: String): Flow<List<Habit>>

    /** Observe ONE habit by id. Emits null if it doesn't exist (e.g. deleted). */
    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeHabit(id: Int): Flow<Habit?>

    @Insert
    suspend fun insert(habit: Habit)

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)
}
