package de.marquisproject.finotes.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.marquisproject.finotes.data.notes.sources.CategoryDAO
import de.marquisproject.finotes.data.notes.sources.NoteDAO
import de.marquisproject.finotes.data.notes.sources.NoteDatabase
import javax.inject.Singleton
import android.util.Log

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
            .addMigrations(
                NoteDatabase.getMigration1to2(app.applicationContext),
                NoteDatabase.getMigration2to3()
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // This is called after the database is opened and migrations are complete.
                    // It is now safe to delete the old files because the transaction has committed.
                    val binFile = app.getDatabasePath("bin.db")
                    if (binFile.exists()) {
                        val deleted = app.deleteDatabase("bin.db")
                        Log.d("DatabaseModule", "Cleanup: bin.db deleted = $deleted")
                    }

                    val archiveFile = app.getDatabasePath("archive.db")
                    if (archiveFile.exists()) {
                        val deleted = app.deleteDatabase("archive.db")
                        Log.d("DatabaseModule", "Cleanup: archive.db deleted = $deleted")
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(db: NoteDatabase): NoteDAO {
        return db.dao
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: NoteDatabase): CategoryDAO {
        return db.categoryDao
    }
}
