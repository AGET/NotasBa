package com.aget.notesba.presentation.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.aget.notesba.data.storage.DrawingRenderer
import com.aget.notesba.data.storage.FileStorage
import com.aget.notesba.domain.model.AttachmentType
import com.aget.notesba.domain.model.Note
import com.aget.notesba.domain.repository.NoteRepository
import com.aget.notesba.domain.usecase.CreateNoteUseCase
import com.aget.notesba.domain.usecase.GetNoteUseCase
import com.aget.notesba.domain.usecase.UpdateNoteUseCase
import com.aget.notesba.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_withNoteId_loadsExistingNote() = runTest {
        val existing = sampleNote(
            id = 5L,
            text = "Nota existente",
            attachmentPath = "/tmp/old.jpg",
            attachmentType = AttachmentType.IMAGE
        )
        val repository = FakeNoteRepository(
            notesById = mutableMapOf(5L to existing)
        )

        val viewModel = createViewModel(
            repository = repository,
            noteId = 5L
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(5L, state.noteId)
        assertEquals("Nota existente", state.text)
        assertEquals("/tmp/old.jpg", state.attachmentPath)
        assertEquals(AttachmentType.IMAGE, state.attachmentType)
        assertNull(state.error)
    }

    @Test
    fun save_whenNoteIsEmpty_setsValidationError() = runTest {
        val repository = FakeNoteRepository()
        val viewModel = createViewModel(repository)
        var onSuccessCalled = false

        viewModel.save {
            onSuccessCalled = true
        }

        assertEquals("La nota no puede estar vacía", viewModel.uiState.value.error)
        assertFalse(onSuccessCalled)
        assertNull(repository.createdNote)
    }

    @Test
    fun save_newNote_createsNoteAndCallsSuccess() = runTest {
        val repository = FakeNoteRepository()
        val viewModel = createViewModel(repository)
        var onSuccessCalled = false

        viewModel.onTextChange("Nueva nota")
        viewModel.save {
            onSuccessCalled = true
        }
        advanceUntilIdle()

        assertTrue(onSuccessCalled)
        assertNotNull(repository.createdNote)
        assertEquals("Nueva nota", repository.createdNote?.text)
        assertFalse(viewModel.uiState.value.isSaving)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun save_existingNoteWithNewImage_updatesNoteAndDeletesOldAttachment() = runTest {
        val existing = sampleNote(
            id = 9L,
            text = "Antes",
            attachmentPath = "/tmp/old.jpg",
            attachmentType = AttachmentType.IMAGE
        )
        val repository = FakeNoteRepository(
            notesById = mutableMapOf(9L to existing)
        )
        val fileStorage = mockk<FileStorage>(relaxed = true)
        val drawingRenderer = mockk<DrawingRenderer>(relaxed = true)
        val selectedUri = mockk<Uri>()
        var onSuccessCalled = false

        every { fileStorage.copyFromUri(selectedUri, "jpg") } returns "/tmp/new.jpg"

        val viewModel = createViewModel(
            repository = repository,
            noteId = 9L,
            fileStorage = fileStorage,
            drawingRenderer = drawingRenderer
        )
        advanceUntilIdle()

        viewModel.onImageSelected(selectedUri)
        advanceUntilIdle()
        viewModel.onTextChange("Después")
        viewModel.save {
            onSuccessCalled = true
        }
        advanceUntilIdle()

        assertTrue(onSuccessCalled)
        assertNotNull(repository.updatedNote)
        assertEquals("Después", repository.updatedNote?.text)
        assertEquals("/tmp/new.jpg", repository.updatedNote?.attachmentPath)
        assertEquals(AttachmentType.IMAGE, repository.updatedNote?.attachmentType)
        verify(exactly = 1) { fileStorage.delete("/tmp/old.jpg") }
    }

    private fun createViewModel(
        repository: FakeNoteRepository,
        noteId: Long? = null,
        fileStorage: FileStorage = mockk(relaxed = true),
        drawingRenderer: DrawingRenderer = mockk(relaxed = true)
    ): NoteEditorViewModel {
        val savedStateHandle = SavedStateHandle()
        if (noteId != null) {
            savedStateHandle["noteId"] = noteId
        }
        return NoteEditorViewModel(
            getNoteUseCase = GetNoteUseCase(repository),
            createNoteUseCase = CreateNoteUseCase(repository),
            updateNoteUseCase = UpdateNoteUseCase(repository),
            fileStorage = fileStorage,
            drawingRenderer = drawingRenderer,
            savedStateHandle = savedStateHandle
        )
    }

    private fun sampleNote(
        id: Long,
        text: String,
        attachmentPath: String? = null,
        attachmentType: AttachmentType? = null
    ): Note = Note(
        id = id,
        text = text,
        attachmentPath = attachmentPath,
        attachmentType = attachmentType,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z")
    )

    private class FakeNoteRepository(
        private val notesById: MutableMap<Long, Note> = mutableMapOf()
    ) : NoteRepository {
        var createdNote: Note? = null
        var updatedNote: Note? = null

        override fun observeNotes(): Flow<List<Note>> =
            flowOf(notesById.values.toList())

        override suspend fun getNote(id: Long): Note? =
            notesById[id]

        override suspend fun create(note: Note): Long {
            createdNote = note
            return 1L
        }

        override suspend fun update(note: Note) {
            updatedNote = note
            notesById[note.id] = note
        }

        override suspend fun delete(note: Note) = Unit
    }
}
