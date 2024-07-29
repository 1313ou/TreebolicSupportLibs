/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Abstract spinner spinnerwheel view. This class should be subclassed.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
abstract class AbstractWheelView (context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int) : AbstractWheel(context, attrs, defStyle) {

    /**
     * The alpha of the selector spinnerwheel when it is dimmed.
     */
    @JvmField
    protected var itemsDimmedAlpha: Int = 0

    /**
     * The alpha of separators spinnerwheel when they are shown.
     */
    private var selectionDividerActiveAlpha: Int = 0

    /**
     * The alpha of separators when they are is dimmed.
     */
    private var selectionDividerDimmedAlpha: Int = 0

    /**
     * The tint of separators.
     */
    private var selectionDividerTint: Int = 0

    /**
     * Top and bottom items offset
     */
    @JvmField
    protected var itemOffsetPercent: Int = 0

    /**
     * Left and right padding value
     */
    @JvmField
    protected var itemsPadding: Int = 0

    /**
     * Divider for showing item to be selected while scrolling
     */
    @JvmField
    protected var selectionDivider: Drawable? = null

    /**
     * The [android.graphics.Paint] for drawing the selector.
     */
    @JvmField
    protected var selectorWheelPaint: Paint? = null

    /**
     * The [android.graphics.Paint] for drawing the separators.
     */
    @JvmField
    protected var separatorsPaint: Paint? = null

    /**
     * Animator for dimming the selector spinnerwheel.
     */
    private var dimSelectorWheelAnimator: Animator? = null

    /**
     * Animator for dimming the separator.
     */
    private var dimSeparatorsAnimator: Animator? = null

    @JvmField
    protected var spinBitmap: Bitmap? = null

    @JvmField
    protected var separatorsBitmap: Bitmap? = null

    // I N I T / S E T

    override fun initAttributes(context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int) {
        super.initAttributes(context, attrs, defStyle)

        // try (final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AbstractWheelView, defStyle, 0))
        var array: TypedArray? = null
        try {
            array = context.obtainStyledAttributes(attrs, R.styleable.AbstractWheelView, defStyle, 0)
            itemsDimmedAlpha = array.getInt(R.styleable.AbstractWheelView_itemsDimmedAlpha, DEF_ITEMS_DIMMED_ALPHA)
            selectionDividerActiveAlpha = array.getInt(R.styleable.AbstractWheelView_selectionDividerActiveAlpha, DEF_SELECTION_DIVIDER_ACTIVE_ALPHA)
            selectionDividerDimmedAlpha = array.getInt(R.styleable.AbstractWheelView_selectionDividerDimmedAlpha, DEF_SELECTION_DIVIDER_DIMMED_ALPHA)
            selectionDividerTint = array.getInt(R.styleable.AbstractWheelView_selectionDividerTint, DEF_SELECTION_DIVIDER_TINT)
            itemOffsetPercent = array.getInt(R.styleable.AbstractWheelView_itemOffsetPercent, DEF_ITEM_OFFSET_PERCENT)
            itemsPadding = array.getDimensionPixelSize(R.styleable.AbstractWheelView_itemsPadding, DEF_ITEM_PADDING)
            selectionDivider = array.getDrawable(R.styleable.AbstractWheelView_selectionDivider)
            //selectionDivider.setTint(selectionDividerTint)
        } finally {
            array?.recycle()
        }
    }

