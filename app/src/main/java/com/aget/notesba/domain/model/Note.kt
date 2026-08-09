package com.aget.notesba.domain.model

import java.time.Instant

enum class AttachmentType {
    IMAGE,
    DRAWING,
    FILE
}

data class Note(
    val id: Long = 0,
    val text: String,
    val attachmentPath: String? = null,
    val attachmentType: AttachmentType? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)
