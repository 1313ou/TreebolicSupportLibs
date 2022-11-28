/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Spinner wheel vertical view.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public class WheelVerticalView extends AbstractWheelView
{
	// private final String TAG = WheelVerticalView.class.getName() + " #" + (++itemID);
	// private static int itemID = -1;

	/**
	 * The height of the selection divider.
	 */
	@SuppressWarnings("WeakerAccess")
	protected int mSelectionDividerHeight;

	// Cached item height
	private int mItemHeight = 0;

	// --------------------------------------------------------------------------
	//
	// Constructors
	//
	// --------------------------------------------------------------------------

	/**
	 * Create a new wheel vertical view.
	 *
	 * @param context The application environment.
	 */
	public WheelVerticalView(@NonNull final Context context)
	{
		this(context, null);
	}

	/**
	 * Create a new wheel vertical view.
	 *
	 * @param context The application environment.
	 * @param attrs   A collection of attributes.
	 */
	public WheelVerticalView(@NonNull final Context context, @Nullable final AttributeSet attrs)
	{
		this(context, attrs, R.attr.abstractWheelViewStyle);
	}

	/**
	 * Create a new wheel vertical view.
	 *
	 * @param context  the application environment.
	 * @param attrs    a collection of attributes.
	 * @param defStyle The default style to apply to this view.
	 */
	public WheelVerticalView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyle)
	{
		super(context, attrs, defStyle);
	}

	// --------------------------------------------------------------------------
	//
	// Initiating assets and setter for selector paint
	//
	// --------------------------------------------------------------------------

	@Override
	protected void initAttributes(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyle)
	{
		super.initAttributes(context, attrs, defStyle);

		if (attrs != null)
		{
			try (final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WheelVerticalView, defStyle, 0))
			{
				this.mSelectionDividerHeight = array.getDimensionPixelSize(R.styleable.WheelVerticalView_selectionDividerHeight, DEF_SELECTION_DIVIDER_SIZE);
			}
		}
	}

	@Override
	public void setSelectorPaintCoeff(float coeff)
	{
		LinearGradient shader;

		int h = getMeasuredHeight();
		int ih = getItemDimension();
		float p1 = (1 - ih / (float) h) / 2;
		float p2 = (1 + ih / (float) h) / 2;
		float z = this.mItemsDimmedAlpha * (1 - coeff);
		float c1f = z + 255 * coeff;

		if (this.mVisibleItems == 2)
		{
			int c1 = Math.round(c1f) << 24;
			int c2 = Math.round(z) << 24;
			int[] colors = {c2, c1, 0xff000000, 0xff000000, c1, c2};
			float[] positions = {0, p1, p1, p2, p2, 1};
			shader = new LinearGradient(0, 0, 0, h, colors, positions, Shader.TileMode.CLAMP);
		}
		else
		{
			float p3 = (1 - ih * 3 / (float) h) / 2;
			float p4 = (1 + ih * 3 / (float) h) / 2;

			float s = 255 * p3 / p1;
			float c3f = s * coeff; // here goes some optimized stuff
			float c2f = z + c3f;

			int c1 = Math.round(c1f) << 24;
			int c2 = Math.round(c2f) << 24;
			int c3 = Math.round(c3f) << 24;

			int[] colors = {0, c3, c2, c1, 0xff000000, 0xff000000, c1, c2, c3, 0};
			float[] positions = {0, p3, p3, p1, p1, p2, p2, p4, p4, 1};
			shader = new LinearGradient(0, 0, 0, h, colors, positions, Shader.TileMode.CLAMP);
		}
		this.mSelectorWheelPaint.setShader(shader);
		invalidate();
	}

	// --------------------------------------------------------------------------
	//
	// Scroller-specific methods
	//
	// --------------------------------------------------------------------------

	@NonNull
	@Override
	protected WheelScroller createScroller(WheelScroller.ScrollingListener scrollingListener)
	{
		return new WheelVerticalScroller(getContext(), scrollingListener);
	}

	@Override
	protected float getMotionEventPosition(@NonNull MotionEvent event)
	{
		return event.getY();
	}

	// --------------------------------------------------------------------------
	//
	// Base measurements
	//
	// --------------------------------------------------------------------------

	@SuppressWarnings("WeakerAccess")
	@Override
	protected int getBaseDimension()
	{
		return getHeight();
	}

	/**
	 * Returns height of the spinnerwheel
	 *
	 * @return the item height
	 */
	@SuppressWarnings("WeakerAccess")
	@Override
	protected int getItemDimension()
	{
		if (this.mItemHeight != 0)
		{
			return this.mItemHeight;
		}

		if (this.mItemsLayout != null && this.mItemsLayout.getChildAt(0) != null)
		{
			this.mItemHeight = this.mItemsLayout.getChildAt(0).getMeasuredHeight();
			return this.mItemHeight;
		}

		return getBaseDimension() / this.mVisibleItems;
	}

	// --------------------------------------------------------------------------
	//
	// Layout creation and measurement operations
	//
	// --------------------------------------------------------------------------

	/**
	 * Creates item layout if necessary
	 */
	@Override
	protected void createItemsLayout()
	{
		if (this.mItemsLayout == null)
		{
			this.mItemsLayout = new LinearLayout(getContext());
			this.mItemsLayout.setOrientation(LinearLayout.VERTICAL);
		}
	}

	@Override
	protected void doItemsLayout()
	{
		this.mItemsLayout.layout(0, 0, getMeasuredWidth() - 2 * this.mItemsPadding, getMeasuredHeight());
	}

	@Override
	protected void measureLayout()
	{
		this.mItemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		this.mItemsLayout.measure(MeasureSpec.makeMeasureSpec(getWidth() - 2 * this.mItemsPadding, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		rebuildItems(); // rebuilding before measuring

		int width = calculateLayoutWidth(widthSize, widthMode);

		int height;
		if (heightMode == MeasureSpec.EXACTLY)
		{
			height = heightSize;
		}
		else
		{
			height = Math.max(getItemDimension() * (this.mVisibleItems - this.mItemOffsetPercent / 100), getSuggestedMinimumHeight());

			if (heightMode == MeasureSpec.AT_MOST)
			{
				height = Math.min(height, heightSize);
			}
		}
		setMeasuredDimension(width, height);
	}

	/**
	 * Calculates control width
	 *
	 * @param widthSize the input layout width
	 * @param mode      the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode)
	{
		this.mItemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.mItemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int width = this.mItemsLayout.getMeasuredWidth();

		if (mode == MeasureSpec.EXACTLY)
		{
			width = widthSize;
		}
		else
		{
			width += 2 * this.mItemsPadding;

			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && widthSize < width)
			{
				width = widthSize;
			}
		}

		// forcing recalculating
		this.mItemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * this.mItemsPadding, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		return width;
	}

	// --------------------------------------------------------------------------
	//
	// Drawing items
	//
	// --------------------------------------------------------------------------

	@Override
	protected void drawItems(@NonNull Canvas canvas)
	{
		canvas.save();
		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		int ih = getItemDimension();

		// resetting intermediate bitmap and recreating canvases
		this.mSpinBitmap.eraseColor(0);
		Canvas c = new Canvas(this.mSpinBitmap);
		Canvas cSpin = new Canvas(this.mSpinBitmap);

		int top = (this.mCurrentItemIdx - this.mFirstItemIdx) * ih + (ih - getHeight()) / 2;
		c.translate(this.mItemsPadding, -top + this.mScrollingOffset);
		this.mItemsLayout.draw(c);

		this.mSeparatorsBitmap.eraseColor(0);
		Canvas cSeparators = new Canvas(this.mSeparatorsBitmap);

		if (this.mSelectionDivider != null)
		{
			// draw the top divider
			int topOfTopDivider = (getHeight() - ih - this.mSelectionDividerHeight) / 2;
			int bottomOfTopDivider = topOfTopDivider + this.mSelectionDividerHeight;
			this.mSelectionDivider.setBounds(0, topOfTopDivider, w, bottomOfTopDivider);
			this.mSelectionDivider.draw(cSeparators);

			// draw the bottom divider
			int topOfBottomDivider = topOfTopDivider + ih;
			int bottomOfBottomDivider = bottomOfTopDivider + ih;
			this.mSelectionDivider.setBounds(0, topOfBottomDivider, w, bottomOfBottomDivider);
			this.mSelectionDivider.draw(cSeparators);
		}

		cSpin.drawRect(0, 0, w, h, this.mSelectorWheelPaint);
		cSeparators.drawRect(0, 0, w, h, this.mSeparatorsPaint);

		canvas.drawBitmap(this.mSpinBitmap, 0, 0, null);
		canvas.drawBitmap(this.mSeparatorsBitmap, 0, 0, null);
		canvas.restore();
	}

}
