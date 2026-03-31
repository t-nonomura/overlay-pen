package dev.overlaypen.app.overlay

import dev.overlaypen.app.model.NormalizedPoint
import dev.overlaypen.app.model.ToolMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingSessionStoreTest {
    @Test
    fun commitStrokeAddsStrokeAndUndoRemovesIt() {
        val session = DrawingSessionStore()
        val brush = session.currentBrush()

        session.commitStroke(
            points = listOf(
                NormalizedPoint(0.1f, 0.2f),
                NormalizedPoint(0.8f, 0.9f),
            ),
            brushSnapshot = brush,
        )

        assertTrue(session.hasStrokes())
        assertEquals(1, session.strokeSnapshot().size)

        session.undo()

        assertFalse(session.hasStrokes())
        assertEquals(0, session.strokeSnapshot().size)
    }

    @Test
    fun updateColorReturnsToPenMode() {
        val session = DrawingSessionStore()

        session.updateTool(ToolMode.ERASER)
        session.updateColor(0xFF005F73.toInt())

        assertEquals(ToolMode.PEN, session.currentBrush().toolMode)
        assertEquals(0xFF005F73.toInt(), session.currentBrush().color)
    }
}
