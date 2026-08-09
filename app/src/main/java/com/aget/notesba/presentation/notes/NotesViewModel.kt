package com.aget.notesba.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aget.notesba.data.storage.FileStorage
import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.usecase.DeleteNoteUseCase
import com.aget.notesba.domain.usecase.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val fileStorage: FileStorage
) : ViewModel() {

    val uiState: StateFlow<NotesUiState> =
        getNotesUseCase()
            .map { notes ->
                NotesUiState(
                    notes = notes,
                    isLoading = false,
                    error = null
                )
            }
            .catch { exception ->

                emit(
                    NotesUiState(
                        notes = emptyList(),
                        isLoading = false,
                        error =
                            exception.message
                                ?: "No se pudieron cargar las notas"
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        5_000
                    ),
                initialValue =
                    NotesUiState(
                        isLoading = true
                    )
            )

    fun deleteNote(
        note: Note
    ) {
        viewModelScope.launch {

            try {
                deleteNoteUseCase(note)
                fileStorage.delete(
                    note.attachmentPath
                )

            } catch (exception: Exception) {
            }
        }
    }
}
