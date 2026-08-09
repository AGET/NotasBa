package com.aget.notesba.di

import android.content.Context
import androidx.room.Room
import com.aget.notesba.data.local.AppDatabase
import com.aget.notesba.data.local.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "personal_notes.db"
        ).build()
    }

    @Provides
    fun provideNoteDao(
        database: AppDatabase
    ): NoteDao {
        return database.noteDao()
    }
}
