package dev.overlaypen.app.model

import dev.overlaypen.app.R

enum class ToolMode {
    PEN,
    ERASER,
}

enum class PenType {
    SOLID,
    HIGHLIGHTER,
    DASHED,
    ;

    fun next(): PenType = when (this) {
        SOLID -> HIGHLIGHTER
        HIGHLIGHTER -> DASHED
        DASHED -> SOLID
    }

    fun labelResId(): Int = when (this) {
        SOLID -> R.string.pen_type_solid
        HIGHLIGHTER -> R.string.pen_type_highlighter
        DASHED -> R.string.pen_type_dashed
    }
}

data class NormalizedPoint(
    val x: Float,
    val y: Float,
)

data class BrushState(
    val toolMode: ToolMode = ToolMode.PEN,
    val color: Int = 0xFF111827.toInt(),
    val strokeWidthDp: Float = 8f,
    val opacity: Float = 1f,
    val penType: PenType = PenType.SOLID,
)

data class Stroke(
    val points: List<NormalizedPoint>,
    val color: Int,
    val strokeWidthDp: Float,
    val opacity: Float,
    val penType: PenType,
    val isEraser: Boolean,
)
