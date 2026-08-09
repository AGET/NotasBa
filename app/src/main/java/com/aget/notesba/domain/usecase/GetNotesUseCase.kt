package com.aget.notesba.domain.usecase

import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return repository.observeNotes()
    }
}
