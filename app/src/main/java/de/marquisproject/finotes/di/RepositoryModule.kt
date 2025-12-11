package de.marquisproject.finotes.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import de.marquisproject.finotes.data.notes.sources.NoteDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDb: NoteDatabase,
    ): NoteRepository {
        return NoteRepository(noteDb)
    }
}