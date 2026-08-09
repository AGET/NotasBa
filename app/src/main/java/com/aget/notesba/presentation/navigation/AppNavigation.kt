package com.aget.notesba.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aget.notesba.presentation.editor.NoteEditorScreen
import com.aget.notesba.presentation.editor.NoteEditorViewModel
import com.aget.notesba.presentation.notes.NotesScreen
import com.aget.notesba.presentation.notes.NotesViewModel

private object Routes {

    const val NOTES = "notes"

    const val EDITOR = "editor"

    const val EDITOR_WITH_ID = "editor/{noteId}"
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.NOTES
    ) {

        // List of notes
        composable(
            route = Routes.NOTES
        ) {

            NotesRoute(
                onCreateNote = {
                    navController.navigate(
                        Routes.EDITOR
                    )
                },
                onEditNote = { noteId ->

                    navController.navigate(
                        "editor/$noteId"
                    )
                }
            )
        }

        // Make a note
        composable(
            route = Routes.EDITOR
        ) {

            NoteEditorRoute(
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
        ) {

            NoteEditorRoute(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// Routes
@Composable
private fun NotesRoute(
    onCreateNote: () -> Unit,
    onEditNote: (Long) -> Unit
) {

    val viewModel: NotesViewModel =
        hiltViewModel()

    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    NotesScreen(
        state = uiState,

        onCreateNote = onCreateNote,

        onNoteClick = { note ->
            onEditNote(note)
        },

        onDeleteNote = { note ->
            viewModel.deleteNote(note)
        }
    )
}

@Composable
private fun NoteEditorRoute(
    onBack: () -> Unit
) {

    val viewModel: NoteEditorViewModel =
        hiltViewModel()

    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    NoteEditorScreen(
        state = uiState,

        onTextChange = { text ->
            viewModel.onTextChange(text)
        },

        onImageSelected = { uri ->
            viewModel.onImageSelected(uri)
        },

        onFileSelected = { uri ->
            viewModel.onFileSelected(uri)
        },

        onStartDrawing = {
            viewModel.startDrawing()
        },

        onDrawingStrokeFinished = { stroke ->
            viewModel.onDrawingStrokeFinished(stroke)
        },
        onSave = {
            viewModel.save(
                onSuccess = onBack
            )
        },

        onBack = onBack
    )
}
