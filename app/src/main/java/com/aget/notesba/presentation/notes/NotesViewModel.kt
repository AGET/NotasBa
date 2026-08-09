package com.aget.notesba.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aget.notesba.data.storage.FileStorage
import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.usecase.DeleteNoteUseCase
import com.aget.notesba.domain.usecase.GetNotesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    private val getNotes: GetNotesUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val fileStorage: FileStorage
) : ViewModel() {

    val uiState: StateFlow<NotesUiState> =
        getNotes()
            .map { notes ->
                NotesUiState(
                    notes = notes,
                    isLoading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(5_000),
                initialValue = NotesUiState(
                    isLoading = true
                )
            )

    fun delete(note: Note) {

        viewModelScope.launch {

            deleteNote(note)

            fileStorage.delete(
                note.attachmentPath
            )
        }
    }

    class Factory(
        private val getNotes: GetNotesUseCase,
        private val deleteNote: DeleteNoteUseCase,
        private val fileStorage: FileStorage
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {

            if (
                modelClass.isAssignableFrom(
                    NotesViewModel::class.java
                )
            ) {
                return NotesViewModel(
                    getNotes = getNotes,
                    deleteNote = deleteNote,
                    fileStorage = fileStorage
                ) as T
            }

            throw IllegalArgumentException(
                "ViewModel desconocido"
            )
        }
    }
}

