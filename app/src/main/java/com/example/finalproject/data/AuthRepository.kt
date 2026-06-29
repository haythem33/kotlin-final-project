package com.example.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Who is signed in, expressed as a closed set of states. The rest of the app
 * navigates based on this and never talks to FirebaseAuth directly.
 */
sealed interface AuthState {
    data class SignedIn(val email: String?) : AuthState
    data object SignedOut : AuthState
}

/**
 * The single wrapper around Firebase Authentication (rubric #5).
 *
 * Keeping all Firebase calls here means:
 *   - the ViewModel/UI stay testable and Firebase-agnostic,
 *   - if we swapped auth providers, only this file changes.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    /**
     * A live stream of the sign-in state. Firebase notifies us via an
     * AuthStateListener whenever the user signs in or out; callbackFlow turns
     * that callback-based API into a Kotlin Flow. awaitClose removes the
     * listener when nobody is collecting, preventing a memory leak.
     */
    val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(if (user != null) AuthState.SignedIn(user.email) else AuthState.SignedOut)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** The CURRENT state, read synchronously — used as the Flow's initial value
     *  so there is no "logged-out flash" on a cold start when already signed in. */
    fun currentState(): AuthState =
        auth.currentUser?.let { AuthState.SignedIn(it.email) } ?: AuthState.SignedOut

    /** The signed-in user's unique id (uid), or null if signed out. Used to
     *  scope habits to their owner. */
    fun currentUserId(): String? = auth.currentUser?.uid

    /** Sign in an existing user. Throws on failure (wrong password, etc.). */
    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    /** Create a new account. Throws on failure (email in use, weak password). */
    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    fun signOut() = auth.signOut()
}
