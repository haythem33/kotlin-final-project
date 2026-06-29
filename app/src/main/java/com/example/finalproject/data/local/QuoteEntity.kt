package com.example.finalproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The cached copy of the last quote we fetched from the internet.
 *
 * We only ever keep ONE quote, so the primary key is hard-coded to 0. Each
 * new fetch REPLACES row 0 (see QuoteDao.upsert). This is what lets the app
 * show a quote on launch without a fresh network call (rubric #3).
 */
@Entity(tableName = "cached_quote")
data class QuoteEntity(
    @PrimaryKey val id: Int = 0,
    val text: String,
    val author: String,
)
