package com.example.finalproject.data

/**
 * The app's own model for a motivational quote — independent of both the
 * network JSON (QuoteDto) and the database row (QuoteEntity). The UI only
 * ever sees this clean type.
 */
data class Quote(
    val text: String,
    val author: String,
)
