package dev.overlaypen.app.overlay

import android.annotation.SuppressLint
import android.graphics.Bitmap
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
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val reusablePath = Path()

    private var inProgressPoints = mutableListOf<NormalizedPoint>()
    private var inProgressBrush = session.currentBrush()
    private var committedBitmap: Bitmap? = null
    private var committedCanvas: Canvas? = null
    private var committedStrokeCount = 0

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        session.addListener(changeListener)
    }

    override fun onDetachedFromWindow() {
        session.removeListener(changeListener)
        recycleCommittedBitmap()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        syncCommittedBitmap()
        val checkpoint = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        committedBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, bitmapPaint)
        }
        if (acceptsInput && inProgressPoints.isNotEmpty()) {
            drawStroke(canvas, inProgressPoints, inProgressBrush)
        }
        canvas.restoreToCount(checkpoint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == oldw && h == oldh) {
            return
        }
        recycleCommittedBitmap()
        committedStrokeCount = 0
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
        reusablePath.reset()
        points.firstOrNull()?.let { first ->
            reusablePath.moveTo(first.x * width, first.y * height)
            points.drop(1).forEach { point ->
                reusablePath.lineTo(point.x * width, point.y * height)
            }
        }
        return reusablePath
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
            dashedPathEffect(stroke.strokeWidthDp)
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

    private fun dashedPathEffect(strokeWidthDp: Float): DashPathEffect {
        val dashLengthPx = (strokeWidthDp * 2.8f).coerceIn(14f, 30f).dpToPx(context)
        val gapLengthPx = (strokeWidthDp * 2.2f).coerceIn(10f, 24f).dpToPx(context)
        return DashPathEffect(floatArrayOf(dashLengthPx, gapLengthPx), 0f)
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

    private fun syncCommittedBitmap() {
        if (width <= 0 || height <= 0) {
            return
        }
        ensureCommittedBitmap()

        val strokes = session.strokeSnapshot()
        if (strokes.isEmpty()) {
            if (committedStrokeCount != 0) {
                clearCommittedBitmap()
                committedStrokeCount = 0
            }
            return
        }

        if (strokes.size < committedStrokeCount) {
            clearCommittedBitmap()
            committedStrokeCount = 0
        }

        if (strokes.size > committedStrokeCount) {
            val targetCanvas = committedCanvas ?: return
            for (index in committedStrokeCount until strokes.size) {
                drawStroke(targetCanvas, strokes[index])
            }
            committedStrokeCount = strokes.size
        }
    }

    private fun ensureCommittedBitmap() {
        val bitmap = committedBitmap
        if (bitmap != null && bitmap.width == width && bitmap.height == height) {
            return
        }
        recycleCommittedBitmap()
        committedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        committedCanvas = Canvas(committedBitmap!!)
        committedStrokeCount = 0
    }

    private fun clearCommittedBitmap() {
        committedBitmap?.eraseColor(Color.TRANSPARENT)
    }

    private fun recycleCommittedBitmap() {
        committedBitmap?.recycle()
        committedBitmap = null
        committedCanvas = null
    }
}
