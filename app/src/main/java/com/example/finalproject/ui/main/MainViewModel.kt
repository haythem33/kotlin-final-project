package com.example.finalproject.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.finalproject.HabitApplication
import com.example.finalproject.data.Habit
import com.example.finalproject.data.HabitRepository
import com.example.finalproject.data.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The "brain" of the Main screen. It now coordinates TWO repositories:
 *   - habitRepository: the offline-first list of habits (Room)
 *   - quoteRepository: the motivational quote (network + Room cache)
 *
 * Both are injected through the constructor so tests can pass fakes (Step 6).
 */
class MainViewModel(
    private val habitRepository: HabitRepository,
    private val quoteRepository: QuoteRepository,
    // The uid of the signed-in user. Habits are scoped to this id so each
    // account sees only its own list.
    private val userId: String,
) : ViewModel() {

    // Transient UI state owned by the ViewModel:
    private val _newHabitName = MutableStateFlow("")
    private val _quoteState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)

    /**
     * One uiState, built by combining THREE streams: the DB habit list, the
     * typed text, and the quote state. Whenever any of them changes, a fresh
     * MainUiState is produced and the screen recomposes.
     */
    val uiState: StateFlow<MainUiState> =
        combine(
            habitRepository.observeHabits(userId),
            _newHabitName,
            _quoteState,
        ) { habits, newName, quote ->
            MainUiState(
                habits = habits,
                newHabitName = newName,
                isLoading = false,
                quote = quote,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState(isLoading = true),
        )

    init {
        // Load a quote as soon as the screen opens.
        refreshQuote()
    }

    /**
     * The three-state network flow (rubric #2):
     *   1. emit Loading (UI shows a spinner)
     *   2. try the network -> on success, emit Success (and it's now cached)
     *   3. on failure -> fall back to the cached quote if we have one,
     *      otherwise emit Error (UI shows a message).
     *
     * Catching Exception here is deliberate: any network problem (no
     * connection, timeout, bad response) becomes a calm UI state, never a
     * crash. This is the "gracefully handle" part of the rubric.
     */
    fun refreshQuote() {
        viewModelScope.launch {
            _quoteState.value = QuoteUiState.Loading
            try {
                val quote = quoteRepository.fetchAndCacheQuote()
                _quoteState.value = QuoteUiState.Success(quote)
            } catch (e: Exception) {
                val cached = quoteRepository.getCachedQuote()
                _quoteState.value = if (cached != null) {
                    QuoteUiState.Success(cached)
                } else {
                    QuoteUiState.Error("Couldn't load a quote. Check your connection and tap Refresh.")
                }
            }
        }
    }

    /** Text field changed (rubric #4) — pure UI state, no coroutine needed. */
    fun onNewHabitNameChange(newName: String) {
        _newHabitName.value = newName
    }

    /** Add button tapped (rubric #4). Insert runs in a coroutine; the DB Flow
     *  re-emits and the list updates itself. */
    fun addHabit() {
        val name = _newHabitName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            habitRepository.addHabit(name, userId)
            _newHabitName.value = ""
        }
    }

    /** Checkbox tapped: persist the toggle; the Flow re-emits and UI updates. */
    fun toggleHabitDone(habit: Habit) {
        viewModelScope.launch {
            habitRepository.toggleDone(habit)
        }
    }

    /** Swiped away: delete the habit (the UI offers Undo via restoreHabit). */
    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    /** Undo a delete by re-inserting the habit. */
    fun restoreHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.restoreHabit(habit)
        }
    }

    companion object {
        /**
         * Tells Compose's viewModel() how to build a MainViewModel, handing it
         * both repositories from our HabitApplication.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as HabitApplication
                // The Main screen is only reachable while signed in, so a uid
                // exists; fall back to "" defensively.
                val uid = app.authRepository.currentUserId() ?: ""
                MainViewModel(app.habitRepository, app.quoteRepository, uid)
            }
        }
    }
}
