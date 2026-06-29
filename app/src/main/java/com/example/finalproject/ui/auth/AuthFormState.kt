package com.example.finalproject.ui.auth

/**
 * State of the login form itself (what the user typed + progress/errors).
 * This is separate from AuthState (who is signed in): one describes the
 * SCREEN, the other describes the SESSION.
 */
data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
