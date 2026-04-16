package dev.overlaypen.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import dev.overlaypen.app.MainActivity
import dev.overlaypen.app.R
import kotlin.math.abs
import kotlin.math.roundToInt

class OverlayService : Service(), ToolPaletteView.Callbacks {
    private lateinit var windowManager: WindowManager
    private val session = DrawingSessionStore()

    private var bubbleView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var passiveCanvasView: OverlayCanvasView? = null
    private var drawingCanvasView: OverlayCanvasView? = null
    private var toolPaletteView: ToolPaletteView? = null
    private var paletteChipView: View? = null
    private var bubblePositionX: Int? = null
    private var bubblePositionY: Int? = null
    private var palettePositionX: Int? = null
    private var palettePositionY: Int? = null
    private var paletteCollapsed = false
    private var compactDrawingEnabled = false

    private val horizontalMarginPx: Int
        get() = dp(16)

    private val verticalMarginPx: Int
        get() = dp(24)

    private val expandedPaletteMinimumVisibleHeightPx: Int
        get() = dp(72)

    private val expandedPaletteMinimumVisibleWidthPx: Int
        get() = dp(120)

    private val expandedPaletteFallbackWidthPx: Int
        get() = dp(288)

    private val expandedPaletteFallbackHeightPx: Int
        get() = dp(360)

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WindowManager::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: ACTION_START) {
            ACTION_START -> startOverlaySession()
            ACTION_OPEN_DRAWING -> {
                startOverlaySession()
                enterDrawingMode()
            }

            ACTION_CLEAR -> clearAnnotations()
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        removeAllWindows()
        session.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onClearAnnotations() {
        clearAnnotations()
    }

    override fun onCollapsePalette() {
        collapseIntoPassiveToolsChip()
    }

    override fun onStopOverlay() {
        stopSelf()
    }

    private fun startOverlaySession() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        startForegroundCompat(buildNotification())
        if (drawingCanvasView != null || toolPaletteView != null) {
            return
        }
        if (paletteCollapsed) {
            removeBubble()
            if (compactDrawingEnabled) {
                removePassiveCanvas()
                ensureCompactDrawingCanvas()
            } else {
                removeCompactDrawingCanvas()
            }
            showCollapsedPaletteChip()
        } else {
            removeCompactDrawingCanvas()
            removePaletteChip()
            if (bubbleView == null) {
                showBubble()
            }
        }
        if (session.hasStrokes() && !compactDrawingEnabled) {
            showPassiveCanvas()
        } else {
            removePassiveCanvas()
        }
    }

    private fun enterDrawingMode() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        paletteCollapsed = false
        compactDrawingEnabled = false
        removePassiveCanvas()
        removeBubble()
        removePaletteChip()
        if (drawingCanvasView == null) {
            drawingCanvasView = OverlayCanvasView(this, session, acceptsInput = true)
            windowManager.addView(drawingCanvasView, createFullscreenParams(flags = interactiveFlags()))
        }
        showExpandedPalette()
    }

    private fun clearAnnotations() {
        session.clear()
        removePassiveCanvas()
    }

    private fun collapseIntoPassiveToolsChip() {
        paletteCollapsed = true
        compactDrawingEnabled = false
        removeExpandedPalette()
        removeCompactDrawingCanvas()
        if (session.hasStrokes()) {
            showPassiveCanvas()
        } else {
            removePassiveCanvas()
        }
        showCollapsedPaletteChip()
    }

    private fun showBubble() {
        if (bubbleView != null) {
            return
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bubblePositionX ?: horizontalMarginPx
            y = bubblePositionY ?: verticalMarginPx
        }
        bubbleParams = params
        bubbleView = createBubbleView(params)
        windowManager.addView(bubbleView, params)
    }

    private fun showPassiveCanvas() {
        if (passiveCanvasView != null || !session.hasStrokes()) {
            return
        }
        passiveCanvasView = OverlayCanvasView(this, session, acceptsInput = false)
        windowManager.addView(
            passiveCanvasView,
            createFullscreenParams(
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                alpha = PASSIVE_OVERLAY_ALPHA,
            ),
        )
    }

    private fun removeBubble() {
        bubbleView?.let { windowManager.removeView(it) }
        bubbleView = null
        bubbleParams = null
    }

    private fun removePassiveCanvas() {
        passiveCanvasView?.let { windowManager.removeView(it) }
        passiveCanvasView = null
    }

    private fun removeDrawingViews() {
        removeCompactDrawingCanvas()
        toolPaletteView?.let { windowManager.removeView(it) }
        toolPaletteView = null
        paletteChipView?.let { windowManager.removeView(it) }
        paletteChipView = null
    }

    private fun removeAllWindows() {
        removeDrawingViews()
        removePassiveCanvas()
        removeBubble()
    }

    private fun createBubbleView(params: WindowManager.LayoutParams): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(14), dp(10))
            background = getDrawable(R.drawable.floating_bubble_bg)
            elevation = 20f
            setOnClickListener { enterDrawingMode() }

            addView(
                createLauncherIconView(
                    sizeDp = 38,
                    iconSizeDp = 18,
                ),
            )

            addView(
                createLauncherLabelView(
                    text = getString(R.string.bubble_label),
                    textSizeSp = 14f,
                ),
            )
        }.also { bubble ->
            attachDragTouchListener(
                touchTargets = listOf(bubble),
                windowView = bubble,
                params = params,
                fallbackWidthPx = dp(126),
                fallbackHeightPx = dp(58),
                snapToHorizontalEdge = false,
            ) { x, y ->
                bubblePositionX = x
                bubblePositionY = y
            }
        }
    }

    private fun createFullscreenParams(
        flags: Int,
        alpha: Float = 1f,
    ): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            this.alpha = alpha
        }
    }

    private fun createPaletteParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = palettePositionX ?: defaultPaletteX()
            y = palettePositionY ?: verticalMarginPx
        }
    }

    private fun createPaletteChipParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bubblePositionX ?: horizontalMarginPx
            y = bubblePositionY ?: verticalMarginPx
        }
    }

    private fun interactiveFlags(): Int {
        return WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    }

    private fun startForegroundCompat(notification: Notification) {
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST)
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val resumeIntent = PendingIntent.getService(
            this,
            2,
            createIntent(this, ACTION_OPEN_DRAWING),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val clearIntent = PendingIntent.getService(
            this,
            3,
            createIntent(this, ACTION_CLEAR),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            4,
            createIntent(this, ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_body))
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(0, getString(R.string.notification_action_resume), resumeIntent)
            .addAction(0, getString(R.string.notification_action_clear), clearIntent)
            .addAction(0, getString(R.string.notification_action_stop), stopIntent)
            .build()
    }

    private fun showExpandedPalette() {
        removePaletteChip()
        if (toolPaletteView != null) {
            return
        }
        toolPaletteView = ToolPaletteView(this, session, this)
        val paletteParams = createPaletteParams()
        windowManager.addView(toolPaletteView, paletteParams)
        attachDragTouchListener(
            touchTargets = toolPaletteView!!.dragHandleViews(),
            windowView = toolPaletteView!!,
            params = paletteParams,
            fallbackWidthPx = expandedPaletteFallbackWidthPx,
            fallbackHeightPx = expandedPaletteFallbackHeightPx,
            minimumVisibleWidthPx = expandedPaletteMinimumVisibleWidthPx,
            minimumVisibleHeightPx = expandedPaletteMinimumVisibleHeightPx,
            snapToHorizontalEdge = true,
            bottomMarginPx = 0,
        ) { x, y ->
            palettePositionX = x
            palettePositionY = y
        }
    }

    private fun showCollapsedPaletteChip() {
        removeExpandedPalette()
        if (paletteChipView != null) {
            return
        }
        val chipParams = createPaletteChipParams()
        paletteChipView = createPaletteChipView(chipParams)
        windowManager.addView(paletteChipView, chipParams)
    }

    private fun removeExpandedPalette() {
        toolPaletteView?.let { windowManager.removeView(it) }
        toolPaletteView = null
    }

    private fun removePaletteChip() {
        paletteChipView?.let { windowManager.removeView(it) }
        paletteChipView = null
    }

    private fun createPaletteChipView(params: WindowManager.LayoutParams): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(14), dp(10))
            background = getDrawable(R.drawable.overlay_chip_bg)
            elevation = 18f

            val launcherBody = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setOnClickListener {
                    paletteCollapsed = false
                    compactDrawingEnabled = false
                    enterDrawingMode()
                }

                addView(
                    createLauncherIconView(
                        sizeDp = 34,
                        iconSizeDp = 16,
                    ),
                )

                addView(
                    createLauncherLabelView(
                        text = getString(R.string.bubble_label),
                        textSizeSp = 13f,
                    ),
                )
            }

            val toggleButton = TextView(context).apply {
                updateCompactDrawingToggle()
                setOnClickListener {
                    post { setCompactDrawingEnabled(!compactDrawingEnabled) }
                }
            }

            addView(launcherBody)
            addView(
                View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(8), 1)
                },
            )
            addView(toggleButton)
        }.also { chip ->
            attachDragTouchListener(
                touchTargets = listOf(chip.getChildAt(0)),
                windowView = chip,
                params = params,
                fallbackWidthPx = dp(180),
                fallbackHeightPx = dp(54),
                snapToHorizontalEdge = false,
            ) { x, y ->
                bubblePositionX = x
                bubblePositionY = y
            }
        }
    }

    private fun TextView.updateCompactDrawingToggle() {
        val enabled = compactDrawingEnabled
        text = getString(if (enabled) R.string.compact_draw_toggle_on else R.string.compact_draw_toggle_off)
        contentDescription = getString(R.string.compact_draw_toggle_description)
        textSize = 12f
        letterSpacing = 0.04f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(if (enabled) 0xFFFFFFFF.toInt() else 0xFF102A43.toInt())
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(18).toFloat()
            setColor(if (enabled) 0xFF0B7285.toInt() else 0xFFD9E2EC.toInt())
        }
    }

    private fun setCompactDrawingEnabled(enabled: Boolean) {
        compactDrawingEnabled = enabled
        if (enabled) {
            removePassiveCanvas()
            ensureCompactDrawingCanvas()
        } else {
            removeCompactDrawingCanvas()
            if (session.hasStrokes()) {
                showPassiveCanvas()
            } else {
                removePassiveCanvas()
            }
        }
        if (paletteCollapsed) {
            removePaletteChip()
            showCollapsedPaletteChip()
        }
    }

    private fun ensureCompactDrawingCanvas() {
        if (drawingCanvasView != null) {
            return
        }
        drawingCanvasView = OverlayCanvasView(this, session, acceptsInput = true)
        windowManager.addView(drawingCanvasView, createFullscreenParams(flags = interactiveFlags()))
    }

    private fun removeCompactDrawingCanvas() {
        drawingCanvasView?.let { windowManager.removeView(it) }
        drawingCanvasView = null
    }

    private fun createLauncherIconView(
        sizeDp: Int,
        iconSizeDp: Int,
    ): View {
        return FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp))
            background = context.getDrawable(R.drawable.overlay_chip_orb_bg)

            addView(
                ImageView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(dp(iconSizeDp), dp(iconSizeDp), Gravity.CENTER)
                    setImageResource(R.drawable.overlay_launcher_icon)
                    setColorFilter(0xFFFFFFFF.toInt(), PorterDuff.Mode.SRC_IN)
                },
            )
        }
    }

    private fun createLauncherLabelView(
        text: String,
        textSizeSp: Float,
    ): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).also {
                it.marginStart = dp(10)
            }
            this.text = text
            textSize = textSizeSp
            letterSpacing = 0.02f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFFFFFFFF.toInt())
        }
    }

    private fun attachDragTouchListener(
        touchTargets: List<View>,
        windowView: View,
        params: WindowManager.LayoutParams,
        fallbackWidthPx: Int,
        fallbackHeightPx: Int,
        minimumVisibleWidthPx: Int = fallbackWidthPx,
        minimumVisibleHeightPx: Int = fallbackHeightPx,
        snapToHorizontalEdge: Boolean,
        bottomMarginPx: Int = verticalMarginPx,
        onPositionChanged: (x: Int, y: Int) -> Unit = { _, _ -> },
    ) {
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        val listener = object : View.OnTouchListener {
            private var startX = 0
            private var startY = 0
            private var downRawX = 0f
            private var downRawY = 0f
            private var dragging = false

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = params.x
                        startY = params.y
                        downRawX = event.rawX
                        downRawY = event.rawY
                        dragging = false
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - downRawX).roundToInt()
                        val dy = (event.rawY - downRawY).roundToInt()
                        if (!dragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                            dragging = true
                        }
                        if (dragging) {
                            val overlayHeight = currentOverlayHeight(windowView, fallbackHeightPx)
                            val coordinates = OverlayPositioning.clamp(
                                requestedX = startX + dx,
                                requestedY = startY + dy,
                                screenWidth = screenWidthPx(),
                                screenHeight = screenHeightPx(),
                                overlayWidth = currentOverlayWidth(windowView, fallbackWidthPx),
                                overlayHeight = overlayHeight,
                                horizontalMargin = horizontalMarginPx,
                                verticalMargin = verticalMarginPx,
                                minimumVisibleWidth = minimumVisibleWidthPx.coerceAtMost(currentOverlayWidth(windowView, fallbackWidthPx)),
                                minimumVisibleHeight = minimumVisibleHeightPx.coerceAtMost(overlayHeight),
                                bottomMargin = bottomMarginPx,
                            )
                            params.x = coordinates.x
                            params.y = coordinates.y
                            windowManager.updateViewLayout(windowView, params)
                            onPositionChanged(params.x, params.y)
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (dragging && snapToHorizontalEdge) {
                            val overlayHeight = currentOverlayHeight(windowView, fallbackHeightPx)
                            val snappedX = OverlayPositioning.snapToHorizontalEdge(
                                currentX = params.x,
                                screenWidth = screenWidthPx(),
                                overlayWidth = currentOverlayWidth(windowView, fallbackWidthPx),
                                horizontalMargin = horizontalMarginPx,
                                minimumVisibleWidth = minimumVisibleWidthPx.coerceAtMost(currentOverlayWidth(windowView, fallbackWidthPx)),
                            )
                            val coordinates = OverlayPositioning.clamp(
                                requestedX = snappedX,
                                requestedY = params.y,
                                screenWidth = screenWidthPx(),
                                screenHeight = screenHeightPx(),
                                overlayWidth = currentOverlayWidth(windowView, fallbackWidthPx),
                                overlayHeight = overlayHeight,
                                horizontalMargin = horizontalMarginPx,
                                verticalMargin = verticalMarginPx,
                                minimumVisibleWidth = minimumVisibleWidthPx.coerceAtMost(currentOverlayWidth(windowView, fallbackWidthPx)),
                                minimumVisibleHeight = minimumVisibleHeightPx.coerceAtMost(overlayHeight),
                                bottomMargin = bottomMarginPx,
                            )
                            params.x = coordinates.x
                            params.y = coordinates.y
                            windowManager.updateViewLayout(windowView, params)
                            onPositionChanged(params.x, params.y)
                        } else if (!dragging && windowView === view) {
                            windowView.performClick()
                        }
                        return true
                    }

                    MotionEvent.ACTION_CANCEL -> return true
                }
                return false
            }
        }
        touchTargets.forEach { it.setOnTouchListener(listener) }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    private fun dp(value: Int): Int {
        val density = resources.displayMetrics.density
        return (value * density).roundToInt()
    }

    private fun defaultPaletteX(): Int {
        return OverlayPositioning.defaultRightDock(
            screenWidth = screenWidthPx(),
            overlayWidth = expandedPaletteFallbackWidthPx,
            horizontalMargin = horizontalMarginPx,
        )
    }

    private fun screenWidthPx(): Int = windowManager.currentWindowMetrics.bounds.width()

    private fun screenHeightPx(): Int = windowManager.currentWindowMetrics.bounds.height()

    private fun currentOverlayWidth(view: View, fallbackWidthPx: Int): Int {
        return view.width.takeIf { it > 0 } ?: fallbackWidthPx
    }

    private fun currentOverlayHeight(view: View, fallbackHeightPx: Int): Int {
        return view.height.takeIf { it > 0 } ?: fallbackHeightPx
    }

    companion object {
        const val ACTION_START = "dev.overlaypen.app.action.START"
        const val ACTION_OPEN_DRAWING = "dev.overlaypen.app.action.OPEN_DRAWING"
        const val ACTION_CLEAR = "dev.overlaypen.app.action.CLEAR"
        const val ACTION_STOP = "dev.overlaypen.app.action.STOP"

        private const val CHANNEL_ID = "overlay_pen_session"
        private const val NOTIFICATION_ID = 4207
        private const val PASSIVE_OVERLAY_ALPHA = 0.78f

        fun createIntent(context: Context, action: String): Intent {
            return Intent(context, OverlayService::class.java).setAction(action)
        }
    }
}

private class BubbleTextView(context: Context) : TextView(context) {
    var onPerformClick: (() -> Unit)? = null

    override fun performClick(): Boolean {
        super.performClick()
        onPerformClick?.invoke()
        return true
    }
}
