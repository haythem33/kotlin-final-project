package com.example.finalproject.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.finalproject.HabitApplication
import com.example.finalproject.data.AuthRepository
import com.example.finalproject.data.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives authentication. It exposes TWO independent states:
 *   - authState: who is signed in (used by navigation to gate screens)
 *   - formState: the login form's text + loading + error
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    /** Sign-in state as a hot StateFlow. Its initial value is read
     *  synchronously from Firebase so navigation knows immediately. */
    val authState: StateFlow<AuthState> =
        repository.authState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = repository.currentState(),
        )

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    fun onEmailChange(value: String) {
        _formState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _formState.update { it.copy(password = value, errorMessage = null) }
    }

    fun signIn() = submit { repository.signIn(it.email.trim(), it.password) }

    fun signUp() = submit { repository.signUp(it.email.trim(), it.password) }

    /**
     * Shared logic for both sign-in and sign-up: validate, show loading, run
     * the suspend auth call, and turn any failure into a friendly message
     * instead of a crash. On success we DON'T navigate here — authState flips
     * and the navigation layer reacts (single source of truth).
     */
    private fun submit(action: suspend (AuthFormState) -> Unit) {
        val form = _formState.value
        if (form.email.isBlank() || form.password.isBlank()) {
            _formState.update { it.copy(errorMessage = "Email and password are required.") }
            return
        }
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                action(form)
                _formState.value = AuthFormState() // clear the form on success
            } catch (e: Exception) {
                _formState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Authentication failed.")
                }
            }
        }
    }

    fun signOut() = repository.signOut()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as HabitApplication
                AuthViewModel(app.authRepository)
            }
        }
    }
}
