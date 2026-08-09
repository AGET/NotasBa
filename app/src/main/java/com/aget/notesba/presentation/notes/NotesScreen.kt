package com.aget.notesba.presentation.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aget.notesba.domain.model.Note
import com.aget.notesba.presentation.notes.components.NoteCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    state: NotesUiState,
    onCreateNote: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis notas")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear nota"
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            items(
                items = state.notes,
                key = { it.id }
            ) { note ->

                NoteCard(
                    note = note,
                    onClick = {
                        onNoteClick(note.id)
                    },
                    onDelete = {
                        onDeleteNote(note)
                    }
                )
            }
        }
    }
}

