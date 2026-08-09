package com.aget.notesba.data.repository

import com.aget.notesba.data.local.NoteDao
import com.aget.notesba.data.local.NoteEntity
import com.aget.notesba.domain.model.AttachmentType
import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeNotes().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getNote(id: Long): Note? =
        dao.getNote(id)?.toDomain()

    override suspend fun create(note: Note): Long =
        dao.insert(note.toEntity())

    override suspend fun update(note: Note) {
        dao.update(note.toEntity())
    }

    override suspend fun delete(note: Note) {
        dao.delete(note.toEntity())
    }

    private fun NoteEntity.toDomain() =
        Note(
            id = id,
            text = text,
            attachmentPath = attachmentPath,
            attachmentType = attachmentType?.let {
                AttachmentType.valueOf(it)
            },
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )

    private fun Note.toEntity() =
        NoteEntity(
            id = id,
            text = text,
            attachmentPath = attachmentPath,
            attachmentType = attachmentType?.name,
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli()
        )
}
