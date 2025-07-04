/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.colors.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import org.treebolic.colors.drawable.AlphaPatternDrawable
import kotlin.math.roundToInt

/**
 * This class draws a panel which which will be filled with a value which can be set. It can be used to show the currently selected value which you will get
 * from the [ColorPickerView].
 *
 * @param context  context
 * @param attrs    attributes
 * @param defStyle style
 *
 * @author Daniel Nilsson
 * @author Bernard
 */
class ColorPanelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    /**
     * Border value
     */
    private var borderColor = -0x919192

    /**
     * Color, the value that should be shown by this view.
     */
    var color: Int = -0x1000000
        set(newColor) {
            isNull = false
            field = newColor
            invalidate()
        }

    /**
     * Border paint
     */
    private lateinit var borderPaint: Paint

    /**
     * Color paint
     */
    private lateinit var colorPaint: Paint

    /**
     * Drawing rect
     */
    private var drawingRect: RectF? = null

    /**
     * Color rect
     */
    private var colorRect: RectF? = null

    /**
     * Alpha pattern
     */
    private var alphaPattern: AlphaPatternDrawable? = null

    /**
     * IsNull
     */
    private var isNull = false

    /**
     * IsIllegal
     */
    var isCrossed: Boolean = false

    init {
        init(context)
    }

    /**
     * Common init
     *
     * @param context context
     */
    private fun init(context: Context) {
        colorPaint = Paint()
        borderPaint = Paint()
        density = context.resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas) {

        // border
        if (BORDER_WIDTH_PX > 0) {
            borderPaint.color = borderColor
            canvas.drawRect(drawingRect!!, borderPaint)
        }

        // crossed
        if (isCrossed) {
            canvas.drawRect(colorRect!!, BACK_PAINT)

            canvas.drawLine(drawingRect!!.left, drawingRect!!.top, drawingRect!!.right, drawingRect!!.bottom, DRAW_PAINT)
            canvas.drawLine(drawingRect!!.right, drawingRect!!.top, drawingRect!!.left, drawingRect!!.bottom, DRAW_PAINT)
            return
        }

        // pattern
        if (alphaPattern != null) {
            alphaPattern!!.draw(canvas)
        }

        // value
        if (!isNull) {
            colorPaint.color = color
            val rect = colorRect
            canvas.drawRect(rect!!, colorPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        drawingRect = RectF()
        drawingRect!!.left = paddingLeft.toFloat()
        drawingRect!!.right = (w - paddingRight).toFloat()
        drawingRect!!.top = paddingTop.toFloat()
        drawingRect!!.bottom = (h - paddingBottom).toFloat()
        setUpColorRect()
    }

    /**
     * Set up value rectangle
     */
    private fun setUpColorRect() {
        val dRect = drawingRect
        val left = dRect!!.left + BORDER_WIDTH_PX
        val top = dRect.top + BORDER_WIDTH_PX
        val bottom = dRect.bottom - BORDER_WIDTH_PX
        val right = dRect.right - BORDER_WIDTH_PX

        colorRect = RectF(left, top, right, bottom)
        alphaPattern = AlphaPatternDrawable((5 * density).toInt())
        alphaPattern!!.setBounds(colorRect!!.left.roundToInt(), colorRect!!.top.roundToInt(), colorRect!!.right.roundToInt(), colorRect!!.bottom.roundToInt())
        isCrossed = false
    }

    /**
     * Set value
     *
     * @param newValue may be null
     */
    fun setValue(newValue: Int?) {
        if (newValue == null) {
            isNull = true
            color = 0x00ffffff
            invalidate()
            return
        }
        color = newValue
    }

    /**
     * Set the value of the border surrounding the panel.
     *
     * @param color border value
     */
    fun setBorderColor(color: Int) {
        borderColor = color
        invalidate()
    }

    companion object {

        /**
         * The width in pixels of the border surrounding the value panel.
         */
        private const val BORDER_WIDTH_PX = 1f

        /**
         * Density
         */
        private var density = 1f

        /**
         * Back paint
         */
        private val BACK_PAINT = Paint().apply {
            color = Color.WHITE
        }

        /**
         * Draw paint
         */
        private val DRAW_PAINT = Paint().apply {
            color = Color.GRAY
        }
    }
}
