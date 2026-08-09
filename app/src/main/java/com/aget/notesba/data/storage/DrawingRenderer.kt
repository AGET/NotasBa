package com.aget.notesba.data.storage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.aget.notesba.domain.model.DrawingStroke

class DrawingRenderer {

    fun render(
        strokes: List<DrawingStroke>,
        width: Int,
        height: Int
    ): Bitmap {

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        // Fondo blanco
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        strokes.forEach { stroke ->

            if (stroke.points.isEmpty()) {
                return@forEach
            }

            val path = Path()

            val firstPoint =
                stroke.points.first()

            path.moveTo(
                firstPoint.x,
                firstPoint.y
            )

            stroke.points
                .drop(1)
                .forEach { point ->

                    path.lineTo(
                        point.x,
                        point.y
                    )
                }

            canvas.drawPath(
                path,
                paint
            )
        }

        return bitmap
    }
}
