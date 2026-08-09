package com.aget.notesba.domain.usecase

import com.aget.notesba.domain.repository.NoteRepository
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke() = repository.observeNotes()
}
