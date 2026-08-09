package com.aget.notesba.presentation.editor

import com.aget.notesba.domain.model.AttachmentType
import com.aget.notesba.domain.model.DrawingStroke

data class NoteEditorUiState(
    val noteId: Long? = null,
    val text: String = "",
    val attachmentPath: String? = null,
    val attachmentType: AttachmentType? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDrawing: Boolean = false,
    val drawingStrokes: List<DrawingStroke> = emptyList(),
    val error: String? = null
)
