/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import org.treebolic.wheel.WheelScroller.ScrollingListener
import kotlin.math.max
import kotlin.math.min

/**
 * Spinner wheel vertical view.
 *
 * @param context  the application environment.
 * @param attrs    a collection of attributes.
 * @param defStyle The default style to apply to this view.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 * @noinspection WeakerAccess
 */
open class WheelVerticalView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyle: Int = R.attr.abstractWheelViewStyle) : AbstractWheelView(context, attrs, defStyle) {

    /**
     * The height of the selection divider.
     */
    private var selectionDividerHeight: Int = 0

    /** Cached item height */
    private var itemHeight = 0

    /** Initiating assets and setter for selector paint */
    override fun initAttributes(context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int) {
        super.initAttributes(context, attrs, defStyle)

        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.WheelVerticalView, defStyle, 0).use {
                selectionDividerHeight = it.getDimensionPixelSize(R.styleable.WheelVerticalView_selectionDividerHeight, DEF_SELECTION_DIVIDER_SIZE)
            }
        }
    }

    override fun setSelectorPaintCoeff(coeff: Float) {
        val shader: LinearGradient

        val h = measuredHeight
        val ih = itemDimension
        val p1 = (1 - ih / h.toFloat()) / 2
        val p2 = (1 + ih / h.toFloat()) / 2
        val z = itemsDimmedAlpha * (1 - coeff)
        val c1f = z + 255 * coeff

        if (visibleItems == 2) {
            val c1 = Math.round(c1f) shl 24
            val c2 = Math.round(z) shl 24
            val colors = intArrayOf(c2, c1, -0x1000000, -0x1000000, c1, c2)
            val positions = floatArrayOf(0f, p1, p1, p2, p2, 1f)
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), colors, positions, Shader.TileMode.CLAMP)
        } else {
            val p3 = (1 - ih * 3 / h.toFloat()) / 2
            val p4 = (1 + ih * 3 / h.toFloat()) / 2

            val s = 255 * p3 / p1
            val c3f = s * coeff // here goes some optimized stuff
            val c2f = z + c3f

            val c1 = Math.round(c1f) shl 24
            val c2 = Math.round(c2f) shl 24
            val c3 = Math.round(c3f) shl 24

            val colors = intArrayOf(0, c3, c2, c1, -0x1000000, -0x1000000, c1, c2, c3, 0)
            val positions = floatArrayOf(0f, p3, p3, p1, p1, p2, p2, p4, p4, 1f)
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), colors, positions, Shader.TileMode.CLAMP)
        }
        selectorWheelPaint!!.setShader(shader)
        invalidate()
    }

    // S C R O L L

    override fun createScroller(scrollingListener: ScrollingListener?): WheelScroller {
        return WheelVerticalScroller(context, scrollingListener)
    }

    override fun getMotionEventPosition(event: MotionEvent): Float {
        return event.y
    }

    override val baseDimension: Int
        get() = height

    /**
     * Item height of the spinnerwheel
     */
    override val itemDimension: Int
        get() {
            if (itemHeight != 0) {
                return itemHeight
            }

            if (itemsLayout != null && itemsLayout!!.getChildAt(0) != null) {
                itemHeight = itemsLayout!!.getChildAt(0).measuredHeight
                return itemHeight
            }

            return baseDimension / visibleItems
        }

    // L A Y O U T   M E A S U R E M E N T

    /**
     * Creates item layout if necessary
     */
    override fun createItemsLayout() {
        if (itemsLayout == null) {
            itemsLayout = LinearLayout(context)
            itemsLayout!!.orientation = LinearLayout.VERTICAL
        }
    }

    override fun doItemsLayout() {
        itemsLayout!!.layout(0, 0, measuredWidth - 2 * itemsPadding, measuredHeight)
    }

    override fun measureLayout() {
        itemsLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        itemsLayout!!.measure(MeasureSpec.makeMeasureSpec(width - 2 * itemsPadding, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        rebuildItems() // rebuilding before measuring

        val width = calculateLayoutWidth(widthSize, widthMode)

        var height: Int
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            height = max((itemDimension * (visibleItems - itemOffsetPercent / 100)).toDouble(), suggestedMinimumHeight.toDouble()).toInt()

            if (heightMode == MeasureSpec.AT_MOST) {
                height = min(height.toDouble(), heightSize.toDouble()).toInt()
            }
        }
        setMeasuredDimension(width, height)
    }

    /**
     * Calculates control width
     *
     * @param widthSize the input layout width
     * @param mode      the layout mode
     * @return the calculated control width
     */
    private fun calculateLayoutWidth(widthSize: Int, mode: Int): Int {
        itemsLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        itemsLayout!!.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        var width = itemsLayout!!.measuredWidth

        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else {
            width += 2 * itemsPadding

            // Check against our minimum width
            width = max(width.toDouble(), suggestedMinimumWidth.toDouble()).toInt()

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize
            }
        }

        // forcing recalculating
        itemsLayout!!.measure(MeasureSpec.makeMeasureSpec(width - 2 * itemsPadding, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))

        return width
    }

    // D R A W I N G   I T E M S

    override fun drawItems(canvas: Canvas) {
        canvas.save()
        val w = measuredWidth
        val h = measuredHeight
        val ih = itemDimension

        // resetting intermediate bitmap and recreating canvases
        spinBitmap!!.eraseColor(0)
        val c = Canvas(spinBitmap!!)
        val cSpin = Canvas(spinBitmap!!)

        val top = (currentItemIdx - firstItemIdx) * ih + (ih - height) / 2
        c.translate(itemsPadding.toFloat(), (-top + scrollingOffset).toFloat())
        itemsLayout!!.draw(c)

        separatorsBitmap!!.eraseColor(0)
        val cSeparators = Canvas(separatorsBitmap!!)

        if (selectionDivider != null) {
            // draw the top divider
            val topOfTopDivider = (height - ih - selectionDividerHeight) / 2
            val bottomOfTopDivider = topOfTopDivider + selectionDividerHeight
            selectionDivider!!.setBounds(0, topOfTopDivider, w, bottomOfTopDivider)
            selectionDivider!!.draw(cSeparators)

            // draw the bottom divider
            val topOfBottomDivider = topOfTopDivider + ih
            val bottomOfBottomDivider = bottomOfTopDivider + ih
            selectionDivider!!.setBounds(0, topOfBottomDivider, w, bottomOfBottomDivider)
            selectionDivider!!.draw(cSeparators)
        }

        cSpin.drawRect(0f, 0f, w.toFloat(), h.toFloat(), selectorWheelPaint!!)
        cSeparators.drawRect(0f, 0f, w.toFloat(), h.toFloat(), separatorsPaint!!)

        canvas.drawBitmap(spinBitmap!!, 0f, 0f, null)
        canvas.drawBitmap(separatorsBitmap!!, 0f, 0f, null)
        canvas.restore()
    }
}
