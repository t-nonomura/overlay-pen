package dev.overlaypen.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayPositioningTest {
    @Test
    fun clampKeepsOverlayInsideScreenBounds() {
        val coordinates = OverlayPositioning.clamp(
            requestedX = 1200,
            requestedY = -20,
            screenWidth = 1080,
            screenHeight = 2400,
            overlayWidth = 200,
            overlayHeight = 300,
            horizontalMargin = 16,
            verticalMargin = 24,
        )

        assertEquals(864, coordinates.x)
        assertEquals(24, coordinates.y)
    }

    @Test
    fun clampAllowsBottomOverflowWhenMinimumVisibleHeightIsSmallerThanOverlay() {
        val coordinates = OverlayPositioning.clamp(
            requestedX = 120,
            requestedY = 3000,
            screenWidth = 1080,
            screenHeight = 2400,
            overlayWidth = 320,
            overlayHeight = 360,
            horizontalMargin = 16,
            verticalMargin = 24,
            minimumVisibleHeight = 104,
            bottomMargin = 0,
        )

        assertEquals(120, coordinates.x)
        assertEquals(2296, coordinates.y)
    }

    @Test
    fun clampAllowsHorizontalOverflowWhenMinimumVisibleWidthIsSmallerThanOverlay() {
        val coordinates = OverlayPositioning.clamp(
            requestedX = -400,
            requestedY = 48,
            screenWidth = 1080,
            screenHeight = 2400,
            overlayWidth = 320,
            overlayHeight = 360,
            horizontalMargin = 16,
            verticalMargin = 24,
            minimumVisibleWidth = 120,
        )

        assertEquals(-184, coordinates.x)
        assertEquals(48, coordinates.y)
    }

    @Test
    fun snapToHorizontalEdgeChoosesLeftDockWhenCloserToLeft() {
        val snappedX = OverlayPositioning.snapToHorizontalEdge(
            currentX = 90,
            screenWidth = 1080,
            overlayWidth = 140,
            horizontalMargin = 16,
        )

        assertEquals(16, snappedX)
    }

    @Test
    fun snapToHorizontalEdgeChoosesRightDockWhenCloserToRight() {
        val snappedX = OverlayPositioning.snapToHorizontalEdge(
            currentX = 700,
            screenWidth = 1080,
            overlayWidth = 140,
            horizontalMargin = 16,
        )

        assertEquals(924, snappedX)
    }

    @Test
    fun snapToHorizontalEdgeAllowsHorizontalOverflowWhenMinimumVisibleWidthIsSmallerThanOverlay() {
        val snappedX = OverlayPositioning.snapToHorizontalEdge(
            currentX = 780,
            screenWidth = 1080,
            overlayWidth = 320,
            horizontalMargin = 16,
            minimumVisibleWidth = 120,
        )

        assertEquals(944, snappedX)
    }

    @Test
    fun defaultCenteredXPlacesOverlayAtScreenCenter() {
        val x = OverlayPositioning.defaultCenteredX(
            screenWidth = 1080,
            overlayWidth = 288,
        )

        assertEquals(396, x)
    }
}
