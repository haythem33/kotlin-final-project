package com.example.finalproject.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.ui.theme.FinalProjectTheme

/** STATEFUL login screen: connects the AuthViewModel to the stateless UI. */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    LoginContent(
        form = form,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = viewModel::signIn,
        onSignUp = viewModel::signUp,
    )
}

/** STATELESS login UI — previewable and easy to reason about. */
@Composable
fun LoginContent(
    form: AuthFormState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Welcome to Habit Tracker",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Enter your email and password, then tap Sign In — " +
                "or tap Create Account if you're new (password must be 6+ characters).",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(24.dp))

        TextField(
            value = form.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        TextField(
            value = form.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            // Hide the characters as the user types.
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        if (form.errorMessage != null) {
            Text(
                text = form.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
        }

        if (form.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Sign In")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSignUp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Create Account")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    FinalProjectTheme {
        LoginContent(
            form = AuthFormState(email = "student@uni.edu"),
            onEmailChange = {},
            onPasswordChange = {},
            onSignIn = {},
            onSignUp = {},
        )
    }
}
