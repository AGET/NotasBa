package com.aget.notesba.presentation.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aget.notesba.domain.model.DrawingStroke
import com.aget.notesba.presentation.editor.components.DrawingCanvas
import com.aget.notesba.presentation.notes.components.AttachmentPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorUiState,
    onTextChange: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onFileSelected: (Uri) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onStartDrawing: () -> Unit,
    onDrawingStrokeFinished: (DrawingStroke) -> Unit
) {

    // Image picker
    val imagePicker =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                onImageSelected(uri)
            }
        }

    // File picker
    val filePicker =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {
                onFileSelected(uri)
            }
        }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text =
                            if (state.noteId == null)
                                "Nueva nota"
                            else
                                "Editar nota"
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Regresar"
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = onSave,
                        enabled = !state.isSaving
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Check,
                            contentDescription =
                                "Guardar"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = state.text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = {
                    Text("Escribe tu nota...")
                },
                shape = RoundedCornerShape(28.dp)
            )

            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                FilledTonalButton(
                    onClick = {
                        imagePicker.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {

                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text("Imagen")
                }

                FilledTonalButton(
                    onClick = onStartDrawing,
                    modifier = Modifier.weight(1f)
                ) {

                    Icon(
                        imageVector = Icons.Default.Draw,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text("Dibujar")
                }
            }

            OutlinedButton(
                onClick = {

                    filePicker.launch(
                        arrayOf("*/*")
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector =
                        Icons.Default.AttachFile,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Adjuntar archivo")
            }

            // Dibujo
            if (state.isDrawing) {

                DrawingCanvas(
                    strokes = state.drawingStrokes,
                    onStrokeFinished =
                        onDrawingStrokeFinished
                )
            }

            // Adjunto
            if (
                state.attachmentPath != null
            ) {

                AttachmentPreview(
                    path =
                        state.attachmentPath,
                    type =
                        state.attachmentType
                )
            }

            // Errores
            if (state.error != null) {

                Text(
                    text = state.error
                )
            }
        }
    }
}
