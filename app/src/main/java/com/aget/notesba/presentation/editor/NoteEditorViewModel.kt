package com.aget.notesba.presentation.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aget.notesba.data.storage.DrawingRenderer
import com.aget.notesba.data.storage.FileStorage
import com.aget.notesba.domain.model.AttachmentType
import com.aget.notesba.domain.model.DrawingStroke
import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.usecase.CreateNoteUseCase
import com.aget.notesba.domain.usecase.GetNoteUseCase
import com.aget.notesba.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val getNoteUseCase: GetNoteUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val fileStorage: FileStorage,
    private val drawingRenderer: DrawingRenderer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long? =
        savedStateHandle.get<Long>("noteId")

    private val _uiState =
        MutableStateFlow(
            NoteEditorUiState(
                noteId = noteId,
                isLoading = noteId != null
            )
        )

    val uiState: StateFlow<NoteEditorUiState> =
        _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(
        id: Long
    ) {
        viewModelScope.launch {

            try {

                val note =
                    getNoteUseCase(id)

                if (note == null) {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error =
                                "No se encontró la nota"
                        )
                    }

                    return@launch
                }

                _uiState.update {
                    it.copy(
                        noteId = note.id,
                        text = note.text,
                        attachmentPath =
                            note.attachmentPath,
                        attachmentType =
                            note.attachmentType,
                        isLoading = false
                    )
                }

            } catch (exception: Exception) {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error =
                            exception.message
                                ?: "No se pudo cargar la nota"
                    )
                }
            }
        }
    }

    fun onTextChange(
        text: String
    ) {
        _uiState.update {
            it.copy(
                text = text,
                error = null
            )
        }
    }

    fun onImageSelected(
        uri: Uri,
        extension: String = "jpg"
    ) {
        viewModelScope.launch {

            try {

                val path =
                    fileStorage.copyFromUri(
                        uri = uri,
                        extension = extension
                    )

                replaceAttachment(
                    path = path,
                    type = AttachmentType.IMAGE
                )

            } catch (exception: Exception) {

                _uiState.update {
                    it.copy(
                        error =
                            exception.message
                                ?: "No se pudo guardar la imagen"
                    )
                }
            }
        }
    }

    fun onFileSelected(
        uri: Uri,
        extension: String = "jpg"
    ) {
        viewModelScope.launch {

            try {

                val path =
                    fileStorage.copyFromUri(
                        uri = uri,
                        extension = extension
                    )

                replaceAttachment(
                    path = path,
                    type = AttachmentType.FILE
                )

            } catch (exception: Exception) {

                _uiState.update {
                    it.copy(
                        error =
                            exception.message
                                ?: "No se pudo guardar el archivo"
                    )
                }
            }
        }
    }

    private fun replaceAttachment(
        path: String,
        type: AttachmentType
    ) {
        _uiState.update {
            it.copy(
                attachmentPath = path,
                attachmentType = type,
                drawingStrokes = emptyList(),
                isDrawing = false,
                error = null
            )
        }
    }

    fun startDrawing() {
        _uiState.update {
            it.copy(
                isDrawing = true,
                error = null
            )
        }
    }

    fun onDrawingStrokeFinished(
        stroke: DrawingStroke
    ) {
        _uiState.update {
            it.copy(
                drawingStrokes =
                    it.drawingStrokes + stroke,
                isDrawing = true,
                error = null
            )
        }
    }

    fun save(
        onSuccess: () -> Unit
    ) {
        val state =
            _uiState.value

        if (
            state.text.isBlank() &&
            state.attachmentPath == null &&
            state.drawingStrokes.isEmpty()
        ) {

            _uiState.update {
                it.copy(
                    error =
                        "La nota no puede estar vacía"
                )
            }

            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isSaving = true,
                    error = null
                )
            }

            try {

                val oldNote =
                    state.noteId?.let {
                        getNoteUseCase(it)
                    }

           // Prioridad al dibujo
                val attachmentPath =
                    if (
                        state.drawingStrokes.isNotEmpty()
                    ) {
                        createDrawing()
                    } else {
                        state.attachmentPath
                    }

                val attachmentType =
                    if (
                        state.drawingStrokes.isNotEmpty()
                    ) {
                        AttachmentType.DRAWING
                    } else {
                        state.attachmentType
                    }

                val now =
                    Instant.now()

                if (state.noteId == null) {

                    val note =
                        Note(
                            text = state.text,
                            attachmentPath =
                                attachmentPath,
                            attachmentType =
                                attachmentType,
                            createdAt = now,
                            updatedAt = now
                        )

                    createNoteUseCase(note)

                } else {

                    if (oldNote == null) {

                        throw IllegalStateException(
                            "La nota ya no existe"
                        )
                    }

                    val updatedNote =
                        oldNote.copy(
                            text = state.text,
                            attachmentPath =
                                attachmentPath,
                            attachmentType =
                                attachmentType,
                            updatedAt = now
                        )

                    updateNoteUseCase(
                        updatedNote
                    )

                    if (
                        oldNote.attachmentPath != null &&
                        oldNote.attachmentPath != attachmentPath
                    ) {
                        fileStorage.delete(
                            oldNote.attachmentPath
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        attachmentPath =
                            attachmentPath,
                        attachmentType =
                            attachmentType,
                        isDrawing = false
                    )
                }

                onSuccess()

            } catch (exception: Exception) {

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error =
                            exception.message
                                ?: "No se pudo guardar la nota"
                    )
                }
            }
        }
    }

    private fun createDrawing(): String {

        val strokes =
            _uiState.value.drawingStrokes

        val bitmap =
            drawingRenderer.render(
                strokes = strokes,
                width = 1080,
                height = 1200
            )

        return fileStorage.saveDrawing(
            bitmap
        )
    }
}
