/*
 * android-spinnerwheel
 * https://github.com/ai212983/android-spinnerwheel
 *
 * based on
 *
 * Android Wheel Control.
 * https://code.google.com/p/android-wheel/
 *
 * Copyright 2011 Yuri Kanivets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.treebolic.wheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Abstract spinner spinnerwheel view. This class should be subclassed.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public abstract class AbstractWheelView extends AbstractWheel
{
	// private final String TAG = AbstractWheelView.class.getName() + " #" + (++itemID);
	// private static int itemID = -1;

	// ----------------------------------
	// Default properties values
	// ----------------------------------

	protected static final int DEF_ITEMS_DIMMED_ALPHA = 50; // 60 in ICS

	protected static final int DEF_SELECTION_DIVIDER_ACTIVE_ALPHA = 70;

	protected static final int DEF_SELECTION_DIVIDER_DIMMED_ALPHA = 70;

	protected static final int DEF_ITEM_OFFSET_PERCENT = 10;

	protected static final int DEF_ITEM_PADDING = 10;

	protected static final int DEF_SELECTION_DIVIDER_SIZE = 2;

	protected static final int DEF_SELECTION_DIVIDER_TINT = 0;

	// ----------------------------------
	// Class properties
	// ----------------------------------

	// configurable properties

	/** The alpha of the selector spinnerwheel when it is dimmed. */
	protected int mItemsDimmedAlpha;

	/** The alpha of separators spinnerwheel when they are shown. */
	protected int mSelectionDividerActiveAlpha;

	/** The alpha of separators when they are is dimmed. */
	protected int mSelectionDividerDimmedAlpha;

	/** The tint of separators. */
	protected int mSelectionDividerTint;

	/** Top and bottom items offset */
	protected int mItemOffsetPercent;

	/** Left and right padding value */
	protected int mItemsPadding;

	/** Divider for showing item to be selected while scrolling */
	protected Drawable mSelectionDivider;

	// the rest

	/**
	 * The {@link android.graphics.Paint} for drawing the selector.
	 */
	protected Paint mSelectorWheelPaint;

	/**
	 * The {@link android.graphics.Paint} for drawing the separators.
	 */
	protected Paint mSeparatorsPaint;

	/**
	 * {@link com.nineoldandroids.animation.Animator} for dimming the selector spinnerwheel.
	 */
	protected Animator mDimSelectorWheelAnimator;

	/**
	 * {@link com.nineoldandroids.animation.Animator} for dimming the selector spinnerwheel.
	 */
	protected Animator mDimSeparatorsAnimator;

	/**
	 * The property for setting the selector paint.
	 */
	protected static final String PROPERTY_SELECTOR_PAINT_COEFF = "selectorPaintCoeff";

	/**
	 * The property for setting the separators paint.
	 */
	protected static final String PROPERTY_SEPARATORS_PAINT_ALPHA = "separatorsPaintAlpha";

	protected Bitmap mSpinBitmap;
	protected Bitmap mSeparatorsBitmap;

	// --------------------------------------------------------------------------
	//
	// Constructor
	//
	// --------------------------------------------------------------------------

	public AbstractWheelView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	// --------------------------------------------------------------------------
	//
	// Initiating assets and setters for paints
	//
	// --------------------------------------------------------------------------

	@Override
	protected void initAttributes(AttributeSet attrs, int defStyle)
	{
		super.initAttributes(attrs, defStyle);

		final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.AbstractWheelView, defStyle, 0);
		this.mItemsDimmedAlpha = array.getInt(R.styleable.AbstractWheelView_itemsDimmedAlpha, DEF_ITEMS_DIMMED_ALPHA);
		this.mSelectionDividerActiveAlpha = array.getInt(R.styleable.AbstractWheelView_selectionDividerActiveAlpha, DEF_SELECTION_DIVIDER_ACTIVE_ALPHA);
		this.mSelectionDividerDimmedAlpha = array.getInt(R.styleable.AbstractWheelView_selectionDividerDimmedAlpha, DEF_SELECTION_DIVIDER_DIMMED_ALPHA);
		this.mSelectionDividerTint = array.getInt(R.styleable.AbstractWheelView_selectionDividerTint, DEF_SELECTION_DIVIDER_TINT);
		this.mItemOffsetPercent = array.getInt(R.styleable.AbstractWheelView_itemOffsetPercent, DEF_ITEM_OFFSET_PERCENT);
		this.mItemsPadding = array.getDimensionPixelSize(R.styleable.AbstractWheelView_itemsPadding, DEF_ITEM_PADDING);
		this.mSelectionDivider = array.getDrawable(R.styleable.AbstractWheelView_selectionDivider);
		array.recycle();
		//this.mSelectionDivider.setTint(this.mSelectionDividerTint);
	}

	@Override
	protected void initData(Context context)
	{
		super.initData(context);

		// creating animators
		this.mDimSelectorWheelAnimator = ObjectAnimator.ofFloat(this, PROPERTY_SELECTOR_PAINT_COEFF, 1, 0);

		this.mDimSeparatorsAnimator = ObjectAnimator.ofInt(this, PROPERTY_SEPARATORS_PAINT_ALPHA, this.mSelectionDividerActiveAlpha,
				this.mSelectionDividerDimmedAlpha);

		// creating paints
		this.mSeparatorsPaint = new Paint();
		this.mSeparatorsPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		this.mSeparatorsPaint.setAlpha(this.mSelectionDividerDimmedAlpha);

		this.mSelectorWheelPaint = new Paint();
		this.mSelectorWheelPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		// drawable tint
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			this.mSelectionDivider.setTint(this.mSelectionDividerTint);
		}
		else
		{
			DrawableCompat.setTint(DrawableCompat.wrap(this.mSelectionDivider), this.mSelectionDividerTint);
		}
	}

	/**
	 * Recreates assets (like bitmaps) when layout size has been changed
	 *
	 * @param width
	 *            New spinnerwheel width
	 * @param height
	 *            New spinnerwheel height
	 */
	@Override
	protected void recreateAssets(int width, int height)
	{
		this.mSpinBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		this.mSeparatorsBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		setSelectorPaintCoeff(0);
	}

	/**
	 * Sets the <code>alpha</code> of the {@link Paint} for drawing separators spinnerwheel.
	 * 
	 * @param alpha
	 *            alpha value from 0 to 255
	 */
	public void setSeparatorsPaintAlpha(int alpha)
	{
		this.mSeparatorsPaint.setAlpha(alpha);
		invalidate();
	}

	/**
	 * Sets the <code>coeff</code> of the {@link Paint} for drawing the selector spinnerwheel.
	 *
	 * @param coeff
	 *            Coefficient from 0 (selector is passive) to 1 (selector is active)
	 */
	abstract public void setSelectorPaintCoeff(float coeff);

	public void setSelectionDivider(Drawable selectionDivider)
	{
		this.mSelectionDivider = selectionDivider;
		//this.mSelectionDivider.setTint(this.mSelectionDividerTint);
	}

	// --------------------------------------------------------------------------
	//
	// Processing scroller events
	//
	// --------------------------------------------------------------------------

	@Override
	protected void onScrollTouched()
	{
		this.mDimSelectorWheelAnimator.cancel();
		this.mDimSeparatorsAnimator.cancel();
		setSelectorPaintCoeff(1);
		setSeparatorsPaintAlpha(this.mSelectionDividerActiveAlpha);
	}

	@Override
	protected void onScrollTouchedUp()
	{
		super.onScrollTouchedUp();
		fadeSelectorWheel(750);
		lightSeparators(750);
	}

	@Override
	protected void onScrollFinished()
	{
		fadeSelectorWheel(500);
		lightSeparators(500);
	}

	// ----------------------------------
	// Animating components
	// ----------------------------------

	/**
	 * Fade the selector spinnerwheel via an animation.
	 *
	 * @param animationDuration
	 *            The duration of the animation.
	 */
	private void fadeSelectorWheel(long animationDuration)
	{
		this.mDimSelectorWheelAnimator.setDuration(animationDuration);
		this.mDimSelectorWheelAnimator.start();
	}

	/**
	 * Fade the selector spinnerwheel via an animation.
	 *
	 * @param animationDuration
	 *            The duration of the animation.
	 */
	private void lightSeparators(long animationDuration)
	{
		this.mDimSeparatorsAnimator.setDuration(animationDuration);
		this.mDimSeparatorsAnimator.start();
	}

	// --------------------------------------------------------------------------
	//
	// Layout measuring
	//
	// --------------------------------------------------------------------------

	/**
	 * Perform layout measurements
	 */
	abstract protected void measureLayout();

	// --------------------------------------------------------------------------
	//
	// Drawing stuff
	//
	// --------------------------------------------------------------------------

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (this.mViewAdapter != null && this.mViewAdapter.getItemsCount() > 0)
		{
			if (rebuildItems())
			{
				measureLayout();
			}
			doItemsLayout();
			drawItems(canvas);
		}
	}

	/**
	 * Draws items on specified canvas
	 *
	 * @param canvas
	 *            the canvas for drawing
	 */
	abstract protected void drawItems(Canvas canvas);
}
