package com.aget.notesba.presentation.notes

import com.aget.notesba.domain.model.Note

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)