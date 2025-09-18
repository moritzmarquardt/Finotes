package de.marquisproject.finotes.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.marquisproject.finotes.data.notes.sources.ArchiveDatabase
import de.marquisproject.finotes.data.notes.sources.BinDatabase
import de.marquisproject.finotes.data.notes.sources.NoteDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Live as long as the application
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(app: Application): NoteDatabase {
        return Room.databaseBuilder(
            app.applicationContext,
            NoteDatabase::class.java,
            "note.db"
        )
            .addMigrations(NoteDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideArchiveDatabase(app: Application): ArchiveDatabase {
        return Room.databaseBuilder(
            app.applicationContext,
            ArchiveDatabase::class.java,
            "archive.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBinDatabase(app: Application): BinDatabase {
        return Room.databaseBuilder(
            app.applicationContext,
            BinDatabase::class.java,
            "bin.db"
        ).build()
    }
}