package de.marquisproject.finotes.data.notes.sources

import androidx.room.*
import de.marquisproject.finotes.data.notes.model.Category
import de.marquisproject.finotes.data.notes.model.CategoryUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Insert
    suspend fun logUsage(usage: CategoryUsage)

    /**
     * Fetches categories sorted by relevance.
     * Relevance is determined by how many times a category was used as a filter 
     * within the specified [cutoff] timestamp.
     */
    @Query("""
        SELECT c.* FROM categories c
        LEFT JOIN (
            SELECT categoryId, COUNT(*) as usageCount 
            FROM category_usage_log 
            WHERE timestamp > :cutoff 
            GROUP BY categoryId
        ) u ON c.id = u.categoryId
        ORDER BY u.usageCount DESC, c.name ASC
    """)
    fun getCategoriesByRelevance(cutoff: Long): Flow<List<Category>>
    
    /**
     * Optional: Cleanup old logs to keep the DB small.
     */
    @Query("DELETE FROM category_usage_log WHERE timestamp < :cutoff")
    suspend fun deleteOldUsageLogs(cutoff: Long)
}
