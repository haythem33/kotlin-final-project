package com.example.finalproject.network

import kotlinx.serialization.Serializable

/**
 * DTO = Data Transfer Object: the EXACT shape of the JSON the API returns.
 *
 * The endpoint https://dummyjson.com/quotes/random responds with:
 *   { "id": 42, "quote": "....", "author": "Kevin Kruse" }
 *
 * @Serializable lets Kotlin Serialization turn that JSON into this object.
 * The property names must match the JSON keys (id, quote, author).
 *
 * We keep this separate from our domain `Quote` model on purpose: if the API
 * ever renames a field, only this file and the mapping change — the rest of
 * the app keeps using the stable `Quote`.
 */
@Serializable
data class QuoteDto(
    val id: Int,
    val quote: String,
    val author: String,
)
