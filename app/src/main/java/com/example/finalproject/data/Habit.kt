package com.example.finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The domain model for a habit AND its Room database row.
 *
 * @Entity(tableName = "habits") tells Room: "store these in a table called
 * habits". Each property becomes a column.
 *
 * @PrimaryKey(autoGenerate = true) makes `id` the unique row id and lets the
 * database assign it automatically. That's why id defaults to 0: when we
 * insert a NEW habit we pass id = 0, and Room replaces it with the next real
 * id. (For a student project it's fine to use the entity directly as the
 * domain model; larger apps sometimes keep them separate.)
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isDone: Boolean = false,
    // The uid of the Firebase user who owns this habit. Queries filter on it
    // so each signed-in user sees only their own habits (rubric extra).
    val ownerId: String = "",
)
