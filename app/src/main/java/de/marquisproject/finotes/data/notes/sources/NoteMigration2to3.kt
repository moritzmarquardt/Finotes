package de.marquisproject.finotes.data.notes.sources

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 2 to 3.
 * Adds the `categories` and `category_usage_log` tables.
 */
class NoteMigration2to3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create categories table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `name` TEXT NOT NULL, 
                `color` INTEGER NOT NULL
            )
        """.trimIndent())

        // Create category_usage_log table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `category_usage_log` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `categoryId` INTEGER NOT NULL, 
                `timestamp` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}
