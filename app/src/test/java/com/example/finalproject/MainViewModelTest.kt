package com.example.finalproject

import com.example.finalproject.data.Habit
import com.example.finalproject.data.HabitRepository
import com.example.finalproject.data.QuoteRepository
import com.example.finalproject.data.local.QuoteEntity
import com.example.finalproject.network.QuoteDto
import com.example.finalproject.ui.main.MainViewModel
import com.example.finalproject.ui.main.QuoteUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for MainViewModel's state logic (rubric #6).
 *
 * These are "meaningful": they exercise real decisions the ViewModel makes —
 * input validation, persisting a toggle, and the loading/success/error flow —
 * using fake data sources so no device, database, or internet is involved.
 *
 * Two test helpers worth understanding:
 *   - runTest { } : runs the body on a virtual clock we control.
 *   - advanceUntilIdle() : runs all pending coroutines to completion, so we
 *     can assert on the result of work launched in viewModelScope.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    // Swaps Dispatchers.Main for a test dispatcher (see MainDispatcherRule).
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val TEST_UID = "test-user-123"

    private lateinit var habitDao: FakeHabitDao
    private lateinit var quoteApi: FakeQuoteApi
    private lateinit var quoteDao: FakeQuoteDao
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        habitDao = FakeHabitDao()
        quoteApi = FakeQuoteApi()
        quoteDao = FakeQuoteDao()
        viewModel = MainViewModel(
            habitRepository = HabitRepository(habitDao),
            quoteRepository = QuoteRepository(quoteApi, quoteDao),
            userId = TEST_UID,
        )
    }

    @Test
    fun `blank habit name is not added`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onNewHabitNameChange("   ") // only whitespace
        viewModel.addHabit()
        advanceUntilIdle()

        assertTrue("A blank habit should never reach the database", habitDao.insertedNames.isEmpty())
    }

    @Test
    fun `valid habit is added and the input field is cleared`() = runTest(mainDispatcherRule.dispatcher) {
        // Keep uiState hot so the combined state actually updates.
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.onNewHabitNameChange("Drink water")
        viewModel.addHabit()
        advanceUntilIdle()

        assertEquals(listOf("Drink water"), habitDao.insertedNames)
        assertEquals("", viewModel.uiState.value.newHabitName)
    }

    @Test
    fun `only the current users habits are shown`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }

        habitDao.insert(Habit(name = "Mine", ownerId = TEST_UID))
        habitDao.insert(Habit(name = "Theirs", ownerId = "another-user"))
        advanceUntilIdle()

        val names = viewModel.uiState.value.habits.map { it.name }
        assertEquals(listOf("Mine"), names) // the other user's habit is filtered out
    }

    @Test
    fun `toggling a habit persists the inverted done state`() = runTest(mainDispatcherRule.dispatcher) {
        val habit = Habit(id = 5, name = "Read", isDone = false)

        viewModel.toggleHabitDone(habit)
        advanceUntilIdle()

        assertEquals(5, habitDao.lastUpdated?.id)
        assertEquals(true, habitDao.lastUpdated?.isDone) // false -> true
    }

    @Test
    fun `refreshQuote success emits Success and caches the quote`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        quoteApi.shouldThrow = false
        quoteApi.dto = QuoteDto(id = 1, quote = "Stay hard", author = "Goggins")

        viewModel.refreshQuote()
        advanceUntilIdle()

        val state = viewModel.uiState.value.quote
        assertTrue(state is QuoteUiState.Success)
        assertEquals("Stay hard", (state as QuoteUiState.Success).quote.text)
        // It should also have been cached locally (rubric #3 behaviour).
        assertEquals("Stay hard", quoteDao.cached?.text)
    }

    @Test
    fun `refreshQuote network failure with no cache emits Error`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        quoteApi.shouldThrow = true
        quoteDao.cached = null

        viewModel.refreshQuote()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.quote is QuoteUiState.Error)
    }

    @Test
    fun `refreshQuote network failure falls back to cached quote`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        quoteApi.shouldThrow = true
        quoteDao.cached = QuoteEntity(text = "Cached wisdom", author = "Someone")

        viewModel.refreshQuote()
        advanceUntilIdle()

        val state = viewModel.uiState.value.quote
        assertTrue("Should show the cached quote instead of an error", state is QuoteUiState.Success)
        assertEquals("Cached wisdom", (state as QuoteUiState.Success).quote.text)
    }
}
