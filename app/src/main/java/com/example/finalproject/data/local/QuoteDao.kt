package com.example.finalproject.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Database operations for the single cached quote.
 *
 * upsert uses OnConflictStrategy.REPLACE: inserting a quote with id = 0 when
 * row 0 already exists overwrites it. So we always keep exactly one — the
 * most recent quote.
 */
@Dao
interface QuoteDao {

    @Query("SELECT * FROM cached_quote WHERE id = 0")
    suspend fun getQuote(): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(quote: QuoteEntity)
}
