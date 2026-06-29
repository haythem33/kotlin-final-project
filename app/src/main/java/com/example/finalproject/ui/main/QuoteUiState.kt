package com.example.finalproject.ui.main

import com.example.finalproject.data.Quote

/**
 * The three states the quote section can be in (rubric #2). A `sealed
 * interface` means the set is CLOSED — the only possibilities are the ones
 * listed here. That lets the UI use an exhaustive `when` with no `else`, so
 * if we ever add a state the compiler forces us to handle it.
 *
 *  - Loading: request in flight -> show a spinner
 *  - Success: we have a quote    -> show it
 *  - Error:   request failed and no cache -> show a message
 */
sealed interface QuoteUiState {
    data object Loading : QuoteUiState
    data class Success(val quote: Quote) : QuoteUiState
    data class Error(val message: String) : QuoteUiState
}
