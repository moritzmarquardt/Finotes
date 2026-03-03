package de.marquisproject.finotes.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.marquisproject.finotes.data.notes.sources.NoteDAO
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
            .addMigrations(NoteDatabase.MIGRATION_1_2(app))
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(db: NoteDatabase): NoteDAO {
        return db.dao
    }
}