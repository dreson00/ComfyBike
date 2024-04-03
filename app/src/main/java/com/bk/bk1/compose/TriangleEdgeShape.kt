package com.bk.bk1.compose

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// https://stackoverflow.com/questions/65965852/jetpack-compose-create-chat-bubble-with-arrow-and-border-elevation
class TriangleEdgeShape(val offset: Int) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val trianglePath = Path().apply {
            moveTo(x = 0f, y = size.height-offset)
            lineTo(x = 0f, y = size.height)
            lineTo(x = 0f + offset, y = size.height)
        }
        return Outline.Generic(path = trianglePath)
    }
}