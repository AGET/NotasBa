package com.aget.notesba.di

import com.aget.notesba.data.repository.NoteRepositoryImpl
import com.aget.notesba.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        implementation: NoteRepositoryImpl
    ): NoteRepository
}
