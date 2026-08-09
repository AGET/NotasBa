package com.aget.notesba.domain.usecase

import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.repository.NoteRepository

class DeleteNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.delete(note)
    }
}
