package dev.overlaypen.app.model

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

    fun label(): String = when (this) {
        SOLID -> "Solid"
        HIGHLIGHTER -> "Highlighter"
        DASHED -> "Dashed"
    }
}

data class NormalizedPoint(
    val x: Float,
    val y: Float,
)

data class BrushState(
    val toolMode: ToolMode = ToolMode.PEN,
    val color: Int = 0xFFE4572E.toInt(),
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
