package com.aget.notesba.di

import android.content.Context
import com.aget.notesba.data.storage.DrawingRenderer
import com.aget.notesba.data.storage.FileStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideFileStorage(
        @ApplicationContext context: Context
    ): FileStorage {
        return FileStorage(context)
    }

    @Provides
    @Singleton
    fun provideDrawingRenderer(): DrawingRenderer {
        return DrawingRenderer()
    }
}
