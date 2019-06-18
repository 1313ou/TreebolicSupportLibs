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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Spinner wheel horizontal view.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public class WheelHorizontalView extends AbstractWheelView
{
	private final String LOG_TAG = WheelVerticalView.class.getName() + " #" + (++itemID);

	private static int itemID = -1;

	/**
	 * The width of the selection divider.
	 */
	@SuppressWarnings("WeakerAccess")
	protected int mSelectionDividerWidth;

	// Item width
	private int itemWidth = 0;

	// --------------------------------------------------------------------------
	//
	// Constructors
	//
	// --------------------------------------------------------------------------

	/**
	 * Create a new wheel horizontal view.
	 *
	 * @param context The application environment.
	 */
	public WheelHorizontalView(@NonNull final Context context)
	{
		this(context, null);
	}

	/**
	 * Create a new wheel horizontal view.
	 *
	 * @param context The application environment.
	 * @param attrs   A collection of attributes.
	 */
	public WheelHorizontalView(@NonNull final Context context, @Nullable final AttributeSet attrs)
	{
		this(context, attrs, R.attr.abstractWheelViewStyle);
	}

	/**
	 * Create a new wheel horizontal view.
	 *
	 * @param context  the application environment.
	 * @param attrs    a collection of attributes.
	 * @param defStyle The default style to apply to this view.
	 */
	public WheelHorizontalView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes int defStyle)
	{
		super(context, attrs, defStyle);
	}

	// --------------------------------------------------------------------------
	//
	// Initiating assets and setter for selector paint
	//
	// --------------------------------------------------------------------------

	@Override
	protected void initAttributes(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes int defStyle)
	{
		super.initAttributes(context, attrs, defStyle);

		if (attrs != null)
		{
			final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.WheelHorizontalView, defStyle, 0);
			this.mSelectionDividerWidth = array.getDimensionPixelSize(R.styleable.WheelHorizontalView_selectionDividerWidth, DEF_SELECTION_DIVIDER_SIZE);
			array.recycle();
		}
	}

	public void setSelectionDividerWidth(int selectionDividerWidth)
	{
		this.mSelectionDividerWidth = selectionDividerWidth;
	}

	@Override
	public void setSelectorPaintCoeff(float coeff)
	{
		if (this.mItemsDimmedAlpha >= 100)
		{
			return;
		}

		LinearGradient shader;

		int w = getMeasuredWidth();
		int iw = getItemDimension();
		float p1 = (1 - iw / (float) w) / 2;
		float p2 = (1 + iw / (float) w) / 2;
		float z = this.mItemsDimmedAlpha * (1 - coeff);
		float c1f = z + 255 * coeff;

		if (this.mVisibleItems == 2)
		{
			float[] positions = {0, p1, p1, p2, p2, 1};

			int c1 = Math.round(c1f) << 24;
			int c2 = Math.round(z) << 24;
			int[] colors = {c2, c1, 0xff000000, 0xff000000, c1, c2};
			shader = new LinearGradient(0, 0, w, 0, colors, positions, Shader.TileMode.CLAMP);
		}
		else
		{
			float p3 = (1 - iw * 3 / (float) w) / 2;
			float p4 = (1 + iw * 3 / (float) w) / 2;
			float[] positions = {0, p3, p3, p1, p1, p2, p2, p4, p4, 1};

			float s = 255 * p3 / p1;
			float c3f = s * coeff; // here goes some optimized stuff
			float c2f = z + c3f;

			int c1 = Math.round(c1f) << 24;
			int c2 = Math.round(c2f) << 24;
			int c3 = Math.round(c3f) << 24;
			//int[] colors = { c2, c2, c2, c2, 0xff000000, 0xff000000, c2, c2, c2, c2 };
			int[] colors = {c3, c3, c2, c1, 0xff000000, 0xff000000, c1, c2, c3, c3};

			shader = new LinearGradient(0, 0, w, 0, colors, positions, Shader.TileMode.CLAMP);
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
		return new WheelHorizontalScroller(getContext(), scrollingListener);
	}

	@Override
	protected float getMotionEventPosition(@NonNull MotionEvent event)
	{
		return event.getX();
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
		return getWidth();
	}

	/**
	 * Returns height of spinnerwheel item
	 *
	 * @return the item width
	 */
	@SuppressWarnings("WeakerAccess")
	@Override
	protected int getItemDimension()
	{
		if (this.itemWidth != 0)
		{
			return this.itemWidth;
		}

		if (this.mItemsLayout != null && this.mItemsLayout.getChildAt(0) != null)
		{
			this.itemWidth = this.mItemsLayout.getChildAt(0).getMeasuredWidth();
			return this.itemWidth;
		}

		return getBaseDimension() / this.mVisibleItems;
	}

	// --------------------------------------------------------------------------
	//
	// Debugging stuff
	//
	// --------------------------------------------------------------------------

	@Override
	protected void onScrollTouchedUp()
	{
		super.onScrollTouchedUp();
		int cnt = this.mItemsLayout.getChildCount();
		View itm;
		Log.e(this.LOG_TAG, " ----- layout: " + this.mItemsLayout.getMeasuredWidth() + this.mItemsLayout.getMeasuredHeight());
		Log.e(this.LOG_TAG, " -------- dumping " + cnt + " items");
		for (int i = 0; i < cnt; i++)
		{
			itm = this.mItemsLayout.getChildAt(i);
			Log.e(this.LOG_TAG, " item #" + i + ": " + itm.getWidth() + "x" + itm.getHeight());
			itm.forceLayout(); // forcing layout without re-rendering parent
		}
		Log.e(this.LOG_TAG, " ---------- dumping finished ");
	}

	// --------------------------------------------------------------------------
	//
	// Layout creation and measurement operations
	//
	// --------------------------------------------------------------------------

	/**
	 * Creates item layouts if necessary
	 */
	@Override
	protected void createItemsLayout()
	{
		if (this.mItemsLayout == null)
		{
			this.mItemsLayout = new LinearLayout(getContext());
			this.mItemsLayout.setOrientation(LinearLayout.HORIZONTAL);
		}
	}

	@Override
	protected void doItemsLayout()
	{
		this.mItemsLayout.layout(0, 0, getMeasuredWidth(), getMeasuredHeight() - 2 * this.mItemsPadding);
	}

	@Override
	protected void measureLayout()
	{
		this.mItemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		// XXX: Locating bug
		this.mItemsLayout.measure(MeasureSpec.makeMeasureSpec(getWidth() + getItemDimension(), MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}

	// XXX: Most likely, measurements of mItemsLayout or/and its children are done incorrectly.
	// Investigate and fix it

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		rebuildItems(); // rebuilding before measuring

		int height = calculateLayoutHeight(heightSize, heightMode);

		int width;
		if (widthMode == MeasureSpec.EXACTLY)
		{
			width = widthSize;
		}
		else
		{
			width = Math.max(getItemDimension() * (this.mVisibleItems - this.mItemOffsetPercent / 100), getSuggestedMinimumWidth());

			if (widthMode == MeasureSpec.AT_MOST)
			{
				width = Math.min(width, widthSize);
			}
		}
		setMeasuredDimension(width, height);
	}

	/**
	 * Calculates control height and creates text layouts
	 *
	 * @param heightSize the input layout height
	 * @param mode       the layout mode
	 * @return the calculated control height
	 */
	private int calculateLayoutHeight(int heightSize, int mode)
	{
		this.mItemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.mItemsLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED));
		int height = this.mItemsLayout.getMeasuredHeight();

		if (mode == MeasureSpec.EXACTLY)
		{
			height = heightSize;
		}
		else
		{
			height += 2 * this.mItemsPadding;

			// Check against our minimum width
			height = Math.max(height, getSuggestedMinimumHeight());

			if (mode == MeasureSpec.AT_MOST && heightSize < height)
			{
				height = heightSize;
			}
		}
		// forcing recalculating
		this.mItemsLayout.measure(
				// MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(400, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - 2 * this.mItemsPadding, MeasureSpec.EXACTLY));

		return height;
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
		int iw = getItemDimension();

		// resetting intermediate bitmap and recreating canvases
		this.mSpinBitmap.eraseColor(0);
		Canvas c = new Canvas(this.mSpinBitmap);
		Canvas cSpin = new Canvas(this.mSpinBitmap);

		int left = (this.mCurrentItemIdx - this.mFirstItemIdx) * iw + (iw - getWidth()) / 2;
		c.translate(-left + this.mScrollingOffset, this.mItemsPadding);
		this.mItemsLayout.draw(c);

		this.mSeparatorsBitmap.eraseColor(0);
		Canvas cSeparators = new Canvas(this.mSeparatorsBitmap);

		if (this.mSelectionDivider != null)
		{
			// draw the top divider
			int leftOfLeftDivider = (getWidth() - iw - this.mSelectionDividerWidth) / 2;
			int rightOfLeftDivider = leftOfLeftDivider + this.mSelectionDividerWidth;
			cSeparators.save();
			// On Gingerbread setBounds() is ignored resulting in an ugly visual bug.
			cSeparators.clipRect(leftOfLeftDivider, 0, rightOfLeftDivider, h);
			this.mSelectionDivider.setBounds(leftOfLeftDivider, 0, rightOfLeftDivider, h);
			this.mSelectionDivider.draw(cSeparators);
			cSeparators.restore();

			cSeparators.save();
			// draw the bottom divider
			int leftOfRightDivider = leftOfLeftDivider + iw;
			int rightOfRightDivider = rightOfLeftDivider + iw;
			// On Gingerbread setBounds() is ignored resulting in an ugly visual bug.
			cSeparators.clipRect(leftOfRightDivider, 0, rightOfRightDivider, h);
			this.mSelectionDivider.setBounds(leftOfRightDivider, 0, rightOfRightDivider, h);
			this.mSelectionDivider.draw(cSeparators);
			cSeparators.restore();
		}

		cSpin.drawRect(0, 0, w, h, this.mSelectorWheelPaint);
		cSeparators.drawRect(0, 0, w, h, this.mSeparatorsPaint);

		canvas.drawBitmap(this.mSpinBitmap, 0, 0, null);
		canvas.drawBitmap(this.mSeparatorsBitmap, 0, 0, null);
		canvas.restore();
	}

}
