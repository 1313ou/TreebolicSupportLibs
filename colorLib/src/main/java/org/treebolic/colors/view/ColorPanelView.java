/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.treebolic.colors.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.treebolic.colors.drawable.AlphaPatternDrawable;

/**
 * This class draws a panel which which will be filled with a color which can be set. It can be used to show the currently selected color which you will get
 * from the {@link ColorPickerView}.
 *
 * @author Daniel Nilsson
 */
public class ColorPanelView extends View
{
	/**
	 * The width in pixels of the border surrounding the color panel.
	 */
	private final static float BORDER_WIDTH_PX = 1;

	/**
	 * Density
	 */
	private static float mDensity = 1f;

	/**
	 * Border color
	 */
	private int mBorderColor = 0xff6E6E6E;

	/**
	 * Color
	 */
	private int mColor = 0xff000000;

	/**
	 * Border paint
	 */
	private Paint mBorderPaint;

	/**
	 * Color paint
	 */
	private Paint mColorPaint;

	/**
	 * Drawing rect
	 */
	private RectF mDrawingRect;

	/**
	 * Color rect
	 */
	private RectF mColorRect;

	/**
	 * Alpha pattern
	 */
	private AlphaPatternDrawable mAlphaPattern;

	/**
	 * IsNull
	 */
	private boolean mIsNull;

	/**
	 * IsIllegal
	 */
	private boolean mIsCrossed;

	/**
	 * Constructor
	 *
	 * @param context context
	 */
	public ColorPanelView(final Context context)
	{
		this(context, null);
	}

	/**
	 * Constructor
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	public ColorPanelView(final Context context, final AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 * Constructor
	 *
	 * @param context  context
	 * @param attrs    attributes
	 * @param defStyle style
	 */
	public ColorPanelView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Common init
	 */
	private void init()
	{
		this.mBorderPaint = new Paint();
		this.mColorPaint = new Paint();
		ColorPanelView.mDensity = getContext().getResources().getDisplayMetrics().density;
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		// border
		//noinspection ConstantConditions
		if (ColorPanelView.BORDER_WIDTH_PX > 0)
		{
			this.mBorderPaint.setColor(this.mBorderColor);
			canvas.drawRect(this.mDrawingRect, this.mBorderPaint);
		}

		// pattern
		if (this.mAlphaPattern != null)
		{
			this.mAlphaPattern.draw(canvas);
		}

		// color
		if (!this.mIsNull)
		{
			this.mColorPaint.setColor(this.mColor);
			final RectF rect = this.mColorRect;
			canvas.drawRect(rect, this.mColorPaint);
		}

		// illegal
		if (!this.mIsCrossed)
		{
			canvas.drawLine(this.mDrawingRect.left, this.mDrawingRect.top, this.mDrawingRect.right, this.mDrawingRect.bottom, this.mBorderPaint);
			canvas.drawLine(this.mDrawingRect.right, this.mDrawingRect.top, this.mDrawingRect.left, this.mDrawingRect.bottom, this.mBorderPaint);
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		this.mDrawingRect = new RectF();
		this.mDrawingRect.left = getPaddingLeft();
		this.mDrawingRect.right = w - getPaddingRight();
		this.mDrawingRect.top = getPaddingTop();
		this.mDrawingRect.bottom = h - getPaddingBottom();
		setUpColorRect();
	}

	/**
	 * Set up color rectangle
	 */
	private void setUpColorRect()
	{
		final RectF dRect = this.mDrawingRect;
		final float left = dRect.left + ColorPanelView.BORDER_WIDTH_PX;
		final float top = dRect.top + ColorPanelView.BORDER_WIDTH_PX;
		final float bottom = dRect.bottom - ColorPanelView.BORDER_WIDTH_PX;
		final float right = dRect.right - ColorPanelView.BORDER_WIDTH_PX;

		this.mColorRect = new RectF(left, top, right, bottom);
		this.mAlphaPattern = new AlphaPatternDrawable((int) (5 * ColorPanelView.mDensity));
		this.mAlphaPattern.setBounds(Math.round(this.mColorRect.left), Math.round(this.mColorRect.top), Math.round(this.mColorRect.right), Math.round(this.mColorRect.bottom));
		this.mIsCrossed = false;
	}

	/**
	 * Set value
	 *
	 * @param color may be null
	 */
	public void setValue(final Integer color)
	{
		if (color == null)
		{
			this.mIsNull = true;
			this.mColor = 0x00ffffff;
			invalidate();
			return;
		}
		setColor(color);
	}

	/**
	 * Set the color that should be shown by this view.
	 *
	 * @param color color
	 */
	public void setColor(final int color)
	{
		this.mIsNull = false;
		this.mColor = color;
		invalidate();
	}

	/**
	 * Set crossed flag
	 *
	 * @param isCrossed whether the display is crossed
	 */
	public void setCrossed(final boolean isCrossed)
	{
		this.mIsCrossed = isCrossed;
	}

	/**
	 * Get the color currently shown by this view.
	 *
	 * @return color
	 */
	public int getColor()
	{
		return this.mColor;
	}

	/**
	 * Set the color of the border surrounding the panel.
	 *
	 * @param color border color
	 */
	public void setBorderColor(final int color)
	{
		this.mBorderColor = color;
		invalidate();
	}

	/**
	 * Get the color of the border surrounding the panel.
	 *
	 * @return border color
	 */
	public int getBorderColor()
	{
		return this.mBorderColor;
	}
}
