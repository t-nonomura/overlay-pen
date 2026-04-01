package dev.overlaypen.app.overlay

import dev.overlaypen.app.model.BrushState
import dev.overlaypen.app.model.NormalizedPoint
import dev.overlaypen.app.model.Stroke
import dev.overlaypen.app.model.ToolMode

class DrawingSessionStore {
    private val listeners = linkedSetOf<() -> Unit>()
    private val strokes = mutableListOf<Stroke>()
    private val redoStrokes = mutableListOf<Stroke>()
    private var brushState = BrushState()

    fun addListener(listener: () -> Unit) {
        listeners += listener
    }

    fun removeListener(listener: () -> Unit) {
        listeners -= listener
    }

    fun currentBrush(): BrushState = brushState

    fun strokeSnapshot(): List<Stroke> = strokes.toList()

    fun strokeCount(): Int = strokes.size

    fun hasStrokes(): Boolean = strokes.isNotEmpty()

    fun hasRedoHistory(): Boolean = redoStrokes.isNotEmpty()

    fun updateTool(mode: ToolMode) {
        brushState = brushState.copy(toolMode = mode)
        notifyChanged()
    }

    fun updateColor(color: Int) {
        brushState = brushState.copy(color = color, toolMode = ToolMode.PEN)
        notifyChanged()
    }

    fun updateWidth(strokeWidthDp: Float) {
        brushState = brushState.copy(strokeWidthDp = strokeWidthDp.coerceIn(2f, 32f))
        notifyChanged()
    }

    fun updateOpacity(opacity: Float) {
        brushState = brushState.copy(opacity = opacity.coerceIn(0.15f, 1f))
        notifyChanged()
    }

    fun cyclePenType() {
        brushState = brushState.copy(
            penType = brushState.penType.next(),
            toolMode = ToolMode.PEN,
        )
        notifyChanged()
    }

    fun commitStroke(points: List<NormalizedPoint>, brushSnapshot: BrushState) {
        if (points.isEmpty()) {
            return
        }
        redoStrokes.clear()
        strokes += Stroke(
            points = points,
            color = brushSnapshot.color,
            strokeWidthDp = brushSnapshot.strokeWidthDp,
            opacity = brushSnapshot.opacity,
            penType = brushSnapshot.penType,
            isEraser = brushSnapshot.toolMode == ToolMode.ERASER,
        )
        notifyChanged()
    }

    fun undo() {
        if (strokes.isEmpty()) {
            return
        }
        redoStrokes += strokes.removeAt(strokes.lastIndex)
        notifyChanged()
    }

    fun redo() {
        if (redoStrokes.isEmpty()) {
            return
        }
        strokes += redoStrokes.removeAt(redoStrokes.lastIndex)
        notifyChanged()
    }

    fun clear() {
        if (strokes.isEmpty() && redoStrokes.isEmpty()) {
            return
        }
        strokes.clear()
        redoStrokes.clear()
        notifyChanged()
    }

    private fun notifyChanged() {
        listeners.forEach { it.invoke() }
    }
}
