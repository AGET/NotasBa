package com.aget.notesba.domain.repository

import com.aget.notesba.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun observeNotes(): Flow<List<Note>>

    suspend fun getNote(id: Long): Note?

    suspend fun create(note: Note): Long

    suspend fun update(note: Note)

    suspend fun delete(note: Note)
}
