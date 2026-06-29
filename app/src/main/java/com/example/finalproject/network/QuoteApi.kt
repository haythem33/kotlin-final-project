package com.example.finalproject.network

import retrofit2.http.GET

/**
 * The HTTP API, described as a Kotlin interface. Retrofit generates the real
 * networking code from these annotations at build time.
 *
 * @GET("quotes/random") = perform an HTTP GET on baseUrl + "quotes/random".
 * The function is `suspend`, so callers must run it in a coroutine and the
 * network call never blocks the main thread. Retrofit deserializes the JSON
 * response into a QuoteDto for us.
 */
interface QuoteApi {

    @GET("quotes/random")
    suspend fun getRandomQuote(): QuoteDto
}
