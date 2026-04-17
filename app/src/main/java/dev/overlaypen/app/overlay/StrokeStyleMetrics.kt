package dev.overlaypen.app.overlay

object StrokeStyleMetrics {
    fun dashedPatternDp(strokeWidthDp: Float): FloatArray {
        val normalizedStrokeWidthDp = strokeWidthDp.coerceAtLeast(2f)
        val dashLengthDp = 16f + (normalizedStrokeWidthDp * 1.1f)
        val gapLengthDp = 10f + (normalizedStrokeWidthDp * 1.5f)
        return floatArrayOf(dashLengthDp, gapLengthDp)
    }
}
