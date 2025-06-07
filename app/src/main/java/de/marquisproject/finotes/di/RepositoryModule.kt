package de.marquisproject.finotes.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import de.marquisproject.finotes.data.notes.sources.ArchiveDatabase
import de.marquisproject.finotes.data.notes.sources.BinDatabase
import de.marquisproject.finotes.data.notes.sources.NoteDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDb: NoteDatabase,
        archiveDb: ArchiveDatabase,
        binDb: BinDatabase
    ): NoteRepository {
        return NoteRepository(noteDb, archiveDb, binDb)
    }
}