    override fun initData(context: Context) {
        super.initData(context)

        // creating animators
        dimSelectorWheelAnimator = ObjectAnimator.ofFloat(this, PROPERTY_SELECTOR_PAINT_COEFF, 1f, 0f)

        dimSeparatorsAnimator = ObjectAnimator.ofInt(this, PROPERTY_SEPARATORS_PAINT_ALPHA, selectionDividerActiveAlpha, selectionDividerDimmedAlpha)

        // creating paints
        separatorsPaint = Paint()
        separatorsPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
        separatorsPaint!!.alpha = selectionDividerDimmedAlpha

        selectorWheelPaint = Paint()
        selectorWheelPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))

        // drawable tint
        checkNotNull(selectionDivider)
        DrawableCompat.setTint(DrawableCompat.wrap(selectionDivider!!), selectionDividerTint)
    }

    /**
     * Recreates assets (like bitmaps) when layout size has been changed
     *
     * @param width  New spinnerwheel width
     * @param height New spinnerwheel height
     */
    override fun recreateAssets(width: Int, height: Int) {
        spinBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        separatorsBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        setSelectorPaintCoeff(0f)
    }

    /**
     * Sets the `alpha` of the [Paint] for drawing separators spinnerwheel.
     *
     * @param alpha alpha value from 0 to 255
     */
    private fun setSeparatorsPaintAlpha(alpha: Int) {
        separatorsPaint!!.alpha = alpha
        invalidate()
    }

    /**
     * Sets the `coeff` of the [Paint] for drawing the selector spinnerwheel.
     *
     * @param coeff Coefficient from 0 (selector is passive) to 1 (selector is active)
     */
    abstract fun setSelectorPaintCoeff(coeff: Float)

    fun setSelectionDivider(divider: Drawable?) {
        selectionDivider = divider
        //selectionDivider.setTint(selectionDividerTint)
    }

    // S C R O L L E R   E V E N T S

    override fun onScrollTouched() {
        dimSelectorWheelAnimator!!.cancel()
        dimSeparatorsAnimator!!.cancel()
        setSelectorPaintCoeff(1f)
        setSeparatorsPaintAlpha(selectionDividerActiveAlpha)
    }

    override fun onScrollTouchedUp() {
        super.onScrollTouchedUp()
        fadeSelectorWheel(750)
        lightSeparators(750)
    }

    override fun onScrollFinished() {
        fadeSelectorWheel(500)
        lightSeparators(500)
    }

    // A N I M A T I O N

    /**
     * Fade the selector spinnerwheel via an animation.
     *
     * @param animationDuration The duration of the animation.
     */
    private fun fadeSelectorWheel(animationDuration: Long) {
        dimSelectorWheelAnimator!!.setDuration(animationDuration)
        dimSelectorWheelAnimator!!.start()
    }

    /**
     * Fade the selector spinnerwheel via an animation.
     *
     * @param animationDuration The duration of the animation.
     */
    private fun lightSeparators(animationDuration: Long) {
        dimSeparatorsAnimator!!.setDuration(animationDuration)
        dimSeparatorsAnimator!!.start()
    }

    // L A Y O U T   M E A S U R I N G

    /**
     * Perform layout measurements
     */
    protected abstract fun measureLayout()

    // D R A W I N G

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (viewAdapter != null && viewAdapter!!.itemsCount > 0) {
            if (rebuildItems()) {
                measureLayout()
            }
            doItemsLayout()
            drawItems(canvas)
        }
    }

    /**
     * Draws items on specified canvas
     *
     * @param canvas the canvas for drawing
     */
    protected abstract fun drawItems(canvas: Canvas)

    companion object {

        // D E F A U L T   V A L U E S

        const val DEF_ITEMS_DIMMED_ALPHA: Int = 50 // 60 in ICS

        const val DEF_SELECTION_DIVIDER_ACTIVE_ALPHA: Int = 70

        const val DEF_SELECTION_DIVIDER_DIMMED_ALPHA: Int = 70

        const val DEF_ITEM_OFFSET_PERCENT: Int = 10

        const val DEF_ITEM_PADDING: Int = 10

        const val DEF_SELECTION_DIVIDER_SIZE: Int = 2

        const val DEF_SELECTION_DIVIDER_TINT: Int = 0

        /**
         * The property for setting the selector paint.
         */
        const val PROPERTY_SELECTOR_PAINT_COEFF: String = "selectorPaintCoeff"

        /**
         * The property for setting the separators paint.
         */
        const val PROPERTY_SEPARATORS_PAINT_ALPHA: String = "separatorsPaintAlpha"
    }
}
