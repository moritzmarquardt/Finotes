package de.marquisproject.finotes.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.marquisproject.finotes.data.notes.repositories.CategoryRepository
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import de.marquisproject.finotes.data.notes.sources.CategoryDAO
import de.marquisproject.finotes.data.notes.sources.NoteDAO
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDAO,
    ): NoteRepository {
        return NoteRepository(noteDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDAO,
    ): CategoryRepository {
        return CategoryRepository(categoryDao)
    }
}
