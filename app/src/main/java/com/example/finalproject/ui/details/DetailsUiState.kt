package com.example.finalproject.ui.details

import com.example.finalproject.data.Habit

/**
 * State of the Details screen.
 *  - Loading: still reading the habit from the database
 *  - Success: we have the habit to show
 *  - Deleted: the habit no longer exists (the user deleted it) -> leave screen
 */
sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val habit: Habit) : DetailsUiState
    data object Deleted : DetailsUiState
}
