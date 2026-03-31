package dev.overlaypen.app.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.toColorInt
import dev.overlaypen.app.R
import dev.overlaypen.app.model.ToolMode

@SuppressLint("ViewConstructor")
class ToolPaletteView(
    context: Context,
    private val session: DrawingSessionStore,
    private val callbacks: Callbacks,
) : LinearLayout(context) {
    interface Callbacks {
        fun onClearAnnotations()
        fun onCollapsePalette()
        fun onKeepAnnotations()
        fun onStopOverlay()
    }

    private val panelLabelColor = Color.WHITE
    private val accentColor = "#EE9B00".toColorInt()
    private val activeButtonTint = "#0B7285".toColorInt()
    private val inactiveButtonTint = "#D9E2EC".toColorInt()
    private val colorSwatches = mutableMapOf<Int, View>()

    private val penButton = paletteButton(context.getString(R.string.palette_pen)) { session.updateTool(ToolMode.PEN) }
    private val eraserButton = paletteButton(context.getString(R.string.palette_eraser)) { session.updateTool(ToolMode.ERASER) }
    private val undoButton = paletteButton(context.getString(R.string.palette_undo)) { session.undo() }
    private val typeButton = paletteButton(context.getString(R.string.tool_type_format, "")) { session.cyclePenType() }
    private val clearButton = paletteButton(context.getString(R.string.palette_clear)) { callbacks.onClearAnnotations() }
    private val keepButton = paletteButton(context.getString(R.string.palette_keep)) { callbacks.onKeepAnnotations() }
    private val closeButton = paletteButton(context.getString(R.string.palette_close)) { callbacks.onStopOverlay() }
    private val dragHandle = paletteHandle(context.getString(R.string.palette_move))
    private val collapseHandle = paletteHandle(context.getString(R.string.palette_hide)) { callbacks.onCollapsePalette() }
    private val widthValue = paletteLabel("")
    private val opacityValue = paletteLabel("")
    private val widthSeekBar = SeekBar(context)
    private val opacitySeekBar = SeekBar(context)
    private val sessionListener: () -> Unit = { post(::refreshFromSession) }

    private val paletteColors = listOf(
        0xFF111827.toInt(),
        0xFFFFFFFF.toInt(),
        0xFF0F766E.toInt(),
        0xFF2563EB.toInt(),
        0xFFDC2626.toInt(),
        0xFFF59E0B.toInt(),
    )

    init {
        orientation = VERTICAL
        background = context.getDrawable(R.drawable.overlay_panel_bg)
        elevation = 16f
        setPadding(dp(16), dp(16), dp(16), dp(16))
        layoutParams = LayoutParams(dp(320), LayoutParams.WRAP_CONTENT)

        addView(headerRow())
        addView(spacer(10))
        addView(buttonRow(penButton, eraserButton, undoButton))
        addView(spacer(8))
        addView(buttonRow(typeButton, clearButton, keepButton))
        addView(spacer(8))
        addView(buttonRow(closeButton))
        addView(spacer(12))
        addView(colorRow())
        addView(spacer(12))
        addView(settingRow(context.getString(R.string.palette_width), widthValue))
        configureWidthSeekBar()
        addView(widthSeekBar)
        addView(spacer(8))
        addView(settingRow(context.getString(R.string.palette_opacity), opacityValue))
        configureOpacitySeekBar()
        addView(opacitySeekBar)

        session.addListener(sessionListener)
        refreshFromSession()
    }

    fun dragHandleView(): View = dragHandle

    override fun onDetachedFromWindow() {
        session.removeListener(sessionListener)
        super.onDetachedFromWindow()
    }

    private fun configureWidthSeekBar() {
        widthSeekBar.max = 30
        widthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    session.updateWidth(progress + 2f)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun configureOpacitySeekBar() {
        opacitySeekBar.max = 85
        opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    session.updateOpacity((progress + 15) / 100f)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun colorRow(): View {
        val container = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        paletteColors.forEach { color ->
            val swatch = View(context).apply {
                layoutParams = LayoutParams(dp(28), dp(28)).also {
                    it.marginEnd = dp(8)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
                setOnClickListener { session.updateColor(color) }
            }
            colorSwatches[color] = swatch
            container.addView(swatch)
        }
        return HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            addView(container)
        }
    }

    private fun settingRow(label: String, valueView: TextView): View {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(paletteLabel(label).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(valueView)
        }
    }

    private fun headerRow(): View {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(paletteLabel(context.getString(R.string.palette_title), 18f, true).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(dragHandle)
            addView(View(context).apply {
                layoutParams = LayoutParams(dp(8), 1)
            })
            addView(collapseHandle)
        }
    }

    private fun buttonRow(vararg buttons: Button): View {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            buttons.forEachIndexed { index, button ->
                addView(button.apply {
                    layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).also { params ->
                        if (index < buttons.lastIndex) {
                            params.marginEnd = dp(8)
                        }
                    }
                })
            }
        }
    }

    private fun paletteButton(label: String, onClick: () -> Unit): Button {
        return Button(context).apply {
            text = label
            isAllCaps = false
            minHeight = dp(40)
            minimumHeight = dp(40)
            backgroundTintList = ColorStateList.valueOf(inactiveButtonTint)
            setOnClickListener { onClick() }
        }
    }

    private fun paletteHandle(label: String, onClick: (() -> Unit)? = null): TextView {
        return TextView(context).apply {
            text = label
            setTextColor(panelLabelColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor("#335B7083".toColorInt())
                setStroke(dp(1), "#66FFFFFF".toColorInt())
            }
            onClick?.let { click ->
                setOnClickListener { click() }
            }
        }
    }

    private fun paletteLabel(
        label: String,
        textSizeSp: Float = 14f,
        bold: Boolean = false,
    ): TextView {
        return TextView(context).apply {
            text = label
            setTextColor(panelLabelColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
            if (bold) {
                setTypeface(typeface, Typeface.BOLD)
            }
        }
    }

    private fun spacer(heightDp: Int): View = View(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(heightDp))
    }

    private fun refreshFromSession() {
        val brush = session.currentBrush()
        val hasStrokes = session.hasStrokes()
        penButton.backgroundTintList = ColorStateList.valueOf(
            if (brush.toolMode == ToolMode.PEN) activeButtonTint else inactiveButtonTint,
        )
        eraserButton.backgroundTintList = ColorStateList.valueOf(
            if (brush.toolMode == ToolMode.ERASER) activeButtonTint else inactiveButtonTint,
        )
        undoButton.isEnabled = hasStrokes
        clearButton.isEnabled = hasStrokes
        keepButton.isEnabled = hasStrokes
        undoButton.alpha = if (hasStrokes) 1f else 0.55f
        clearButton.alpha = if (hasStrokes) 1f else 0.55f
        keepButton.alpha = if (hasStrokes) 1f else 0.55f
        typeButton.text = context.getString(R.string.tool_type_format, context.getString(brush.penType.labelResId()))
        widthValue.text = context.getString(R.string.tool_width_format, brush.strokeWidthDp.toInt())
        opacityValue.text = context.getString(R.string.tool_opacity_format, (brush.opacity * 100).toInt())
        widthSeekBar.progress = (brush.strokeWidthDp - 2f).toInt()
        opacitySeekBar.progress = (brush.opacity * 100).toInt() - 15
        colorSwatches.forEach { (color, swatch) ->
            val selected = brush.color == color && brush.toolMode == ToolMode.PEN
            swatch.alpha = if (selected) 1f else 0.45f
            swatch.scaleX = if (selected) 1.15f else 1f
            swatch.scaleY = if (selected) 1.15f else 1f
            swatch.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                setStroke(dp(if (selected) 2 else 1), accentColor)
            }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics,
        ).toInt()
    }
}
