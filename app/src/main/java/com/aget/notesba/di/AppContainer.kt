package com.aget.notesba.di

import android.content.Context
import androidx.room.Room
import com.aget.notesba.data.local.AppDatabase
import com.aget.notesba.data.repository.NoteRepositoryImpl
import com.aget.notesba.data.storage.DrawingRenderer
import com.aget.notesba.data.storage.FileStorage
import com.aget.notesba.domain.usecase.CreateNoteUseCase
import com.aget.notesba.domain.usecase.DeleteNoteUseCase
import com.aget.notesba.domain.usecase.GetNoteUseCase
import com.aget.notesba.domain.usecase.GetNotesUseCase
import com.aget.notesba.domain.usecase.UpdateNoteUseCase

class AppContainer(
    context: Context
) {

    private val database =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notes_ba.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    private val noteDao =
        database.noteDao()

    private val noteRepository =
        NoteRepositoryImpl(noteDao)

    val fileStorage =
        FileStorage(context)

    val drawingRenderer =
        DrawingRenderer()

    val createNote =
        CreateNoteUseCase(noteRepository)

    val updateNote =
        UpdateNoteUseCase(noteRepository)

    val deleteNote =
        DeleteNoteUseCase(noteRepository)

    val getNotes =
        GetNotesUseCase(noteRepository)

    val getNote =
        GetNoteUseCase(noteRepository)
}
