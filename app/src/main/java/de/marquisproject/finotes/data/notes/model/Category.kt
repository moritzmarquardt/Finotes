package de.marquisproject.finotes.data.notes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a user-defined category.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int // ARGB color
)

/**
 * Logs each time a category is used as a filter.
 * This powers the dynamic sorting based on usage in a specific timeframe.
 */
@Entity(tableName = "category_usage_log")
data class CategoryUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
