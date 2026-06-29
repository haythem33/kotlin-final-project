package com.example.finalproject

import android.app.Application
import com.example.finalproject.data.AuthRepository
import com.example.finalproject.data.HabitRepository
import com.example.finalproject.data.QuoteRepository
import com.example.finalproject.data.local.HabitDatabase
import com.example.finalproject.network.QuoteApiProvider

/**
 * A custom Application class — created once when the app process starts and
 * living for the app's whole lifetime. We use it as a tiny, manual dependency
 * container: it builds the database and repository a single time and shares
 * them.
 *
 * `by lazy` means each object is only created the first time it's actually
 * needed, then reused. (Bigger apps use a DI library like Hilt for this; for
 * a student project this manual approach is clearer and easier to defend.)
 *
 * Remember: this only works because it's registered in AndroidManifest.xml
 * with android:name=".HabitApplication".
 */
class HabitApplication : Application() {
    private val database by lazy { HabitDatabase.getInstance(this) }

    val habitRepository by lazy { HabitRepository(database.habitDao()) }

    private val quoteApi by lazy { QuoteApiProvider.create() }
    val quoteRepository by lazy { QuoteRepository(quoteApi, database.quoteDao()) }

    val authRepository by lazy { AuthRepository() }
}
