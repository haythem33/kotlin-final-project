package com.example.finalproject.navigation

import kotlinx.serialization.Serializable

/**
 * The set of screens (destinations) the user can navigate to.
 *
 * With type-safe Navigation Compose, each destination is just a normal
 * Kotlin type marked @Serializable. The big win over the old string-route
 * approach ("details/{habitId}") is that the COMPILER checks our navigation:
 * if we forget to pass a habitId, the code won't build. No typos in route
 * strings, no manual argument parsing.
 */

/** The Login screen (rubric #5). No data to carry. */
@Serializable
object LoginRoute

/** The Main Habit List screen. It carries no data, so an object is enough. */
@Serializable
object MainRoute

/**
 * The Habit Details screen. We pass only the habit's id (rubric #1: "data is
 * passed to the detail screen"); the screen then observes that habit LIVE from
 * the database, so its details/state are always current — not a stale snapshot
 * taken at navigation time.
 */
@Serializable
data class DetailsRoute(
    val habitId: Int,
)
