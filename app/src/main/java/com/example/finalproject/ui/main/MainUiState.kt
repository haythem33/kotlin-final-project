package com.example.finalproject.ui.main

import com.example.finalproject.data.Habit

/**
 * The complete, single source of truth for the Main Habit List screen.
 *
 * Everything the screen needs to draw itself lives in ONE object:
 *   - the list of habits to show
 *   - what the user has currently typed into the "new habit" text field
 *   - whether we are loading (used later for the network spinner, rubric #2)
 *   - an optional error message (used later for the error state, rubric #2)
 *
 * The screen will simply read these fields and render them. It holds none
 * of this itself — that is the "state lives in the ViewModel" rule.
 *
 * Defaults are provided for every field so the very first state the screen
 * sees is a valid, empty-but-safe state (no nulls to crash on).
 */
data class MainUiState(
    val habits: List<Habit> = emptyList(),
    val newHabitName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // The motivational-quote section's state (rubric #2). Starts Loading.
    val quote: QuoteUiState = QuoteUiState.Loading,
)
