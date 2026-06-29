package com.example.finalproject.data

import com.example.finalproject.data.local.QuoteDao
import com.example.finalproject.data.local.QuoteEntity
import com.example.finalproject.network.QuoteApi

/**
 * Owns the motivational-quote data. It coordinates the TWO sources:
 *   - the network (QuoteApi) — the fresh source
 *   - Room (QuoteDao)        — the local cache
 *
 * The ViewModel calls these and never touches Retrofit or Room directly.
 */
class QuoteRepository(
    private val api: QuoteApi,
    private val dao: QuoteDao,
) {

    /**
     * Fetch a fresh quote from the internet AND cache it locally, then return
     * it. If the network fails this THROWS — the ViewModel decides what to do
     * (show the cached quote or an error). Mapping DTO -> domain Quote happens
     * right here.
     */
    suspend fun fetchAndCacheQuote(): Quote {
        val dto = api.getRandomQuote()
        val quote = Quote(text = dto.quote, author = dto.author)
        dao.upsert(QuoteEntity(text = quote.text, author = quote.author))
        return quote
    }

    /** The last cached quote, or null if we've never successfully fetched one. */
    suspend fun getCachedQuote(): Quote? =
        dao.getQuote()?.let { Quote(text = it.text, author = it.author) }
}
