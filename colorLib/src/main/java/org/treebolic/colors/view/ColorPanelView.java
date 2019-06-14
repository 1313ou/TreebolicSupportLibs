/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.colors.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.treebolic.colors.drawable.AlphaPatternDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class draws a panel which which will be filled with a value which can be set. It can be used to show the currently selected value which you will get
 * from the {@link ColorPickerView}.
 *
 * @author Daniel Nilsson
 */
public class ColorPanelView extends View
{
	/**
	 * The width in pixels of the border surrounding the value panel.
	 */
	private final static float BORDER_WIDTH_PX = 1;

	/**
	 * Density
	 */
	private static float mDensity = 1f;

	/**
	 * Border value
	 */
	private int mBorderColor = 0xff6E6E6E;

	/**
	 * Color
	 */
	private int mColor = 0xff000000;

	/**
	 * Back paint
	 */
	static private final Paint mBackPaint = new Paint();

	static
	{
		mBackPaint.setColor(Color.WHITE);
	}

	/**
	 * Draw paint
	 */
	static private final Paint mDrawPaint = new Paint();

	static
	{
		mDrawPaint.setColor(Color.GRAY);
	}

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
		this.mColorPaint = new Paint();
		this.mBorderPaint = new Paint();
		ColorPanelView.mDensity = getContext().getResources().getDisplayMetrics().density;
	}

	@Override
	protected void onDraw(@NonNull final Canvas canvas)
	{
		// border
		//noinspection ConstantConditions
		if (ColorPanelView.BORDER_WIDTH_PX > 0)
		{
			this.mBorderPaint.setColor(this.mBorderColor);
			canvas.drawRect(this.mDrawingRect, this.mBorderPaint);
		}

		// crossed
		if (this.mIsCrossed)
		{
			canvas.drawRect(this.mColorRect, mBackPaint);

			canvas.drawLine(this.mDrawingRect.left, this.mDrawingRect.top, this.mDrawingRect.right, this.mDrawingRect.bottom, mDrawPaint);
			canvas.drawLine(this.mDrawingRect.right, this.mDrawingRect.top, this.mDrawingRect.left, this.mDrawingRect.bottom, mDrawPaint);
			return;
		}

		// pattern
		if (this.mAlphaPattern != null)
		{
			this.mAlphaPattern.draw(canvas);
		}

		// value
		if (!this.mIsNull)
		{
			this.mColorPaint.setColor(this.mColor);
			final RectF rect = this.mColorRect;
			canvas.drawRect(rect, this.mColorPaint);
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
	protected void onSizeChanged(final int w, final int h, final int oldW, final int oldH)
	{
		super.onSizeChanged(w, h, oldW, oldH);

		this.mDrawingRect = new RectF();
		this.mDrawingRect.left = getPaddingLeft();
		this.mDrawingRect.right = w - getPaddingRight();
		this.mDrawingRect.top = getPaddingTop();
		this.mDrawingRect.bottom = h - getPaddingBottom();
		setUpColorRect();
	}

	/**
	 * Set up value rectangle
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
	public void setValue(@Nullable final Integer color)
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
	 * Set the value that should be shown by this view.
	 *
	 * @param color value
	 */
	public void setColor(final int color)
	{
		this.mIsNull = false;
		this.mColor = color;
		invalidate();
	}

	/**
	 * Get the value currently shown by this view.
	 *
	 * @return value
	 */
	public int getColor()
	{
		return this.mColor;
	}

	/**
	 * Set the value of the border surrounding the panel.
	 *
	 * @param color border value
	 */
	public void setBorderColor(@SuppressWarnings("SameParameterValue") final int color)
	{
		this.mBorderColor = color;
		invalidate();
	}

	/**
	 * Get the value of the border surrounding the panel.
	 *
	 * @return border value
	 */
	public int getBorderColor()
	{
		return this.mBorderColor;
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
}
