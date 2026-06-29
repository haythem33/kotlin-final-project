package com.example.finalproject.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.data.Habit
import com.example.finalproject.ui.theme.FinalProjectTheme

/**
 * STATEFUL Details screen. Observes the habit live via DetailsViewModel and
 * navigates back automatically once the habit has been deleted.
 */
@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    viewModel: DetailsViewModel = viewModel(factory = DetailsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // When the habit is gone (deleted), leave this screen.
    LaunchedEffect(uiState) {
        if (uiState is DetailsUiState.Deleted) onBack()
    }

    DetailsContent(
        uiState = uiState,
        onToggleDone = viewModel::toggleDone,
        onDelete = viewModel::delete,
        onBack = onBack,
    )
}

/** STATELESS Details UI. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsContent(
    uiState: DetailsUiState,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Details") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
        ) {
            when (uiState) {
                DetailsUiState.Loading, DetailsUiState.Deleted -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is DetailsUiState.Success -> {
                    val habit = uiState.habit
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (habit.isDone) "Status: Completed ✅" else "Status: Not done yet ⏳",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(32.dp))

                    // Toggle done/undone right here — updates the database and
                    // is reflected on the list screen too (same source of truth).
                    Button(
                        onClick = onToggleDone,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (habit.isDone) "Mark as not done" else "Mark as done")
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Delete habit")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsPreview() {
    FinalProjectTheme {
        DetailsContent(
            uiState = DetailsUiState.Success(
                Habit(id = 1, name = "Read 10 pages", isDone = true),
            ),
            onToggleDone = {},
            onDelete = {},
            onBack = {},
        )
    }
}
