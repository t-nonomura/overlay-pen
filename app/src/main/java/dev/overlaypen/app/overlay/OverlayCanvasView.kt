package dev.overlaypen.app.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import dev.overlaypen.app.model.BrushState
import dev.overlaypen.app.model.NormalizedPoint
import dev.overlaypen.app.model.PenType
import dev.overlaypen.app.model.Stroke
import dev.overlaypen.app.model.ToolMode

@SuppressLint("ViewConstructor")
class OverlayCanvasView @JvmOverloads constructor(
    context: Context,
    private val session: DrawingSessionStore,
    private val acceptsInput: Boolean,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val changeListener: () -> Unit = { postInvalidateOnAnimation() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var inProgressPoints = mutableListOf<NormalizedPoint>()
    private var inProgressBrush = session.currentBrush()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        session.addListener(changeListener)
    }

    override fun onDetachedFromWindow() {
        session.removeListener(changeListener)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val checkpoint = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        session.strokeSnapshot().forEach { drawStroke(canvas, it) }
        if (acceptsInput && inProgressPoints.isNotEmpty()) {
            drawStroke(canvas, inProgressPoints, inProgressBrush)
        }
        canvas.restoreToCount(checkpoint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!acceptsInput || width == 0 || height == 0) {
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                inProgressBrush = session.currentBrush()
                inProgressPoints = mutableListOf(normalize(event.x, event.y))
                postInvalidateOnAnimation()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                for (index in 0 until event.historySize) {
                    inProgressPoints += normalize(event.getHistoricalX(index), event.getHistoricalY(index))
                }
                inProgressPoints += normalize(event.x, event.y)
                postInvalidateOnAnimation()
                return true
            }

            MotionEvent.ACTION_UP -> {
                inProgressPoints += normalize(event.x, event.y)
                session.commitStroke(inProgressPoints.toList(), inProgressBrush)
                inProgressPoints.clear()
                performClick()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                inProgressPoints.clear()
                postInvalidateOnAnimation()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun normalize(x: Float, y: Float): NormalizedPoint {
        return NormalizedPoint(
            x = (x / width.toFloat()).coerceIn(0f, 1f),
            y = (y / height.toFloat()).coerceIn(0f, 1f),
        )
    }

    private fun drawStroke(canvas: Canvas, stroke: Stroke) {
        val path = buildPath(stroke.points)
        configurePaint(stroke)
        if (stroke.points.size == 1) {
            val point = stroke.points.first()
            canvas.drawPoint(point.x * width, point.y * height, paint)
        } else {
            canvas.drawPath(path, paint)
        }
    }

    private fun drawStroke(canvas: Canvas, points: List<NormalizedPoint>, brush: BrushState) {
        val path = buildPath(points)
        configurePaint(
            Stroke(
                points = points,
                color = brush.color,
                strokeWidthDp = brush.strokeWidthDp,
                opacity = brush.opacity,
                penType = brush.penType,
                isEraser = brush.toolMode == ToolMode.ERASER,
            ),
        )
        if (points.size == 1) {
            val point = points.first()
            canvas.drawPoint(point.x * width, point.y * height, paint)
        } else {
            canvas.drawPath(path, paint)
        }
    }

    private fun buildPath(points: List<NormalizedPoint>): Path {
        val path = Path()
        points.firstOrNull()?.let { first ->
            path.moveTo(first.x * width, first.y * height)
            points.drop(1).forEach { point ->
                path.lineTo(point.x * width, point.y * height)
            }
        }
        return path
    }

    private fun configurePaint(stroke: Stroke) {
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = stroke.strokeWidthDp.dpToPx(context)
        paint.blendMode = if (stroke.isEraser) BlendMode.CLEAR else BlendMode.SRC_OVER
        paint.pathEffect = if (!stroke.isEraser && stroke.penType == PenType.DASHED) {
            DashPathEffect(floatArrayOf(24f, 18f), 0f)
        } else {
            null
        }
        paint.color = when {
            stroke.isEraser -> Color.TRANSPARENT
            stroke.penType == PenType.HIGHLIGHTER -> {
                val scaledOpacity = (stroke.opacity * 0.45f).coerceAtLeast(0.18f)
                stroke.color.withAlpha(scaledOpacity)
            }

            else -> stroke.color.withAlpha(stroke.opacity)
        }
    }

    private fun Int.withAlpha(opacity: Float): Int {
        val alpha = (opacity.coerceIn(0f, 1f) * 255).toInt()
        return (this and 0x00FFFFFF) or (alpha shl 24)
    }

    private fun Float.dpToPx(context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            context.resources.displayMetrics,
        )
    }
}
