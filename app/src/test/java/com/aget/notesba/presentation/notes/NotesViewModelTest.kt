package com.aget.notesba.presentation.notes

import com.aget.notesba.data.storage.FileStorage
import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.repository.NoteRepository
import com.aget.notesba.domain.usecase.DeleteNoteUseCase
import com.aget.notesba.domain.usecase.GetNotesUseCase
import com.aget.notesba.testing.MainDispatcherRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_emitsNotesFromUseCase() = runTest {
        val repository = FakeNoteRepository()
        val fileStorage = mockk<FileStorage>(relaxed = true)
        val viewModel = NotesViewModel(
            getNotesUseCase = GetNotesUseCase(repository),
            deleteNoteUseCase = DeleteNoteUseCase(repository),
            fileStorage = fileStorage
        )
        val note = sampleNote(id = 1L, text = "Nota 1")

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        repository.notesFlow.value = listOf(note)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(note), viewModel.uiState.value.notes)
        assertNull(viewModel.uiState.value.error)

        collectJob.cancel()
    }

    @Test
    fun deleteNote_deletesNoteAndAttachment() = runTest {
        val repository = FakeNoteRepository()
        val fileStorage = mockk<FileStorage>(relaxed = true)
        val viewModel = NotesViewModel(
            getNotesUseCase = GetNotesUseCase(repository),
            deleteNoteUseCase = DeleteNoteUseCase(repository),
            fileStorage = fileStorage
        )
        val note = sampleNote(
            id = 7L,
            text = "Con adjunto",
            attachmentPath = "/tmp/attachment.jpg"
        )

        viewModel.deleteNote(note)
        advanceUntilIdle()

        assertEquals(note, repository.deletedNote)
        verify(exactly = 1) { fileStorage.delete("/tmp/attachment.jpg") }
    }

    private fun sampleNote(
        id: Long,
        text: String,
        attachmentPath: String? = null
    ): Note = Note(
        id = id,
        text = text,
        attachmentPath = attachmentPath,
        attachmentType = null,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z")
    )

    private class FakeNoteRepository : NoteRepository {
        val notesFlow = MutableStateFlow<List<Note>>(emptyList())
        var deletedNote: Note? = null

        override fun observeNotes(): Flow<List<Note>> = notesFlow

        override suspend fun getNote(id: Long): Note? = null

        override suspend fun create(note: Note): Long = 0L

        override suspend fun update(note: Note) = Unit

        override suspend fun delete(note: Note) {
            deletedNote = note
        }
    }
}
