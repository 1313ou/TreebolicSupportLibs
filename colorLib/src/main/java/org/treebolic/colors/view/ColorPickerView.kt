/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.colors.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.res.use
import org.treebolic.colors.R
import org.treebolic.colors.drawable.AlphaPatternDrawable
import kotlin.math.max
import kotlin.math.min

/**
 * Displays a value picker to the user and allow them to select a value. A slider for the alpha channel is also available. Enable it by setting
 * setAlphaSliderVisible(boolean) to true.
 * @param context  context
 * @param attrs    attributes
 * @param defStyle def style
 *
 * @author Daniel Nilsson
 * @author Bernard Bou
 */
class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    /**
     * Color change listener
     */
    interface OnColorChangedListener {

        fun onColorChanged(newColor: Int)
    }

    /**
     * The width in dp of the hue panel.
     */
    private var huePanelWidth = 30f

    /**
     * The height in dp of the alpha panel
     */
    private var alphaPanelHeight = 20f

    /**
     * The distance in dp between the different value panels.
     */
    private var panelSpacing = 10f

    /**
     * The radius in dp of the value palette tracker circle.
     */
    private var paletteCircleTrackerRadius = 5f

    /**
     * The dp which the tracker of the hue or alpha panel will extend outside of its bounds.
     */
    private var rectangleTrackerOffset = 2f

    // P A I N T S

    /**
     * Saturation paint
     */
    private var satValPaint: Paint? = null

    /**
     * Saturation tracker paint
     */
    private var satValTrackerPaint: Paint? = null

    /**
     * Hue paint
     */
    private var huePaint: Paint? = null

    /**
     * Hue tracker paint
     */
    private var hueAlphaTrackerPaint: Paint? = null

    /**
     * Alpha paint
     */
    private var alphaPaint: Paint? = null

    /**
     * Alpha text paint
     */
    private var alphaTextPaint: Paint? = null

    /**
     * Border paint
     */
    private var borderPaint: Paint? = null

    // S H A D E R S

    /**
     * Value shader
     */
    private var valShader: Shader? = null

    /**
     * Saturation shader
     */
    private var satShader: Shader? = null

    /**
     * Hue shader
     */
    private var hueShader: Shader? = null

    /**
     * Alpha shader
     */
    private var alphaShader: Shader? = null

    /*
	 * We cache a bitmap of the sat/val panel which is expensive to draw each time. We can reuse it when the user is sliding the circle picker as long as the hue isn't changed.
	 */
    private var satValBackgroundCache: BitmapCache? = null

    // V A L U E S

    private var alpha = 0xff

    private var hue = 360f

    private var sat = 0f

    private var value = 0f

    // S E T T I N G S

    /**
     * Text that will be shown in the alpha slider.
     */
    private var alphaSliderText: String? = null

    /**
     * Show alpha panel
     */
    private var showAlphaPanel = false

    /**
     * Slider tracker value
     */
    private var sliderTrackerColor = -0x424243

    /**
     * Border value
     */
    private var borderColor = -0x919192

    // S T A T E

    /**
     * To remember which panel that has the "focus" when processing hardware button data.
     */
    private var lastTouchedPanel = PANEL_SAT_VAL

    /**
     * Start touch point
     */
    private var startTouchPoint: Point? = null

    /**
     * Offset from the edge we must have or else the finger tracker will get clipped when it is drawn outside of the view.
     */
    var drawingOffset = 0

    // L I S T E N E R

    /**
     * Listener
     */
    private var listener: OnColorChangedListener? = null

    // R E C T A N G L E S

    /**
     * Distance form the edges of the view of where we are allowed to draw.
     */
    private var drawingRect: RectF? = null

    /**
     * Saturation rectangle
     */
    private var satValRect: RectF? = null

    /**
     * Hue rectangle
     */
    private var hueRect: RectF? = null

    /**
     * Alpha rectangle
     */
    private var alphaRect: RectF? = null

    // P A T T E R N

    /**
     * Alpha pattern
     */
    private var alphaPattern: AlphaPatternDrawable? = null

    init {
        init(context, attrs)
    }

    /**
     * Common init
     *
     * @param context context
     * @param attrs   attributes
     */
    private fun init(context: Context, attrs: AttributeSet?) {
        // load those if set in xml resource file.
        context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView).use {
            showAlphaPanel = it.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false)
            alphaSliderText = it.getString(R.styleable.ColorPickerView_alphaChannelText)
            sliderTrackerColor = it.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, -0x424243)
            borderColor = it.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, -0x919192)
        }

        density = context.resources.displayMetrics.density
        paletteCircleTrackerRadius *= density
        rectangleTrackerOffset *= density
        huePanelWidth *= density
        alphaPanelHeight *= density
        panelSpacing *= density

        drawingOffset = calculateRequiredOffset()

        initPaintTools()

        // needed for receiving trackball motion events.
        isFocusable = true
        isFocusableInTouchMode = true
    }

    /**
     * Init paint tools
     */
    private fun initPaintTools() {
        satValPaint = Paint()
        satValTrackerPaint = Paint()
        huePaint = Paint()
        hueAlphaTrackerPaint = Paint()
        alphaPaint = Paint()
        alphaTextPaint = Paint()
        borderPaint = Paint()

        satValTrackerPaint!!.style = Paint.Style.STROKE
        satValTrackerPaint!!.strokeWidth = 2f * density
        satValTrackerPaint!!.isAntiAlias = true

        hueAlphaTrackerPaint!!.color = sliderTrackerColor
        hueAlphaTrackerPaint!!.style = Paint.Style.STROKE
        hueAlphaTrackerPaint!!.strokeWidth = 2f * density
        hueAlphaTrackerPaint!!.isAntiAlias = true

        alphaTextPaint!!.color = -0xe3e3e4
        alphaTextPaint!!.textSize = 14f * density
        alphaTextPaint!!.isAntiAlias = true
        alphaTextPaint!!.textAlign = Align.CENTER
        alphaTextPaint!!.isFakeBoldText = true
    }

    private fun calculateRequiredOffset(): Int {
        var offset = max(paletteCircleTrackerRadius.toDouble(), rectangleTrackerOffset.toDouble()).toFloat()
        offset = max(offset.toDouble(), (BORDER_WIDTH_PX * density).toDouble()).toFloat()
        return (offset * 1.5f).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        if (drawingRect!!.width() <= 0 || drawingRect!!.height() <= 0) {
            return
        }
        drawSatValPanel(canvas)
        drawHuePanel(canvas)
        drawAlphaPanel(canvas)
    }

    private fun drawSatValPanel(canvas: Canvas) {
        val rect = satValRect
        if (BORDER_WIDTH_PX > 0) {
            borderPaint!!.color = borderColor
            canvas.drawRect(
                drawingRect!!.left,
                drawingRect!!.top, rect!!.right + BORDER_WIDTH_PX, rect.bottom + BORDER_WIDTH_PX,
                borderPaint!!
            )
        }
        if (valShader == null) {
            // Black gradient has either not been created or the view has been resized.
            valShader = LinearGradient(rect!!.left, rect.top, rect.left, rect.bottom, -0x1, -0x1000000, TileMode.CLAMP)
        }
        // If the hue has changed we need to recreate the cache.
        if (satValBackgroundCache == null || satValBackgroundCache!!.value != hue) {
            if (satValBackgroundCache == null) {
                satValBackgroundCache = BitmapCache()
            }
            // We create our bitmap in the cache if it doesn't exist.
            if (satValBackgroundCache!!.bitmap == null) {
                satValBackgroundCache!!.bitmap = Bitmap.createBitmap(rect!!.width().toInt(), rect.height().toInt(), Bitmap.Config.ARGB_8888)
            }
            // We create the canvas once so we can draw on our bitmap and the hold on to it.
            if (satValBackgroundCache!!.canvas == null) {
                satValBackgroundCache!!.canvas = Canvas(satValBackgroundCache!!.bitmap!!)
            }
            val rgb = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))

            satShader = LinearGradient(rect!!.left, rect.top, rect.right, rect.top, -0x1, rgb, TileMode.CLAMP)

            val mShader = ComposeShader(valShader!!, satShader as LinearGradient, PorterDuff.Mode.MULTIPLY)
            satValPaint!!.setShader(mShader)

            // Finally we draw on our canvas, the result will be stored in our bitmap which is already in the cache.
            // Since this is drawn on a canvas not rendered on screen it will automatically not be using the hardware acceleration.
            // And this was the code that wasn't supported by hardware acceleration which mean there is no need to turn it of anymore.
            // The rest of the view will still be hardware accelerated!!
            satValBackgroundCache!!.canvas!!.drawRect(
                0f, 0f,
                satValBackgroundCache!!.bitmap!!.width.toFloat(),
                satValBackgroundCache!!.bitmap!!.height.toFloat(),
                satValPaint!!
            )
            // We set the hue value in our cache to which hue it was drawn with,
            // then we know that if it hasn't changed we can reuse our cached bitmap.
            satValBackgroundCache!!.value = hue
        }
        // We draw our bitmap from the cached, if the hue has changed
        // then it was just recreated otherwise the old one will be used.
        canvas.drawBitmap(satValBackgroundCache!!.bitmap!!, null, rect!!, null)
        val p = satValToPoint(sat, value)
        satValTrackerPaint!!.color = -0x1000000
        canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), paletteCircleTrackerRadius - density, satValTrackerPaint!!)
        satValTrackerPaint!!.color = -0x222223
        canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), paletteCircleTrackerRadius, satValTrackerPaint!!)
    }

    private fun drawHuePanel(canvas: Canvas) {
        val rect = hueRect
        if (BORDER_WIDTH_PX > 0) {
            borderPaint!!.color = borderColor
            canvas.drawRect(
                rect!!.left - BORDER_WIDTH_PX, rect.top - BORDER_WIDTH_PX, rect.right + BORDER_WIDTH_PX, rect.bottom + BORDER_WIDTH_PX,
                borderPaint!!
            )
        }
        if (hueShader == null) {
            // The hue shader has either not yet been created or the view has been resized.
            hueShader = LinearGradient(0f, 0f, 0f, rect!!.height(), buildHueColorArray(), null, TileMode.CLAMP)
            huePaint!!.setShader(hueShader)
        }
        canvas.drawRect(rect!!, huePaint!!)
        val rectHeight = 4 * density / 2
        val p = hueToPoint(hue)
        val r = RectF()
        r.left = rect.left - rectangleTrackerOffset
        r.right = rect.right + rectangleTrackerOffset
        r.top = p.y - rectHeight
        r.bottom = p.y + rectHeight
        canvas.drawRoundRect(r, 2f, 2f, hueAlphaTrackerPaint!!)
    }

    private fun drawAlphaPanel(canvas: Canvas) {
        if ((!showAlphaPanel || alphaRect == null) || alphaPattern == null) {
            return
        }
        val rect = alphaRect
        if (BORDER_WIDTH_PX > 0) {
            borderPaint!!.color = borderColor
            canvas.drawRect(
                rect!!.left - BORDER_WIDTH_PX, rect.top - BORDER_WIDTH_PX, rect.right + BORDER_WIDTH_PX, rect.bottom + BORDER_WIDTH_PX,
                borderPaint!!
            )
        }
        alphaPattern!!.draw(canvas)
        val hsv = floatArrayOf(hue, sat, value)
        val color = Color.HSVToColor(hsv)
        val acolor = Color.HSVToColor(0, hsv)
        alphaShader = LinearGradient(rect!!.left, rect.top, rect.right, rect.top, color, acolor, TileMode.CLAMP)
        alphaPaint!!.setShader(alphaShader)
        canvas.drawRect(rect, alphaPaint!!)
        if (alphaSliderText != null && alphaSliderText!!.isNotEmpty()) {
            canvas.drawText(alphaSliderText!!, rect.centerX(), rect.centerY() + 4 * density, alphaTextPaint!!)
        }
        val rectWidth = 4 * density / 2
        val p = alphaToPoint(alpha)
        val r = RectF()
        r.left = p.x - rectWidth
        r.right = p.x + rectWidth
        r.top = rect.top - rectangleTrackerOffset
        r.bottom = rect.bottom + rectangleTrackerOffset
        canvas.drawRoundRect(r, 2f, 2f, hueAlphaTrackerPaint!!)
    }

    private fun hueToPoint(hue: Float): Point {
        val rect = hueRect
        val height = rect!!.height()
        val p = Point()
        p.y = (height - hue * height / 360f + rect.top).toInt()
        p.x = rect.left.toInt()
        return p
    }

    private fun satValToPoint(sat: Float, `val`: Float): Point {
        val rect = satValRect
        val height = rect!!.height()
        val width = rect.width()
        val p = Point()
        p.x = (sat * width + rect.left).toInt()
        p.y = ((1f - `val`) * height + rect.top).toInt()
        return p
    }

    private fun alphaToPoint(alpha: Int): Point {
        val rect = alphaRect
        val width = rect!!.width()
        val p = Point()
        p.x = (width - alpha * width / 0xff + rect.left).toInt()
        p.y = rect.top.toInt()
        return p
    }

    private fun pointToSatVal(x0: Float, y0: Float): FloatArray {
        var x = x0
        var y = y0
        val rect = satValRect
        val result = FloatArray(2)
        val width = rect!!.width()
        val height = rect.height()
        x = if (x < rect.left) {
            0f
        } else if (x > rect.right) {
            width
        } else {
            x - rect.left
        }
        y = if (y < rect.top) {
            0f
        } else if (y > rect.bottom) {
            height
        } else {
            y - rect.top
        }
        result[0] = 1f / width * x
        result[1] = 1f - 1f / height * y
        return result
    }

    private fun pointToHue(y0: Float): Float {
        var y = y0
        val rect = hueRect
        val height = rect!!.height()
        y = if (y < rect.top) {
            0f
        } else if (y > rect.bottom) {
            height
        } else {
            y - rect.top
        }
        return 360f - y * 360f / height
    }

    private fun pointToAlpha(x0: Int): Int {
        var x = x0
        val rect = alphaRect
        val width = rect!!.width().toInt()
        x = if (x < rect.left) {
            0
        } else if (x > rect.right) {
            width
        } else {
            x - rect.left.toInt()
        }
        return 0xff - x * 0xff / width
    }

    override fun onTrackballEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        var update = false
        if (event.action == MotionEvent.ACTION_MOVE) {
            when (lastTouchedPanel) {
                PANEL_SAT_VAL -> {
                    var sat2: Float = sat + x / 50f
                    var value2: Float = value - y / 50f

                    if (sat2 < 0f) {
                        sat2 = 0f
                    } else if (sat2 > 1f) {
                        sat2 = 1f
                    }
                    if (value2 < 0f) {
                        value2 = 0f
                    } else if (value2 > 1f) {
                        value2 = 1f
                    }
                    sat = sat2
                    value = value2
                    update = true
                }

                PANEL_HUE -> {
                    var hue2 = hue - y * 10f
                    if (hue2 < 0f) {
                        hue2 = 0f
                    } else if (hue2 > 360f) {
                        hue2 = 360f
                    }
                    hue = hue2
                    update = true
                }

                PANEL_ALPHA -> if (showAlphaPanel && alphaRect != null) {
                    var alpha2 = (alpha - x * 10).toInt()
                    if (alpha2 < 0) {
                        alpha2 = 0
                    } else if (alpha2 > 0xff) {
                        alpha2 = 0xff
                    }
                    alpha = alpha2
                    update = true
                }

                else -> {}
            }
        }
        if (update) {
            if (listener != null) {
                listener!!.onColorChanged(Color.HSVToColor(alpha, floatArrayOf(hue, sat, value)))
            }
            invalidate()
            return true
        }
        return super.onTrackballEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var update = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTouchPoint = Point(event.x.toInt(), event.y.toInt())
                update = moveTrackersIfNeeded(event)
            }

            MotionEvent.ACTION_MOVE -> update = moveTrackersIfNeeded(event)
            MotionEvent.ACTION_UP -> {
                startTouchPoint = null
                update = moveTrackersIfNeeded(event)
            }

            else -> {}
        }
        if (update) {
            if (listener != null) {
                listener!!.onColorChanged(Color.HSVToColor(alpha, floatArrayOf(hue, sat, value)))
            }
            invalidate()
            return true
        }
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return super.onTouchEvent(event)
    }

    private fun moveTrackersIfNeeded(event: MotionEvent): Boolean {
        if (startTouchPoint == null) {
            return false
        }
        var update = false
        val startX = startTouchPoint!!.x
        val startY = startTouchPoint!!.y
        if (hueRect!!.contains(startX.toFloat(), startY.toFloat())) {
            lastTouchedPanel = PANEL_HUE
            hue = pointToHue(event.y)
            update = true
        } else if (satValRect!!.contains(startX.toFloat(), startY.toFloat())) {
            lastTouchedPanel = PANEL_SAT_VAL
            val result = pointToSatVal(event.x, event.y)
            sat = result[0]
            value = result[1]
            update = true
        } else if (alphaRect != null && alphaRect!!.contains(startX.toFloat(), startY.toFloat())) {
            lastTouchedPanel = PANEL_ALPHA
            alpha = pointToAlpha(event.x.toInt())
            update = true
        }
        return update
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var finalWidth = 0
        var finalHeight = 0
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthAllowed = MeasureSpec.getSize(widthMeasureSpec)
        val heightAllowed = MeasureSpec.getSize(heightMeasureSpec)
        // Log.d("value-picker-view", "widthMode: ${modeToString(widthMode)} heightMode: ${modeToString(heightMode)} widthAllowed: $widthAllowed heightAllowed: $heightAllowed")
        if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY) {
            // An exact value has been set in either direction, we need to stay within this size.
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                // The with has been specified exactly, we need to adopt the height to fit.
                var h = (widthAllowed - panelSpacing - huePanelWidth).toInt()
                if (showAlphaPanel) {
                    h += (panelSpacing + alphaPanelHeight).toInt()
                }
                // We can't fit the view in this container, set the size to whatever was allowed.
                finalHeight = min(h.toDouble(), heightAllowed.toDouble()).toInt()
                finalWidth = widthAllowed
            } else if ( /*heightMode == MeasureSpec.EXACTLY && */widthMode != MeasureSpec.EXACTLY) {
                // The height has been specified exactly, we need to stay within this height and adopt the width.
                var w = (heightAllowed + panelSpacing + huePanelWidth).toInt()

                if (showAlphaPanel) {
                    w -= (panelSpacing - alphaPanelHeight).toInt()
                }
                // we can't fit within this container, set the size to whatever was allowed.
                finalWidth = min(w.toDouble(), widthAllowed.toDouble()).toInt()
                finalHeight = heightAllowed
            } else {
                // If we get here the dev has set the width and height to exact sizes. For example match_parent or 300dp.
                // This will mean that the sat/val panel will not be square but it doesn't matter. It will work anyway.
                // In all other scenarios our goal is to make that panel square.
                // We set the sizes to exactly what we were told.
                finalWidth = widthAllowed
                finalHeight = heightAllowed
            }
        } else {
            // If no exact size has been set we try to make our view as big as possible within the allowed space.
            // Calculate the needed with to layout the view based on the allowed height.
            var widthNeeded = (heightAllowed + panelSpacing + huePanelWidth).toInt()
            // Calculate the needed height to layout the view based on the allowed width.
            var heightNeeded = (widthAllowed - panelSpacing - huePanelWidth).toInt()
            if (showAlphaPanel) {
                widthNeeded -= (panelSpacing + alphaPanelHeight).toInt()
                heightNeeded += (panelSpacing + alphaPanelHeight).toInt()
            }
            if (widthNeeded <= widthAllowed) {
                finalWidth = widthNeeded
                finalHeight = heightAllowed
            } else if (heightNeeded <= heightAllowed) {
                finalHeight = heightNeeded
                finalWidth = widthAllowed
            }
        }
        // Log.d("value-picker-view", "Final Size: $finalWidth x $finalHeight")
        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        drawingRect = RectF()
        drawingRect!!.left = (drawingOffset + paddingLeft).toFloat()
        drawingRect!!.right = (w - drawingOffset - paddingRight).toFloat()
        drawingRect!!.top = (drawingOffset + paddingTop).toFloat()
        drawingRect!!.bottom = (h - drawingOffset - paddingBottom).toFloat()
        // The need to be recreated because they depend on the size of the view.
        valShader = null
        satShader = null
        hueShader = null
        alphaShader = null
        setUpSatValRect()
        setUpHueRect()
        setUpAlphaRect()
    }

    private fun setUpSatValRect() {
        // Calculate the size for the big value rectangle.
        val dRect = drawingRect
        val left = dRect!!.left + BORDER_WIDTH_PX
        val top = dRect.top + BORDER_WIDTH_PX
        var bottom = dRect.bottom - BORDER_WIDTH_PX
        val right = dRect.right - BORDER_WIDTH_PX - panelSpacing - huePanelWidth
        if (showAlphaPanel) {
            bottom -= alphaPanelHeight + panelSpacing
        }
        satValRect = RectF(left, top, right, bottom)
    }

    private fun setUpHueRect() {
        // Calculate the size for the hue slider on the left.
        val dRect = drawingRect
        val left = dRect!!.right - huePanelWidth + BORDER_WIDTH_PX
        val top = dRect.top + BORDER_WIDTH_PX
        val bottom = dRect.bottom - BORDER_WIDTH_PX - (if (showAlphaPanel) panelSpacing + alphaPanelHeight else 0f)
        val right = dRect.right - BORDER_WIDTH_PX
        hueRect = RectF(left, top, right, bottom)
    }

    private fun setUpAlphaRect() {
        if (!showAlphaPanel) {
            return
        }
        val dRect = drawingRect
        val left = dRect!!.left + BORDER_WIDTH_PX
        val top = dRect.bottom - alphaPanelHeight + BORDER_WIDTH_PX
        val bottom = dRect.bottom - BORDER_WIDTH_PX
        val right = dRect.right - BORDER_WIDTH_PX
        alphaRect = RectF(left, top, right, bottom)
        alphaPattern = AlphaPatternDrawable((5 * density).toInt())
        alphaPattern!!.setBounds(
            Math.round(alphaRect!!.left), Math.round(alphaRect!!.top), Math.round(alphaRect!!.right), Math.round(
                alphaRect!!.bottom
            )
        )
    }

    /**
     * Set a OnColorChangedListener to get notified when the value selected by the user has changed.
     *
     * @param listener change listener
     */
    fun setOnColorChangedListener(listener: OnColorChangedListener?) {
        this.listener = listener
    }

    /**
     * The current value this view is showing.
     */
    var color: Int
        get() = Color.HSVToColor(alpha, floatArrayOf(hue, sat, value))
        set(color) {
            setColor(color, false)
        }

    /**
     * Set the value this view should show.
     *
     * @param color    The value that should be selected.
     * @param callback If you want to get a callback to your OnColorChangedListener.
     */
    fun setColor(color: Int, callback: Boolean) {
        val alpha0 = Color.alpha(color)
        val red = Color.red(color)
        val blue = Color.blue(color)
        val green = Color.green(color)
        val hsv = FloatArray(3)
        Color.RGBToHSV(red, green, blue, hsv)
        alpha = alpha0
        hue = hsv[0]
        sat = hsv[1]
        value = hsv[2]
        if (callback && listener != null) {
            listener!!.onColorChanged(Color.HSVToColor(alpha, floatArrayOf(hue, sat, value)))
        }
        invalidate()
    }

    /**
     * Get the drawing offset of the value picker view. The drawing offset is the distance from the side of a panel to the side of the view minus the padding.
     * Useful if you want to have your own panel below showing the currently selected value and want to align it perfectly.
     *
     * @return The offset in pixels.
     */
    fun getDrawingOffset(): Float {
        return drawingOffset.toFloat()
    }

    /**
     * Set if the user is allowed to adjust the alpha panel. Default is false. If it is set to false no alpha will be set.
     *
     * @param visible true if slider is to be visible
     */
    fun setAlphaSliderVisible(visible: Boolean) {
        if (showAlphaPanel != visible) {
            showAlphaPanel = visible
            // Reset all shader to force a recreation. Otherwise they will not look right after the size of the view has changed.
            valShader = null
            satShader = null
            hueShader = null
            alphaShader = null
            requestLayout()
        }
    }

    /**
     * Set the value of the tracker slider on the hue and alpha panel.
     *
     * @param color tracker value
     */
    fun setSliderTrackerColor(color: Int) {
        sliderTrackerColor = color
        hueAlphaTrackerPaint!!.color = sliderTrackerColor
        invalidate()
    }

    /**
     * Get value of the tracker slider on the hue and alpha panel.
     *
     * @return slider tracker value
     */
    fun getSliderTrackerColor(): Int {
        return sliderTrackerColor
    }

    /**
     * Set the value of the border surrounding all panels.
     *
     * @param color border value
     */
    fun setBorderColor(color: Int) {
        borderColor = color
        invalidate()
    }

    // /**
    //  * Get the value of the border surrounding all panels.
    //  */
    // fun getBorderColor(): Int {
    //     return borderColor
    // }

    /**
     * Set the text that should be shown in the alpha slider. Set to null to disable text.
     *
     * @param stringRes string resource id.
     */
    fun setAlphaSliderText(@StringRes stringRes: Int) {
        val text = context.getString(stringRes)
        setAlphaSliderText(text)
    }

    /**
     * Set the text that should be shown in the alpha slider. Set to null to disable text.
     *
     * @param text Text that should be shown.
     */
    fun setAlphaSliderText(text: String?) {
        alphaSliderText = text
        invalidate()
    }

    private class BitmapCache {

        var canvas: Canvas? = null
        var bitmap: Bitmap? = null
        var value: Float = 0f
    }

    companion object {

        // I N D I C E S

        private const val PANEL_SAT_VAL = 0
        private const val PANEL_HUE = 1
        private const val PANEL_ALPHA = 2

        // D I M E N S I O N S

        /**
         * The width in pixels of the border surrounding all value panels.
         */
        private const val BORDER_WIDTH_PX = 1f

        /**
         * Density
         */
        private var density = 1f

        private fun buildHueColorArray(): IntArray {
            val hue = IntArray(361)

            var count = 0
            var i = hue.size - 1
            while (i >= 0) {
                hue[count] = Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
                i--
                count++
            }

            return hue
        }
    }
}
