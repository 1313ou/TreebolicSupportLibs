/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.core.content.res.use
import org.treebolic.wheel.WheelScroller.ScrollingListener
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Spinner wheel horizontal view.
 *
 * @param context  the application environment.
 * @param attrs    a collection of attributes.
 * @param defStyle The default style to apply to this view.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
class WheelHorizontalView

// C O N S T R U C T O R S

/**
 * Create a new wheel horizontal view.
 *
 * @param context  the application environment.
 * @param attrs    a collection of attributes.
 * @param defStyle The default style to apply to this view.
 */
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyle: Int = R.attr.abstractWheelViewStyle) : AbstractWheelView(context, attrs, defStyle) {

    /**
     * The width of the selection divider.
     */
    private var selectionDividerWidth: Int = 0

    /** Item width */
    private var itemWidth = 0

    /** Initiating assets and setter for selector paint */
    override fun initAttributes(context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int) {
        super.initAttributes(context, attrs, defStyle)

        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.WheelHorizontalView, defStyle, 0).use {
                selectionDividerWidth = it.getDimensionPixelSize(R.styleable.WheelHorizontalView_selectionDividerWidth, DEF_SELECTION_DIVIDER_SIZE)
            }
        }
    }

    fun setSelectionDividerWidth(width: Int) {
        selectionDividerWidth = width
    }

    override fun setSelectorPaintCoeff(coeff: Float) {
        if (itemsDimmedAlpha >= 100) {
            return
        }

        val shader: LinearGradient

        val w = measuredWidth
        val iw = itemDimension
        val p1 = (1 - iw / w.toFloat()) / 2
        val p2 = (1 + iw / w.toFloat()) / 2
        val z = itemsDimmedAlpha * (1 - coeff)
        val c1f = z + 255 * coeff

        if (visibleItems == 2) {
            val positions = floatArrayOf(0f, p1, p1, p2, p2, 1f)

            val c1 = c1f.roundToInt() shl 24
            val c2 = z.roundToInt() shl 24
            val colors = intArrayOf(c2, c1, -0x1000000, -0x1000000, c1, c2)
            shader = LinearGradient(0f, 0f, w.toFloat(), 0f, colors, positions, Shader.TileMode.CLAMP)
        } else {
            val p3 = (1 - iw * 3 / w.toFloat()) / 2
            val p4 = (1 + iw * 3 / w.toFloat()) / 2
            val positions = floatArrayOf(0f, p3, p3, p1, p1, p2, p2, p4, p4, 1f)

            val s = 255 * p3 / p1
            val c3f = s * coeff // here goes some optimized stuff
            val c2f = z + c3f

            val c1 = c1f.roundToInt() shl 24
            val c2 = c2f.roundToInt() shl 24
            val c3 = c3f.roundToInt() shl 24
            val colors = intArrayOf(c3, c3, c2, c1, -0x1000000, -0x1000000, c1, c2, c3, c3)

            shader = LinearGradient(0f, 0f, w.toFloat(), 0f, colors, positions, Shader.TileMode.CLAMP)
        }
        selectorWheelPaint!!.shader = shader
        invalidate()
    }

    // S C R O L L

    override fun createScroller(scrollingListener: ScrollingListener?): WheelScroller {
        return WheelHorizontalScroller(context, scrollingListener)
    }

    override fun getMotionEventPosition(event: MotionEvent): Float {
        return event.x
    }

    override val baseDimension: Int
        get() = width

    /**
     * Height of spinnerwheel item
     */
    override val itemDimension: Int
        get() {
            if (itemWidth != 0) {
                return itemWidth
            }

            if (itemsLayout != null && itemsLayout!!.getChildAt(0) != null) {
                itemWidth = itemsLayout!!.getChildAt(0).measuredWidth
                return itemWidth
            }

            return baseDimension / visibleItems
        }

    // D E B U G

    override fun onScrollTouchedUp() {
        super.onScrollTouchedUp()
        val cnt = itemsLayout!!.childCount
        var itm: View
        Log.e(TAG, " ----- layout: " + itemsLayout!!.measuredWidth + itemsLayout!!.measuredHeight)
        Log.e(TAG, " -------- dumping $cnt items")
        for (i in 0 until cnt) {
            itm = itemsLayout!!.getChildAt(i)
            Log.e(TAG, " item #" + i + ": " + itm.width + "x" + itm.height)
            itm.forceLayout() // forcing layout without re-rendering parent
        }
        Log.e(TAG, " ---------- dumping finished ")
    }

    // L A Y O U T   M E A S U R E M E N T

    /**
     * Creates item layouts if necessary
     */
    override fun createItemsLayout() {
        if (itemsLayout == null) {
            itemsLayout = LinearLayout(context)
            itemsLayout!!.orientation = LinearLayout.HORIZONTAL
        }
    }

    override fun doItemsLayout() {
        itemsLayout!!.layout(0, 0, measuredWidth, measuredHeight - 2 * itemsPadding)
    }

    override fun measureLayout() {
        itemsLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        // XXX: Locating bug
        itemsLayout!!.measure(MeasureSpec.makeMeasureSpec(width + itemDimension, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST))
    }

    // XXX: Most likely, measurements of itemsLayout or/and its children are done incorrectly.
    // Investigate and fix it
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        rebuildItems() // rebuilding before measuring

        val height = calculateLayoutHeight(heightSize, heightMode)

        var width: Int
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else {
            width = max((itemDimension * (visibleItems - itemOffsetPercent / 100)).toDouble(), suggestedMinimumWidth.toDouble()).toInt()

            if (widthMode == MeasureSpec.AT_MOST) {
                width = min(width.toDouble(), widthSize.toDouble()).toInt()
            }
        }
        setMeasuredDimension(width, height)
    }

    /**
     * Calculates control height and creates text layouts
     *
     * @param heightSize the input layout height
     * @param mode       the layout mode
     * @return the calculated control height
     */
    private fun calculateLayoutHeight(heightSize: Int, mode: Int): Int {
        itemsLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        itemsLayout!!.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED))
        var height = itemsLayout!!.measuredHeight

        if (mode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            height += 2 * itemsPadding

            // Check against our minimum width
            height = max(height.toDouble(), suggestedMinimumHeight.toDouble()).toInt()

            if (mode == MeasureSpec.AT_MOST && heightSize < height) {
                height = heightSize
            }
        }
        // forcing recalculating
        itemsLayout!!.measure( // MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(400, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - 2 * itemsPadding, MeasureSpec.EXACTLY)
        )

        return height
    }

    // D R A W I N G

    override fun drawItems(canvas: Canvas) {
        canvas.save()
        val w = measuredWidth
        val h = measuredHeight
        val iw = itemDimension

        // resetting intermediate bitmap and recreating canvases
        spinBitmap!!.eraseColor(0)
        val c = Canvas(spinBitmap!!)
        val cSpin = Canvas(spinBitmap!!)

        val left = (currentItemIdx - firstItemIdx) * iw + (iw - width) / 2
        c.translate((-left + scrollingOffset).toFloat(), itemsPadding.toFloat())
        itemsLayout!!.draw(c)

        separatorsBitmap!!.eraseColor(0)
        val cSeparators = Canvas(separatorsBitmap!!)

        if (selectionDivider != null) {
            // draw the top divider
            val leftOfLeftDivider = (width - iw - selectionDividerWidth) / 2
            val rightOfLeftDivider = leftOfLeftDivider + selectionDividerWidth
            cSeparators.save()
            // On Gingerbread setBounds() is ignored resulting in an ugly visual bug.
            cSeparators.clipRect(leftOfLeftDivider, 0, rightOfLeftDivider, h)
            selectionDivider!!.setBounds(leftOfLeftDivider, 0, rightOfLeftDivider, h)
            selectionDivider!!.draw(cSeparators)
            cSeparators.restore()

            cSeparators.save()
            // draw the bottom divider
            val leftOfRightDivider = leftOfLeftDivider + iw
            val rightOfRightDivider = rightOfLeftDivider + iw
            // On Gingerbread setBounds() is ignored resulting in an ugly visual bug.
            cSeparators.clipRect(leftOfRightDivider, 0, rightOfRightDivider, h)
            selectionDivider!!.setBounds(leftOfRightDivider, 0, rightOfRightDivider, h)
            selectionDivider!!.draw(cSeparators)
            cSeparators.restore()
        }

        cSpin.drawRect(0f, 0f, w.toFloat(), h.toFloat(), selectorWheelPaint!!)
        cSeparators.drawRect(0f, 0f, w.toFloat(), h.toFloat(), separatorsPaint!!)

        canvas.drawBitmap(spinBitmap!!, 0f, 0f, null)
        canvas.drawBitmap(separatorsBitmap!!, 0f, 0f, null)
        canvas.restore()
    }

    companion object {

        private var itemID = -1

        private val TAG = WheelVerticalView::class.java.name + " #" + (++itemID)
    }
}
