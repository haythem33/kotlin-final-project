package com.example.finalproject

import com.example.finalproject.data.Habit
import com.example.finalproject.data.local.HabitDao
import com.example.finalproject.data.local.QuoteDao
import com.example.finalproject.data.local.QuoteEntity
import com.example.finalproject.network.QuoteApi
import com.example.finalproject.network.QuoteDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * A fake HabitDao backed by an in-memory list + a Flow, so we can test the
 * ViewModel without a real Room database. It also RECORDS what it was asked to
 * do (insertedNames, lastUpdated) so tests can assert on behaviour.
 */
class FakeHabitDao : HabitDao {
    private val habitsFlow = MutableStateFlow<List<Habit>>(emptyList())
    val insertedNames = mutableListOf<String>()
    var lastUpdated: Habit? = null
    private var nextId = 1

    override fun observeHabits(ownerId: String): Flow<List<Habit>> =
        habitsFlow.map { list -> list.filter { it.ownerId == ownerId } }

    override fun observeHabit(id: Int): Flow<Habit?> =
        habitsFlow.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(habit: Habit) {
        insertedNames += habit.name
        habitsFlow.value = habitsFlow.value + habit.copy(id = nextId++)
    }

    override suspend fun update(habit: Habit) {
        lastUpdated = habit
        habitsFlow.value = habitsFlow.value.map { if (it.id == habit.id) habit else it }
    }

    override suspend fun delete(habit: Habit) {
        habitsFlow.value = habitsFlow.value.filterNot { it.id == habit.id }
    }
}

/** A fake QuoteApi that either returns a canned quote or simulates a network failure. */
class FakeQuoteApi : QuoteApi {
    var shouldThrow = false
    var dto = QuoteDto(id = 1, quote = "Default quote", author = "Default author")

    override suspend fun getRandomQuote(): QuoteDto {
        if (shouldThrow) throw IOException("Simulated network failure")
        return dto
    }
}

/** A fake QuoteDao holding a single cached quote in memory. */
class FakeQuoteDao : QuoteDao {
    var cached: QuoteEntity? = null

    override suspend fun getQuote(): QuoteEntity? = cached

    override suspend fun upsert(quote: QuoteEntity) {
        cached = quote
    }
}
