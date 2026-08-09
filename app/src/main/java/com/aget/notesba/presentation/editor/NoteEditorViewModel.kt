package com.aget.notesba.presentation.editor

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aget.notesba.data.storage.DrawingRenderer
import com.aget.notesba.data.storage.FileStorage
import com.aget.notesba.domain.model.AttachmentType
import com.aget.notesba.domain.model.DrawingStroke
import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.usecase.CreateNoteUseCase
import com.aget.notesba.domain.usecase.GetNoteUseCase
import com.aget.notesba.domain.usecase.UpdateNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class NoteEditorViewModel(
    private val noteId: Long?,
    private val getNote: GetNoteUseCase,
    private val createNote: CreateNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val fileStorage: FileStorage,
    private val drawingRenderer: DrawingRenderer
) : ViewModel() {

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

    private fun loadNote(id: Long) {

        viewModelScope.launch {

            val note = getNote(id)

            if (note != null) {

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

            } else {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se encontró la nota"
                    )
                }
            }
        }
    }

    fun onTextChange(text: String) {

        _uiState.update {
            it.copy(text = text)
        }
    }

    fun onImageSelected(uri: Uri) {

        viewModelScope.launch {

            try {

                val path =
                    fileStorage.copyFromUri(
                        uri = uri,
                        extension = "jpg"
                    )

                replaceAttachment(
                    path = path,
                    type = AttachmentType.IMAGE
                )

            } catch (exception: Exception) {

                _uiState.update {
                    it.copy(
                        error =
                            "No se pudo guardar la imagen"
                    )
                }
            }
        }
    }

    fun onFileSelected(uri: Uri) {

        viewModelScope.launch {

            try {

                val path =
                    fileStorage.copyFromUri(
                        uri = uri,
                        extension = "file"
                    )

                replaceAttachment(
                    path = path,
                    type = AttachmentType.FILE
                )

            } catch (exception: Exception) {

                _uiState.update {
                    it.copy(
                        error =
                            "No se pudo guardar el archivo"
                    )
                }
            }
        }
    }

    private suspend fun replaceAttachment(
        path: String,
        type: AttachmentType
    ) {

        val previousPath =
            _uiState.value.attachmentPath

        if (
            previousPath != null &&
            previousPath != path
        ) {
            fileStorage.delete(previousPath)
        }

        _uiState.update {
            it.copy(
                attachmentPath = path,
                attachmentType = type
            )
        }
    }

    fun startDrawing() {

        _uiState.update {
            it.copy(
                isDrawing = true
            )
        }
    }

    fun onDrawingStrokeFinished(stroke: DrawingStroke) {
        _uiState.update { it.copy(drawingStrokes = it.drawingStrokes + stroke, isDrawing = true) }
    }

    private suspend fun createDrawingIfNeeded(): String? {

        val state = _uiState.value

        if (state.drawingStrokes.isEmpty()) {
            return state.attachmentPath
        }

        val bitmap =
            drawingRenderer.render(
                strokes = state.drawingStrokes,
                width = 1080,
                height = 1200
            )

        return fileStorage.saveDrawing(
            bitmap
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun save(
        onSuccess: () -> Unit
    ) {

        val state = _uiState.value

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

                val now = Instant.now()

                // Generar PNG del dibujo
                val attachmentPath =
                    createDrawingIfNeeded()

                val attachmentType =
                    if (state.drawingStrokes.isNotEmpty()) {
                        AttachmentType.DRAWING
                    } else {
                        state.attachmentType
                    }

                // Crear
                if (state.noteId == null) {

                    val note = Note(
                        text = state.text,
                        attachmentPath =
                            attachmentPath,
                        attachmentType =
                            attachmentType,
                        createdAt = now,
                        updatedAt = now
                    )

                    createNote(note)

                } else {

                    // Ediciones
                    val existing =
                        getNote(state.noteId)

                    if (existing != null) {

                        val oldAttachment =
                            existing.attachmentPath

                        val note = existing.copy(
                            text = state.text,
                            attachmentPath =
                                attachmentPath,
                            attachmentType =
                                attachmentType,
                            updatedAt = now
                        )

                        updateNote(note)

                        // Eliminar el archivo anterior
                        // solamente si cambió.
                        if (
                            oldAttachment != null &&
                            oldAttachment != attachmentPath
                        ) {
                            fileStorage.delete(
                                oldAttachment
                            )
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        attachmentPath =
                            attachmentPath,
                        attachmentType =
                            attachmentType
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

    class Factory(
        private val noteId: Long?,
        private val getNote: GetNoteUseCase,
        private val createNote: CreateNoteUseCase,
        private val updateNote: UpdateNoteUseCase,
        private val fileStorage: FileStorage,
        private val drawingRenderer: DrawingRenderer
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {

            if (
                modelClass.isAssignableFrom(
                    NoteEditorViewModel::class.java
                )
            ) {

                return NoteEditorViewModel(
                    noteId = noteId,
                    getNote = getNote,
                    createNote = createNote,
                    updateNote = updateNote,
                    fileStorage = fileStorage,
                    drawingRenderer = drawingRenderer
                ) as T
            }

            throw IllegalArgumentException(
                "ViewModel desconocido"
            )
        }
    }
}
