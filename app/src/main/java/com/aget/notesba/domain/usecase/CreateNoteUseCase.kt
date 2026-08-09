package com.aget.notesba.domain.usecase

import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.repository.NoteRepository

class CreateNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long {
        return repository.create(note)
    }
}
