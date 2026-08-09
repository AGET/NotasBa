package com.aget.notesba.domain.usecase

import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class NoteUseCasesTest {

    private val sampleNote = Note(
        id = 10L,
        text = "Nota de prueba",
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z")
    )

    @Test
    fun createNoteUseCase_returnsCreatedIdAndDelegatesToRepository() = runBlocking {
        val repository = FakeNoteRepository(
            createdId = 42L
        )
        val useCase = CreateNoteUseCase(repository)

        val result = useCase(sampleNote)

        assertEquals(42L, result)
        assertEquals(sampleNote, repository.createdNote)
    }

    @Test
    fun updateNoteUseCase_delegatesToRepository() = runBlocking {
        val repository = FakeNoteRepository()
        val useCase = UpdateNoteUseCase(repository)

        useCase(sampleNote)

        assertEquals(sampleNote, repository.updatedNote)
    }

    @Test
    fun deleteNoteUseCase_delegatesToRepository() = runBlocking {
        val repository = FakeNoteRepository()
        val useCase = DeleteNoteUseCase(repository)

        useCase(sampleNote)

        assertEquals(sampleNote, repository.deletedNote)
    }

    @Test
    fun getNoteUseCase_returnsRepositoryResult() = runBlocking {
        val repository = FakeNoteRepository(
            noteToReturn = sampleNote
        )
        val useCase = GetNoteUseCase(repository)

        val result = useCase(10L)

        assertEquals(10L, repository.requestedId)
        assertEquals(sampleNote, result)
    }

    @Test
    fun getNoteUseCase_returnsNullWhenRepositoryHasNoNote() = runBlocking {
        val repository = FakeNoteRepository(
            noteToReturn = null
        )
        val useCase = GetNoteUseCase(repository)

        val result = useCase(99L)

        assertEquals(99L, repository.requestedId)
        assertNull(result)
    }

    @Test
    fun getNotesUseCase_returnsFlowFromRepository() = runBlocking {
        val notes = listOf(sampleNote)
        val repository = FakeNoteRepository(
            notesFlow = flowOf(notes)
        )
        val useCase = GetNotesUseCase(repository)

        val result = useCase().first()

        assertEquals(notes, result)
    }

    private class FakeNoteRepository(
        private val createdId: Long = 1L,
        private val noteToReturn: Note? = null,
        private val notesFlow: Flow<List<Note>> = flowOf(emptyList())
    ) : NoteRepository {

        var createdNote: Note? = null
        var updatedNote: Note? = null
        var deletedNote: Note? = null
        var requestedId: Long? = null

        override fun observeNotes(): Flow<List<Note>> = notesFlow

        override suspend fun getNote(id: Long): Note? {
            requestedId = id
            return noteToReturn
        }

        override suspend fun create(note: Note): Long {
            createdNote = note
            return createdId
        }

        override suspend fun update(note: Note) {
            updatedNote = note
        }

        override suspend fun delete(note: Note) {
            deletedNote = note
        }
    }
}
