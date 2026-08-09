package com.aget.notesba.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aget.notesba.NotesBaApplication
import com.aget.notesba.presentation.editor.NoteEditorScreen
import com.aget.notesba.presentation.editor.NoteEditorViewModel
import com.aget.notesba.presentation.notes.NotesScreen
import com.aget.notesba.presentation.notes.NotesViewModel

private object Routes {
    const val NOTES = "notes"
    const val EDITOR = "editor"
    const val EDITOR_WITH_ID = "editor/{noteId}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.NOTES
    ) {

        // Listado de las notas
        composable(
            route = Routes.NOTES
        ) {
            NotesRoute(
                onCreateNote = {
                    navController.navigate(Routes.EDITOR)
                },
                onEditNote = { noteId ->
                    navController.navigate(
                        "editor/$noteId"
                    )
                }
            )
        }

        // New note
        composable(
            route = Routes.EDITOR
        ) {
            NoteEditorRoute(
                noteId = null,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit note
        composable(
            route = Routes.EDITOR_WITH_ID,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            val noteId =
                backStackEntry.arguments
                    ?.getLong("noteId")

            NoteEditorRoute(
                noteId = noteId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}


// Rutes
@Composable
private fun NotesRoute(
    onCreateNote: () -> Unit,
    onEditNote: (Long) -> Unit
) {
    val context = LocalContext.current

    val application =
        context.applicationContext as NotesBaApplication

    val viewModel: NotesViewModel = viewModel(
        factory = NotesViewModel.Factory(
            getNotes = application.container.getNotes,
            deleteNote = application.container.deleteNote,
            fileStorage = application.container.fileStorage
        )
    )

    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    NotesScreen(
        state = uiState,
        onCreateNote = onCreateNote,
        onNoteClick = onEditNote,
        onDeleteNote = viewModel::delete
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun NoteEditorRoute(
    noteId: Long?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val application =
        context.applicationContext as NotesBaApplication

    val viewModel: NoteEditorViewModel = viewModel(
        key = "note-editor-${noteId ?: "new"}",
        factory = NoteEditorViewModel.Factory(
            noteId = noteId,
            getNote = application.container.getNote,
            createNote = application.container.createNote,
            updateNote = application.container.updateNote,
            fileStorage = application.container.fileStorage,
            drawingRenderer = application.container.drawingRenderer
        )
    )

    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    NoteEditorScreen(
        state = uiState,
        onTextChange = viewModel::onTextChange,
        onImageSelected = viewModel::onImageSelected,
        onFileSelected = viewModel::onFileSelected,
        onSave = {
            viewModel.save(
                onSuccess = onBack
            )
        },
        onBack = onBack,
        onStartDrawing = viewModel::startDrawing,
        onDrawingStrokeFinished =
            viewModel::onDrawingStrokeFinished
    )
}
