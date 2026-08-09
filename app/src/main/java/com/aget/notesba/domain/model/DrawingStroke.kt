package com.aget.notesba.domain.model

data class DrawingPoint(
    val x: Float,
    val y: Float
)

data class DrawingStroke(
    val points: List<DrawingPoint>
)
