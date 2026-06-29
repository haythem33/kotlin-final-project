package com.example.finalproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finalproject.data.AuthState
import com.example.finalproject.ui.auth.AuthViewModel
import com.example.finalproject.ui.auth.LoginScreen
import com.example.finalproject.ui.details.DetailsScreen
import com.example.finalproject.ui.main.MainScreen

/**
 * The navigation graph, now gated by authentication (rubric #5).
 *
 * The AuthViewModel is created once here and shared. We OBSERVE its authState
 * and let navigation react to it — we never navigate manually from inside the
 * login screen. That keeps a single source of truth: "the user is signed in"
 * is decided by Firebase, and the UI simply follows.
 */
@Composable
fun AppNavHost(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Pick the first screen from the CURRENT auth state (read synchronously),
    // so an already-signed-in user doesn't see a login flash on launch.
    val startDestination: Any = remember {
        if (authViewModel.authState.value is AuthState.SignedIn) MainRoute else LoginRoute
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<LoginRoute> {
            LoginScreen(viewModel = authViewModel)
        }

        composable<MainRoute> {
            MainScreen(
                onHabitClick = { habit ->
                    navController.navigate(DetailsRoute(habitId = habit.id))
                },
                onLogout = { authViewModel.signOut() },
            )
        }

        composable<DetailsRoute> {
            // DetailsViewModel reads its habitId from the navigation argument
            // via SavedStateHandle, so we don't pass it through here.
            DetailsScreen(onBack = { navController.popBackStack() })
        }
    }

    // React to sign-in / sign-out. launchSingleTop + popUpTo keep the back
    // stack clean (e.g. after login, Back must NOT return to the login screen).
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignedIn -> navController.navigate(MainRoute) {
                popUpTo(LoginRoute) { inclusive = true }
                launchSingleTop = true
            }
            AuthState.SignedOut -> navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
