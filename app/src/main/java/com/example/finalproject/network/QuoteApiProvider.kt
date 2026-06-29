package com.example.finalproject.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Builds the single Retrofit-backed QuoteApi instance.
 *
 * - baseUrl: every @GET path is appended to this.
 * - Json { ignoreUnknownKeys = true }: if the API adds fields we don't model,
 *   we ignore them instead of crashing — defensive against API changes.
 * - asConverterFactory tells Retrofit to use Kotlin Serialization to turn the
 *   JSON body into our @Serializable DTOs.
 */
object QuoteApiProvider {

    private const val BASE_URL = "https://dummyjson.com/"

    private val json = Json { ignoreUnknownKeys = true }

    fun create(): QuoteApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(QuoteApi::class.java)
    }
}
