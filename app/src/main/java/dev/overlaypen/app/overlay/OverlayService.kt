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
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
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

    override fun onKeepAnnotations() {
        keepAnnotations()
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
        if (bubbleView == null) {
            showBubble()
        }
        if (session.hasStrokes()) {
            showPassiveCanvas()
        }
    }

    private fun enterDrawingMode() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        removePassiveCanvas()
        removeBubble()
        if (drawingCanvasView == null) {
            drawingCanvasView = OverlayCanvasView(this, session, acceptsInput = true)
            windowManager.addView(drawingCanvasView, createFullscreenParams(flags = interactiveFlags()))
        }
        if (toolPaletteView == null) {
            toolPaletteView = ToolPaletteView(this, session, this)
            windowManager.addView(toolPaletteView, createPaletteParams())
        }
    }

    private fun keepAnnotations() {
        removeDrawingViews()
        showBubble()
        if (session.hasStrokes()) {
            showPassiveCanvas()
        } else {
            removePassiveCanvas()
        }
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
            x = dp(24)
            y = dp(160)
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
        drawingCanvasView?.let { windowManager.removeView(it) }
        drawingCanvasView = null
        toolPaletteView?.let { windowManager.removeView(it) }
        toolPaletteView = null
    }

    private fun removeAllWindows() {
        removeDrawingViews()
        removePassiveCanvas()
        removeBubble()
    }

    private fun createBubbleView(params: WindowManager.LayoutParams): View {
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        return BubbleTextView(this).apply {
            text = getString(R.string.bubble_label)
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(dp(18), dp(14), dp(18), dp(14))
            background = getDrawable(R.drawable.floating_bubble_bg)
            elevation = 18f
            onPerformClick = { enterDrawingMode() }

            var startX = 0
            var startY = 0
            var downRawX = 0f
            var downRawY = 0f
            var dragging = false

            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = params.x
                        startY = params.y
                        downRawX = event.rawX
                        downRawY = event.rawY
                        dragging = false
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - downRawX).roundToInt()
                        val dy = (event.rawY - downRawY).roundToInt()
                        if (!dragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                            dragging = true
                        }
                        if (dragging) {
                            params.x = startX + dx
                            params.y = startY + dy
                            windowManager.updateViewLayout(view, params)
                        }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!dragging) {
                            view.performClick()
                        }
                        true
                    }

                    else -> false
                }
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
            gravity = Gravity.TOP or Gravity.END
            x = dp(16)
            y = dp(40)
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
        val stopIntent = PendingIntent.getService(
            this,
            3,
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
            .addAction(0, getString(R.string.notification_action_stop), stopIntent)
            .build()
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

    companion object {
        const val ACTION_START = "dev.overlaypen.app.action.START"
        const val ACTION_OPEN_DRAWING = "dev.overlaypen.app.action.OPEN_DRAWING"
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
