package de.marquisproject.finotes.data.notes.repositories

import de.marquisproject.finotes.data.notes.model.Category
import de.marquisproject.finotes.data.notes.model.CategoryUsage
import de.marquisproject.finotes.data.notes.sources.CategoryDAO
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDAO
) {
    /**
     * Returns categories sorted by relevance (usage in the last 30 days).
     */
    fun getCategoriesSortedByRelevance(): Flow<List<Category>> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return categoryDao.getCategoriesByRelevance(thirtyDaysAgo)
    }

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)

    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    /**
     * Call this whenever a category chip is selected to filter notes.
     */
    suspend fun logCategoryUsage(categoryId: Long) {
        categoryDao.logUsage(CategoryUsage(categoryId = categoryId))
        
        // Bonus: Clean up very old logs (e.g., older than 60 days) to keep DB healthy
        val sixtyDaysAgo = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000)
        categoryDao.deleteOldUsageLogs(sixtyDaysAgo)
    }
}
