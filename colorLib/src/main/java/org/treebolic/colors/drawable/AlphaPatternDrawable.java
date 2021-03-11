/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.colors.drawable;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * This drawable will draw a simple white and gray chessboard pattern. It's pattern you will often see as a background behind a partly transparent image in many
 * applications.
 *
 * @author Daniel Nilsson
 */
public class AlphaPatternDrawable extends Drawable
{
	private final int mRectangleSize;

	private final Paint mPaint = new Paint();
	private final Paint mPaintWhite = new Paint();
	private final Paint mPaintGray = new Paint();

	private int numRectanglesHorizontal;
	private int numRectanglesVertical;

	/**
	 * Bitmap in which the pattern will be cached. This is so the pattern will not have to be recreated each time draw() gets called. Because recreating the
	 * pattern i rather expensive. I will only be recreated if the size changes.
	 */
	private Bitmap mBitmap;

	public AlphaPatternDrawable(final int rectangleSize)
	{
		this.mRectangleSize = rectangleSize;
		this.mPaintWhite.setColor(0xffffffff);
		this.mPaintGray.setColor(0xffcbcbcb);
	}

	@Override
	public void draw(@NonNull final Canvas canvas)
	{
		canvas.drawBitmap(this.mBitmap, null, getBounds(), this.mPaint);
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	public int getOpacity()
	{
		return PixelFormat.UNKNOWN;
	}

	@Override
	public void setAlpha(final int alpha)
	{
		throw new UnsupportedOperationException("Alpha is not supported by this drawable.");
	}

	@Override
	public void setColorFilter(final ColorFilter cf)
	{
		throw new UnsupportedOperationException("ColorFilter is not supported by this drawable.");
	}

	@Override
	protected void onBoundsChange(@NonNull final Rect bounds)
	{
		super.onBoundsChange(bounds);

		final int height = bounds.height();
		final int width = bounds.width();
		this.numRectanglesHorizontal = (int) Math.ceil((float) width / this.mRectangleSize);
		this.numRectanglesVertical = (int) Math.ceil((float) height / this.mRectangleSize);

		generatePatternBitmap();
	}

	/**
	 * This will generate a bitmap with the pattern as big as the rectangle we were allow to draw on. We do this to cache the bitmap so we don't need to
	 * recreate it each time draw() is called since it takes a few milliseconds.
	 */
	private void generatePatternBitmap()
	{
		if (getBounds().width() <= 0 || getBounds().height() <= 0)
		{
			return;
		}

		this.mBitmap = Bitmap.createBitmap(getBounds().width(), getBounds().height(), Config.ARGB_8888);
		final Canvas canvas = new Canvas(this.mBitmap);

		final Rect r = new Rect();
		boolean verticalStartWhite = true;
		for (int i = 0; i <= this.numRectanglesVertical; i++)
		{
			boolean isWhite = verticalStartWhite;
			for (int j = 0; j <= this.numRectanglesHorizontal; j++)
			{
				r.top = i * this.mRectangleSize;
				r.left = j * this.mRectangleSize;
				r.bottom = r.top + this.mRectangleSize;
				r.right = r.left + this.mRectangleSize;

				canvas.drawRect(r, isWhite ? this.mPaintWhite : this.mPaintGray);

				isWhite = !isWhite;
			}
			verticalStartWhite = !verticalStartWhite;
		}
	}
}
