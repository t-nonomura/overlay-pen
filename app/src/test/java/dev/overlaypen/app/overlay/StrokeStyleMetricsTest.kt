package dev.overlaypen.app.overlay

import org.junit.Assert.assertTrue
import org.junit.Test

class StrokeStyleMetricsTest {
    @Test
    fun dashedPatternKeepsThinStrokeSpacingReadable() {
        val pattern = StrokeStyleMetrics.dashedPatternDp(8f)

        assertTrue(pattern[0] >= 24f)
        assertTrue(pattern[1] >= 22f)
    }

    @Test
    fun dashedPatternWidensGapForThickStroke() {
        val thinPattern = StrokeStyleMetrics.dashedPatternDp(8f)
        val thickPattern = StrokeStyleMetrics.dashedPatternDp(32f)

        assertTrue(thickPattern[1] > thinPattern[1] * 2f)
        assertTrue(thickPattern[1] > 32f)
        assertTrue(thickPattern[0] > thinPattern[0] * 1.8f)
    }
}
