package com.example.finalproject.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.navigation.toRoute
import com.example.finalproject.HabitApplication
import com.example.finalproject.data.HabitRepository
import com.example.finalproject.navigation.DetailsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Brain of the Details screen.
 *
 * It needs to know WHICH habit to show. Navigation already stored the
 * DetailsRoute argument (habitId) in the SavedStateHandle, so we read it back
 * with savedStateHandle.toRoute<DetailsRoute>(). This is the recommended way
 * to get navigation arguments into a ViewModel — cleaner and more testable
 * than passing them through composables by hand.
 */
class DetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
) : ViewModel() {

    private val habitId: Int = savedStateHandle.toRoute<DetailsRoute>().habitId

    /**
     * Observe this single habit LIVE. If it becomes null (deleted), we map to
     * the Deleted state so the screen can navigate back automatically.
     */
    val uiState: StateFlow<DetailsUiState> =
        repository.observeHabit(habitId)
            .map { habit ->
                if (habit == null) DetailsUiState.Deleted else DetailsUiState.Success(habit)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetailsUiState.Loading,
            )

    fun toggleDone() {
        val habit = (uiState.value as? DetailsUiState.Success)?.habit ?: return
        viewModelScope.launch { repository.toggleDone(habit) }
    }

    fun delete() {
        val habit = (uiState.value as? DetailsUiState.Success)?.habit ?: return
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as HabitApplication
                // createSavedStateHandle() builds a SavedStateHandle pre-filled
                // with the navigation arguments for this destination.
                DetailsViewModel(createSavedStateHandle(), app.habitRepository)
            }
        }
    }
}
