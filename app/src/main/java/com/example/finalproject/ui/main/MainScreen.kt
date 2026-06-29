package com.example.finalproject.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.data.Habit
import com.example.finalproject.data.Quote
import com.example.finalproject.ui.theme.FinalProjectTheme
import kotlinx.coroutines.launch

/**
 * STATEFUL screen.
 *
 * This thin wrapper is the only Composable that touches the ViewModel. Its
 * job is to:
 *   1. obtain the MainViewModel (viewModel() gives us the SAME instance
 *      across recompositions and rotations),
 *   2. observe its state, and
 *   3. hand plain data + callbacks down to the stateless content below.
 *
 * Why collectAsStateWithLifecycle() and not plain collectAsState()?
 *   It stops collecting the StateFlow when the screen is in the background
 *   (STOPPED), so we don't waste work/battery updating a screen nobody sees.
 *   It is the recommended collector for Android UIs.
 */
@Composable
fun MainScreen(
    onHabitClick: (Habit) -> Unit,
    onLogout: () -> Unit,
    // We pass MainViewModel.Factory because the ViewModel now needs the
    // repository in its constructor — viewModel() can't build that on its own.
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
) {
    // "by" + getValue lets us read uiState.field directly (delegated property).
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainScreenContent(
        uiState = uiState,
        onNewHabitNameChange = viewModel::onNewHabitNameChange,
        onAddHabit = viewModel::addHabit,
        onToggleDone = viewModel::toggleHabitDone,
        onHabitClick = onHabitClick,
        onRefreshQuote = viewModel::refreshQuote,
        onLogout = onLogout,
        onDeleteHabit = viewModel::deleteHabit,
        onRestoreHabit = viewModel::restoreHabit,
    )
}

/**
 * STATELESS content.
 *
 * This receives everything as parameters and owns no state. Benefits:
 *   - It can be shown in a @Preview (below) without a ViewModel.
 *   - It is trivial to reason about and test: same inputs -> same UI.
 * This separation is exactly the "View is dumb" half of MVVM.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    uiState: MainUiState,
    onNewHabitNameChange: (String) -> Unit,
    onAddHabit: () -> Unit,
    onToggleDone: (Habit) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onRefreshQuote: () -> Unit,
    onLogout: () -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onRestoreHabit: (Habit) -> Unit,
) {
    // UI-only state (not business state, so it's fine to live here): the
    // Snackbar host and a scope to show the "Undo" snackbar from.
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Habits") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Logout") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            // --- Daily progress summary ---
            ProgressSummary(habits = uiState.habits)

            Spacer(Modifier.height(12.dp))

            // --- Rubric #2: motivational quote (compact, still 3 states) ---
            QuoteBar(state = uiState.quote, onRefresh = onRefreshQuote)

            Spacer(Modifier.height(12.dp))

            // --- Rubric #4: text input + button ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = uiState.newHabitName,        // state flows DOWN
                    onValueChange = onNewHabitNameChange, // events flow UP
                    label = { Text("New habit") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = onAddHabit) {
                    Text("Add")
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- The habit list (three states) ---
            when {
                // Loading: shown until the database emits its first list.
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                // Empty: a clear message instead of a blank screen.
                uiState.habits.isEmpty() -> {
                    Text("No habits yet. Add your first one above!")
                }
                // Success: the list of habits.
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items = uiState.habits, key = { it.id }) { habit ->
                            SwipeableHabitRow(
                                habit = habit,
                                onClick = { onHabitClick(habit) },
                                onToggleDone = { onToggleDone(habit) },
                                onDelete = {
                                    onDeleteHabit(habit)
                                    // Offer Undo; re-insert if the user taps it.
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted “${habit.name}”",
                                            actionLabel = "Undo",
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            onRestoreHabit(habit)
                                        }
                                    }
                                },
                                // animateItem() smoothly animates this row when
                                // the list reorders, or an item is added/removed.
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Wraps a habit row in a SwipeToDismissBox so the user can swipe it away.
 * Swiping from right→left (EndToStart) confirms the delete; behind the row we
 * reveal a red "Delete" background so the gesture is discoverable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableHabitRow(
    habit: Habit,
    onClick: () -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true // commit the dismissal
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false, // only allow swipe left-to-delete
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    ) {
        HabitRow(
            habit = habit,
            onClick = onClick,
            onToggleDone = onToggleDone,
        )
    }
}

/** One row in the list: tap the card to open details, tap the box to toggle. */
@Composable
private fun HabitRow(
    habit: Habit,
    onClick: () -> Unit,
    onToggleDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A done habit is dimmed; animateColorAsState fades smoothly between the
    // normal and dimmed colours when the user checks/unchecks it.
    val targetColor =
        if (habit.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.onSurface
    val textColor by animateColorAsState(targetValue = targetColor, label = "habitTextColor")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                // Strike through completed habits.
                textDecoration = if (habit.isDone) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f),
            )
            Checkbox(
                checked = habit.isDone,
                onCheckedChange = { onToggleDone() },
            )
        }
    }
}

/**
 * A summary card showing how many habits are done today, with a progress bar.
 * The done count / total are DERIVED from the habit list right here in the UI
 * — they aren't separate state, so they can never get out of sync.
 */
@Composable
private fun ProgressSummary(habits: List<Habit>) {
    val total = habits.size
    val done = habits.count { it.isDone }
    val fraction = if (total == 0) 0f else done.toFloat() / total
    // Smoothly animate the bar as the fraction changes.
    val animatedProgress by animateFloatAsState(targetValue = fraction, label = "progress")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today's progress",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$done of $total habits completed",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * Compact motivational-quote bar. Still renders ALL THREE required states
 * (rubric #2) via an exhaustive `when` over the sealed QuoteUiState — just in
 * a single slim row instead of a large card.
 */
@Composable
private fun QuoteBar(
    state: QuoteUiState,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when (state) {
                // LOADING — small inline spinner
                QuoteUiState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Loading motivation…",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                // SUCCESS — quote + author on one line, truncated if long
                is QuoteUiState.Success -> {
                    Text(
                        text = "“${state.quote.text}” — ${state.quote.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // ERROR — short message
                is QuoteUiState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        TextButton(onClick = onRefresh) { Text("Refresh") }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    FinalProjectTheme {
        MainScreenContent(
            uiState = MainUiState(
                habits = listOf(
                    Habit(id = 1, name = "Drink 2L of water"),
                    Habit(id = 2, name = "Read 10 pages", isDone = true),
                ),
                newHabitName = "Meditate",
                quote = QuoteUiState.Success(
                    Quote(text = "Well done is better than well said.", author = "Benjamin Franklin"),
                ),
            ),
            onNewHabitNameChange = {},
            onAddHabit = {},
            onToggleDone = {},
            onHabitClick = {},
            onRefreshQuote = {},
            onLogout = {},
            onDeleteHabit = {},
            onRestoreHabit = {},
        )
    }
}
