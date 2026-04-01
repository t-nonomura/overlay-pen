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
        bottomVisibleHeight: Int = overlayHeight,
        bottomMargin: Int = verticalMargin,
    ): OverlayCoordinates {
        val minX = horizontalMargin
        val minY = verticalMargin
        val maxX = (screenWidth - overlayWidth - horizontalMargin).coerceAtLeast(minX)
        val maxY = (screenHeight - bottomVisibleHeight - bottomMargin).coerceAtLeast(minY)
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
    ): Int {
        val leftDock = horizontalMargin
        val rightDock = (screenWidth - overlayWidth - horizontalMargin).coerceAtLeast(leftDock)
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
}
