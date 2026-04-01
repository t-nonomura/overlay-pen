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
        assertEquals(1, session.strokeCount())
        assertEquals(1, session.strokeSnapshot().size)

        session.undo()

        assertFalse(session.hasStrokes())
        assertTrue(session.hasRedoHistory())
        assertEquals(0, session.strokeCount())
        assertEquals(0, session.strokeSnapshot().size)
    }

    @Test
    fun redoRestoresLastUndoneStroke() {
        val session = DrawingSessionStore()
        val brush = session.currentBrush()

        session.commitStroke(
            points = listOf(NormalizedPoint(0.1f, 0.2f)),
            brushSnapshot = brush,
        )

        session.undo()
        session.redo()

        assertTrue(session.hasStrokes())
        assertFalse(session.hasRedoHistory())
        assertEquals(1, session.strokeCount())
    }

    @Test
    fun committingNewStrokeClearsRedoHistory() {
        val session = DrawingSessionStore()
        val brush = session.currentBrush()

        session.commitStroke(
            points = listOf(NormalizedPoint(0.1f, 0.2f)),
            brushSnapshot = brush,
        )
        session.undo()
        session.commitStroke(
            points = listOf(NormalizedPoint(0.3f, 0.4f)),
            brushSnapshot = brush,
        )

        assertFalse(session.hasRedoHistory())
        assertEquals(1, session.strokeCount())
    }

    @Test
    fun updateColorReturnsToPenMode() {
        val session = DrawingSessionStore()

        session.updateTool(ToolMode.ERASER)
        session.updateColor(0xFF005F73.toInt())

        assertEquals(ToolMode.PEN, session.currentBrush().toolMode)
        assertEquals(0xFF005F73.toInt(), session.currentBrush().color)
    }

    @Test
    fun clearRemovesEveryStroke() {
        val session = DrawingSessionStore()
        val brush = session.currentBrush()

        session.commitStroke(
            points = listOf(NormalizedPoint(0.2f, 0.3f)),
            brushSnapshot = brush,
        )
        session.commitStroke(
            points = listOf(NormalizedPoint(0.4f, 0.6f)),
            brushSnapshot = brush,
        )

        session.clear()

        assertFalse(session.hasStrokes())
        assertFalse(session.hasRedoHistory())
        assertEquals(0, session.strokeCount())
        assertEquals(0, session.strokeSnapshot().size)
    }
}
