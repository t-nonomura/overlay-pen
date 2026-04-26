package dev.overlaypen.app.overlay

data class OverlayCoordinates(
    val x: Int,
    val y: Int,
)

object OverlayPositioning {
    fun clamp(
        requestedX: Int,
        requestedY: Int,
        screenWidth: Int,
        screenHeight: Int,
        overlayWidth: Int,
        overlayHeight: Int,
        horizontalMargin: Int,
        verticalMargin: Int,
        minimumVisibleWidth: Int = overlayWidth,
        minimumVisibleHeight: Int = overlayHeight,
        bottomMargin: Int = verticalMargin,
    ): OverlayCoordinates {
        val clampedVisibleWidth = minimumVisibleWidth.coerceIn(1, overlayWidth)
        val minX = (horizontalMargin + clampedVisibleWidth - overlayWidth)
        val minY = verticalMargin
        val maxX = (screenWidth - clampedVisibleWidth - horizontalMargin).coerceAtLeast(minX)
        val clampedVisibleHeight = minimumVisibleHeight.coerceIn(1, overlayHeight)
        val maxY = (screenHeight - clampedVisibleHeight - bottomMargin).coerceAtLeast(minY)
        return OverlayCoordinates(
            x = requestedX.coerceIn(minX, maxX),
            y = requestedY.coerceIn(minY, maxY),
        )
    }

    fun snapToHorizontalEdge(
        currentX: Int,
        screenWidth: Int,
        overlayWidth: Int,
        horizontalMargin: Int,
        minimumVisibleWidth: Int = overlayWidth,
    ): Int {
        val clampedVisibleWidth = minimumVisibleWidth.coerceIn(1, overlayWidth)
        val leftDock = horizontalMargin + clampedVisibleWidth - overlayWidth
        val rightDock = (screenWidth - clampedVisibleWidth - horizontalMargin).coerceAtLeast(leftDock)
        val overlayMidpoint = currentX + (overlayWidth / 2)
        val screenMidpoint = screenWidth / 2
        return if (overlayMidpoint < screenMidpoint) leftDock else rightDock
    }

    fun defaultRightDock(
        screenWidth: Int,
        overlayWidth: Int,
        horizontalMargin: Int,
    ): Int {
        return (screenWidth - overlayWidth - horizontalMargin).coerceAtLeast(horizontalMargin)
    }

    fun defaultCenteredX(
        screenWidth: Int,
        overlayWidth: Int,
    ): Int {
        return (screenWidth - overlayWidth) / 2
    }
}
