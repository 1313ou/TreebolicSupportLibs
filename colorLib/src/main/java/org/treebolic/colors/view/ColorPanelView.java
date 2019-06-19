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
	private static float density = 1f;

	/**
	 * Border value
	 */
	private int borderColor = 0xff6E6E6E;

	/**
	 * Color
	 */
	private int color = 0xff000000;

	/**
	 * Back paint
	 */
	static private final Paint BACK_PAINT = new Paint();

	static
	{
		BACK_PAINT.setColor(Color.WHITE);
	}

	/**
	 * Draw paint
	 */
	static private final Paint DRAW_PAINT = new Paint();

	static
	{
		DRAW_PAINT.setColor(Color.GRAY);
	}

	/**
	 * Border paint
	 */
	private Paint borderPaint;

	/**
	 * Color paint
	 */
	private Paint colorPaint;

	/**
	 * Drawing rect
	 */
	private RectF drawingRect;

	/**
	 * Color rect
	 */
	private RectF colorRect;

	/**
	 * Alpha pattern
	 */
	private AlphaPatternDrawable alphaPattern;

	/**
	 * IsNull
	 */
	private boolean isNull;

	/**
	 * IsIllegal
	 */
	private boolean isCrossed;

	/**
	 * Constructor
	 *
	 * @param context context
	 */
	public ColorPanelView(@NonNull final Context context)
	{
		this(context, null);
	}

	/**
	 * Constructor
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	public ColorPanelView(@NonNull final Context context, final AttributeSet attrs)
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
	public ColorPanelView(@NonNull final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * Common init
	 *
	 * @param context context
	 */
	private void init(@NonNull final Context context)
	{
		this.colorPaint = new Paint();
		this.borderPaint = new Paint();
		ColorPanelView.density = context.getResources().getDisplayMetrics().density;
	}

	@Override
	protected void onDraw(@NonNull final Canvas canvas)
	{
		// border
		//noinspection ConstantConditions
		if (ColorPanelView.BORDER_WIDTH_PX > 0)
		{
			this.borderPaint.setColor(this.borderColor);
			canvas.drawRect(this.drawingRect, this.borderPaint);
		}

		// crossed
		if (this.isCrossed)
		{
			canvas.drawRect(this.colorRect, BACK_PAINT);

			canvas.drawLine(this.drawingRect.left, this.drawingRect.top, this.drawingRect.right, this.drawingRect.bottom, DRAW_PAINT);
			canvas.drawLine(this.drawingRect.right, this.drawingRect.top, this.drawingRect.left, this.drawingRect.bottom, DRAW_PAINT);
			return;
		}

		// pattern
		if (this.alphaPattern != null)
		{
			this.alphaPattern.draw(canvas);
		}

		// value
		if (!this.isNull)
		{
			this.colorPaint.setColor(this.color);
			final RectF rect = this.colorRect;
			canvas.drawRect(rect, this.colorPaint);
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

		this.drawingRect = new RectF();
		this.drawingRect.left = getPaddingLeft();
		this.drawingRect.right = w - getPaddingRight();
		this.drawingRect.top = getPaddingTop();
		this.drawingRect.bottom = h - getPaddingBottom();
		setUpColorRect();
	}

	/**
	 * Set up value rectangle
	 */
	private void setUpColorRect()
	{
		final RectF dRect = this.drawingRect;
		final float left = dRect.left + ColorPanelView.BORDER_WIDTH_PX;
		final float top = dRect.top + ColorPanelView.BORDER_WIDTH_PX;
		final float bottom = dRect.bottom - ColorPanelView.BORDER_WIDTH_PX;
		final float right = dRect.right - ColorPanelView.BORDER_WIDTH_PX;

		this.colorRect = new RectF(left, top, right, bottom);
		this.alphaPattern = new AlphaPatternDrawable((int) (5 * ColorPanelView.density));
		this.alphaPattern.setBounds(Math.round(this.colorRect.left), Math.round(this.colorRect.top), Math.round(this.colorRect.right), Math.round(this.colorRect.bottom));
		this.isCrossed = false;
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
			this.isNull = true;
			this.color = 0x00ffffff;
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
		this.isNull = false;
		this.color = color;
		invalidate();
	}

	/**
	 * Get the value currently shown by this view.
	 *
	 * @return value
	 */
	public int getColor()
	{
		return this.color;
	}

	/**
	 * Set the value of the border surrounding the panel.
	 *
	 * @param color border value
	 */
	public void setBorderColor(@SuppressWarnings("SameParameterValue") final int color)
	{
		this.borderColor = color;
		invalidate();
	}

	/**
	 * Get the value of the border surrounding the panel.
	 *
	 * @return border value
	 */
	public int getBorderColor()
	{
		return this.borderColor;
	}

	/**
	 * Set crossed flag
	 *
	 * @param isCrossed whether the display is crossed
	 */
	public void setCrossed(final boolean isCrossed)
	{
		this.isCrossed = isCrossed;
	}
}
