package com.aget.notesba.presentation.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.aget.notesba.domain.model.DrawingPoint
import com.aget.notesba.domain.model.DrawingStroke

@Composable
fun DrawingCanvas(
    strokes: List<DrawingStroke>,
    onStrokeFinished: (DrawingStroke) -> Unit
) {
    var currentStroke by remember {
        mutableStateOf<List<Offset>>(emptyList())
    }

    val strokeColor =
        MaterialTheme.colorScheme.primary

    val backgroundColor =
        MaterialTheme.colorScheme.surfaceContainer

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(
                RoundedCornerShape(28.dp)
            )
            .background(backgroundColor)
            .pointerInput(Unit) {

                detectDragGestures(

                    onDragStart = { offset ->

                        currentStroke =
                            listOf(offset)
                    },

                    onDrag = { change, _ ->

                        currentStroke =
                            currentStroke +
                                    change.position
                    },

                    onDragEnd = {

                        if (currentStroke.isNotEmpty()) {

                            val stroke =
                                DrawingStroke(
                                    points =
                                        currentStroke.map {
                                            DrawingPoint(
                                                x = it.x,
                                                y = it.y
                                            )
                                        }
                                )

                            onStrokeFinished(stroke)
                        }

                        currentStroke = emptyList()
                    }
                )
            }
    ) {

        // Dibujos guardados
        strokes.forEach { stroke ->

            stroke.points
                .map {
                    Offset(
                        x = it.x,
                        y = it.y
                    )
                }
                .zipWithNext()
                .forEach { (start, end) ->

                    drawLine(
                        color = strokeColor,
                        start = start,
                        end = end,
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
        }

        // Trazo actual
        currentStroke
            .zipWithNext()
            .forEach { (start, end) ->

                drawLine(
                    color = strokeColor,
                    start = start,
                    end = end,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }
    }
}
