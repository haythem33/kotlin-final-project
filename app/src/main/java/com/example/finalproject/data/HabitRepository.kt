package com.example.finalproject.data

import com.example.finalproject.data.local.HabitDao
import kotlinx.coroutines.flow.Flow

/**
 * The single gateway to habit data for the rest of the app.
 *
 * Why have a repository at all (the ViewModel could call the DAO directly)?
 *   - It hides WHERE data comes from. Today it's only Room; in Step 4 we add
 *     a network source here and decide how local + remote combine. The
 *     ViewModel never learns about that change.
 *   - It gives us one clean, fake-able type to inject into the ViewModel,
 *     which makes the ViewModel unit-testable (Step 6).
 */
class HabitRepository(private val dao: HabitDao) {

    /** A live stream of THIS user's habits; updates whenever the table changes. */
    fun observeHabits(ownerId: String): Flow<List<Habit>> = dao.observeHabits(ownerId)

    /** A live stream of a single habit (null once it's deleted). */
    fun observeHabit(id: Int): Flow<Habit?> = dao.observeHabit(id)

    /** Insert a brand-new habit owned by ownerId (id = 0 lets Room auto-assign). */
    suspend fun addHabit(name: String, ownerId: String) {
        dao.insert(Habit(name = name, ownerId = ownerId))
    }

    /** Flip a habit's done state by saving a copy with isDone inverted. */
    suspend fun toggleDone(habit: Habit) {
        dao.update(habit.copy(isDone = !habit.isDone))
    }

    /** Permanently remove a habit. */
    suspend fun deleteHabit(habit: Habit) {
        dao.delete(habit)
    }

    /** Re-insert a previously deleted habit (used by the Undo action). Passing
     *  the original habit keeps its id, so it returns to its place in the list. */
    suspend fun restoreHabit(habit: Habit) {
        dao.insert(habit)
    }
}
