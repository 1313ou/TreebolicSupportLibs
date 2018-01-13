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
 *
 *
 *
 *
 * Change Log:
 *
 * 1.1
 * - Fixed buggy measure and layout code. You can now make the view any size you want.
 * - Optimization of the drawing using a bitmap cache, a lot faster!
 * - Support for hardware acceleration for all but the problematic
 *	 part of the view will still be software rendered but much faster!
 *   See comment in drawSatValPanel() for more info.
 * - Support for declaring some variables in xml.
 */

package org.treebolic.colors.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.treebolic.colors.R;
import org.treebolic.colors.drawable.AlphaPatternDrawable;

/**
 * Displays a color picker to the user and allow them to select a color. A slider for the alpha channel is also available. Enable it by setting
 * setAlphaSliderVisible(boolean) to true.
 *
 * @author Daniel Nilsson
 */
public class ColorPickerView extends View
{
	/**
	 * Color change listener
	 */
	public interface OnColorChangedListener
	{
		void onColorChanged(int newColor);
	}

	// I N D I C E S

	private final static int PANEL_SAT_VAL = 0;
	private final static int PANEL_HUE = 1;
	private final static int PANEL_ALPHA = 2;

	// D I M E N S I O N S

	/**
	 * The width in pixels of the border surrounding all color panels.
	 */
	private final static float BORDER_WIDTH_PX = 1;

	/**
	 * The width in dp of the hue panel.
	 */
	private float HUE_PANEL_WIDTH = 30f;

	/**
	 * The height in dp of the alpha panel
	 */
	private float ALPHA_PANEL_HEIGHT = 20f;

	/**
	 * The distance in dp between the different color panels.
	 */
	private float PANEL_SPACING = 10f;

	/**
	 * The radius in dp of the color palette tracker circle.
	 */
	private float PALETTE_CIRCLE_TRACKER_RADIUS = 5f;

	/**
	 * The dp which the tracker of the hue or alpha panel will extend outside of its bounds.
	 */
	private float RECTANGLE_TRACKER_OFFSET = 2f;

	/**
	 * Density
	 */
	private static float mDensity = 1f;

	// P A I N T S

	/**
	 * Saturation paint
	 */
	private Paint mSatValPaint;

	/**
	 * Saturation tracker paint
	 */
	private Paint mSatValTrackerPaint;

	/**
	 * Hue paint
	 */
	private Paint mHuePaint;

	/**
	 * Hue tracker paint
	 */
	private Paint mHueAlphaTrackerPaint;

	/**
	 * Alpha paint
	 */
	private Paint mAlphaPaint;

	/**
	 * Alpha text paint
	 */
	private Paint mAlphaTextPaint;

	/**
	 * Border paint
	 */
	private Paint mBorderPaint;

	// S H A D E R S

	/**
	 * Value shader
	 */
	@Nullable
	private Shader mValShader;

	/**
	 * Saturation shader
	 */
	@Nullable
	private Shader mSatShader;

	/**
	 * Hue shader
	 */
	@Nullable
	private Shader mHueShader;

	/**
	 * Alpha shader
	 */
	@Nullable
	private Shader mAlphaShader;

	/*
	 * We cache a bitmap of the sat/val panel which is expensive to draw each time. We can reuse it when the user is sliding the circle picker as long as the
	 * hue isn't changed.
	 */
	private BitmapCache mSatValBackgroundCache;

	// V A L U E S
	private int mAlpha = 0xff;
	private float mHue = 360f;
	private float mSat = 0f;
	private float mVal = 0f;

	// S E T T I N G S
	/**
	 * Alpha slider text
	 */
	@Nullable
	private String mAlphaSliderText = null;

	/**
	 * Show alpha panel
	 */
	private boolean mShowAlphaPanel = false;

	/**
	 * Slider tracker color
	 */
	private int mSliderTrackerColor = 0xFFBDBDBD;

	/**
	 * Border color
	 */
	private int mBorderColor = 0xFF6E6E6E;

	// S T A T E

	/**
	 * To remember which panel that has the "focus" when processing hardware button data.
	 */
	private int mLastTouchedPanel = ColorPickerView.PANEL_SAT_VAL;

	/**
	 * Start touch point
	 */
	@Nullable
	private Point mStartTouchPoint = null;

	/**
	 * Offset from the edge we must have or else the finger tracker will get clipped when it is drawn outside of the view.
	 */
	private int mDrawingOffset;

	// L I S T E N E R

	/**
	 * Listener
	 */
	private OnColorChangedListener mListener;

	// R E C T A N G L E S

	/**
	 * Distance form the edges of the view of where we are allowed to draw.
	 */
	private RectF mDrawingRect;

	/**
	 * Saturation rectangle
	 */
	private RectF mSatValRect;

	/**
	 * Hue rectangle
	 */
	private RectF mHueRect;

	/**
	 * Alpha rectangle
	 */
	private RectF mAlphaRect;

	// P A T T E R N

	/**
	 * Alpha pattern
	 */
	private AlphaPatternDrawable mAlphaPattern;

	/**
	 * Constructor
	 *
	 * @param context context
	 */
	public ColorPickerView(final Context context)
	{
		this(context, null);
	}

	/**
	 * Constructor
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	public ColorPickerView(final Context context, final AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 * Constructor
	 *
	 * @param context  context
	 * @param attrs    attributes
	 * @param defStyle def style
	 */
	public ColorPickerView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs);
	}

	/**
	 * Common init
	 *
	 * @param attrs attributes
	 */
	private void init(final AttributeSet attrs)
	{
		// Load those if set in xml resource file.
		final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
		this.mShowAlphaPanel = array.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false);
		this.mAlphaSliderText = array.getString(R.styleable.ColorPickerView_alphaChannelText);
		this.mSliderTrackerColor = array.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, 0xFFBDBDBD);
		this.mBorderColor = array.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, 0xFF6E6E6E);
		array.recycle();

		ColorPickerView.mDensity = getContext().getResources().getDisplayMetrics().density;
		this.PALETTE_CIRCLE_TRACKER_RADIUS *= ColorPickerView.mDensity;
		this.RECTANGLE_TRACKER_OFFSET *= ColorPickerView.mDensity;
		this.HUE_PANEL_WIDTH *= ColorPickerView.mDensity;
		this.ALPHA_PANEL_HEIGHT *= ColorPickerView.mDensity;
		this.PANEL_SPACING = this.PANEL_SPACING * ColorPickerView.mDensity;

		this.mDrawingOffset = calculateRequiredOffset();

		initPaintTools();

		// Needed for receiving trackball motion events.
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	/**
	 * Init paint tools
	 */
	private void initPaintTools()
	{
		this.mSatValPaint = new Paint();
		this.mSatValTrackerPaint = new Paint();
		this.mHuePaint = new Paint();
		this.mHueAlphaTrackerPaint = new Paint();
		this.mAlphaPaint = new Paint();
		this.mAlphaTextPaint = new Paint();
		this.mBorderPaint = new Paint();

		this.mSatValTrackerPaint.setStyle(Style.STROKE);
		this.mSatValTrackerPaint.setStrokeWidth(2f * ColorPickerView.mDensity);
		this.mSatValTrackerPaint.setAntiAlias(true);

		this.mHueAlphaTrackerPaint.setColor(this.mSliderTrackerColor);
		this.mHueAlphaTrackerPaint.setStyle(Style.STROKE);
		this.mHueAlphaTrackerPaint.setStrokeWidth(2f * ColorPickerView.mDensity);
		this.mHueAlphaTrackerPaint.setAntiAlias(true);

		this.mAlphaTextPaint.setColor(0xff1c1c1c);
		this.mAlphaTextPaint.setTextSize(14f * ColorPickerView.mDensity);
		this.mAlphaTextPaint.setAntiAlias(true);
		this.mAlphaTextPaint.setTextAlign(Align.CENTER);
		this.mAlphaTextPaint.setFakeBoldText(true);
	}

	private int calculateRequiredOffset()
	{
		float offset = Math.max(this.PALETTE_CIRCLE_TRACKER_RADIUS, this.RECTANGLE_TRACKER_OFFSET);
		offset = Math.max(offset, ColorPickerView.BORDER_WIDTH_PX * ColorPickerView.mDensity);

		return (int) (offset * 1.5f);
	}

	@NonNull
	private static int[] buildHueColorArray()
	{
		final int[] hue = new int[361];

		int count = 0;
		for (int i = hue.length - 1; i >= 0; i--, count++)
		{
			hue[count] = Color.HSVToColor(new float[]{i, 1f, 1f});
		}

		return hue;
	}

	@Override
	protected void onDraw(@NonNull final Canvas canvas)
	{
		if (this.mDrawingRect.width() <= 0 || this.mDrawingRect.height() <= 0)
		{
			return;
		}

		drawSatValPanel(canvas);
		drawHuePanel(canvas);
		drawAlphaPanel(canvas);
	}

	private void drawSatValPanel(@NonNull final Canvas canvas)
	{
		/*
		 * Draw time for this code without using bitmap cache and hardware acceleration was around 20ms. Now with the bitmap cache and the ability to use
		 * hardware acceleration we are down at 1ms as long as the hue isn't changed. If the hue is changed we the sat/val rectangle will be rendered in
		 * software and it takes around 10ms. But since the rest of the view will be rendered in hardware the performance gain is big!
		 */

		final RectF rect = this.mSatValRect;

		//noinspection ConstantConditions
		if (ColorPickerView.BORDER_WIDTH_PX > 0)
		{
			this.mBorderPaint.setColor(this.mBorderColor);
			canvas.drawRect(this.mDrawingRect.left, this.mDrawingRect.top, rect.right + ColorPickerView.BORDER_WIDTH_PX, rect.bottom + ColorPickerView.BORDER_WIDTH_PX, this.mBorderPaint);
		}

		if (this.mValShader == null)
		{
			// Black gradient has either not been created or the view has been resized.
			this.mValShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, 0xffffffff, 0xff000000, TileMode.CLAMP);
		}

		// If the hue has changed we need to recreate the cache.
		if (this.mSatValBackgroundCache == null || this.mSatValBackgroundCache.value != this.mHue)
		{

			if (this.mSatValBackgroundCache == null)
			{
				this.mSatValBackgroundCache = new BitmapCache();
			}

			// We create our bitmap in the cache if it doesn't exist.
			if (this.mSatValBackgroundCache.bitmap == null)
			{
				this.mSatValBackgroundCache.bitmap = Bitmap.createBitmap((int) rect.width(), (int) rect.height(), Config.ARGB_8888);
			}

			// We create the canvas once so we can draw on our bitmap and the hold on to it.
			if (this.mSatValBackgroundCache.canvas == null)
			{
				this.mSatValBackgroundCache.canvas = new Canvas(this.mSatValBackgroundCache.bitmap);
			}

			final int rgb = Color.HSVToColor(new float[]{this.mHue, 1f, 1f});

			this.mSatShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, 0xffffffff, rgb, TileMode.CLAMP);

			final ComposeShader mShader = new ComposeShader(this.mValShader, this.mSatShader, PorterDuff.Mode.MULTIPLY);
			this.mSatValPaint.setShader(mShader);

			// Finally we draw on our canvas, the result will be stored in our bitmap which is already in the cache.
			// Since this is drawn on a canvas not rendered on screen it will automatically not be using the hardware acceleration.
			// And this was the code that wasn't supported by hardware acceleration which mean there is no need to turn it of anymore.
			// The rest of the view will still be hardware accelerated!!
			this.mSatValBackgroundCache.canvas.drawRect(0, 0, this.mSatValBackgroundCache.bitmap.getWidth(), this.mSatValBackgroundCache.bitmap.getHeight(), this.mSatValPaint);

			// We set the hue value in our cache to which hue it was drawn with,
			// then we know that if it hasn't changed we can reuse our cached bitmap.
			this.mSatValBackgroundCache.value = this.mHue;

		}

		// We draw our bitmap from the cached, if the hue has changed
		// then it was just recreated otherwise the old one will be used.
		canvas.drawBitmap(this.mSatValBackgroundCache.bitmap, null, rect, null);

		final Point p = satValToPoint(this.mSat, this.mVal);

		this.mSatValTrackerPaint.setColor(0xff000000);
		canvas.drawCircle(p.x, p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS - 1f * ColorPickerView.mDensity, this.mSatValTrackerPaint);

		this.mSatValTrackerPaint.setColor(0xffdddddd);
		canvas.drawCircle(p.x, p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS, this.mSatValTrackerPaint);
	}

	private void drawHuePanel(@NonNull final Canvas canvas)
	{
		/*
		 * Drawn with hw acceleration, very fast.
		 */
		final RectF rect = this.mHueRect;

		//noinspection ConstantConditions
		if (ColorPickerView.BORDER_WIDTH_PX > 0)
		{
			this.mBorderPaint.setColor(this.mBorderColor);
			canvas.drawRect(rect.left - ColorPickerView.BORDER_WIDTH_PX, rect.top - ColorPickerView.BORDER_WIDTH_PX, rect.right + ColorPickerView.BORDER_WIDTH_PX, rect.bottom + ColorPickerView.BORDER_WIDTH_PX, this.mBorderPaint);
		}

		if (this.mHueShader == null)
		{
			// The hue shader has either not yet been created or the view has been resized.
			this.mHueShader = new LinearGradient(0, 0, 0, rect.height(), ColorPickerView.buildHueColorArray(), null, TileMode.CLAMP);
			this.mHuePaint.setShader(this.mHueShader);
		}

		canvas.drawRect(rect, this.mHuePaint);

		final float rectHeight = 4 * ColorPickerView.mDensity / 2;

		final Point p = hueToPoint(this.mHue);

		final RectF r = new RectF();
		r.left = rect.left - this.RECTANGLE_TRACKER_OFFSET;
		r.right = rect.right + this.RECTANGLE_TRACKER_OFFSET;
		r.top = p.y - rectHeight;
		r.bottom = p.y + rectHeight;

		canvas.drawRoundRect(r, 2, 2, this.mHueAlphaTrackerPaint);
	}

	private void drawAlphaPanel(@NonNull final Canvas canvas)
	{
		/*
		 * Will be drawn with hw acceleration, very fast.
		 */

		if (!this.mShowAlphaPanel || this.mAlphaRect == null || this.mAlphaPattern == null)
		{
			return;
		}

		final RectF rect = this.mAlphaRect;

		//noinspection ConstantConditions
		if (ColorPickerView.BORDER_WIDTH_PX > 0)
		{
			this.mBorderPaint.setColor(this.mBorderColor);
			canvas.drawRect(rect.left - ColorPickerView.BORDER_WIDTH_PX, rect.top - ColorPickerView.BORDER_WIDTH_PX, rect.right + ColorPickerView.BORDER_WIDTH_PX, rect.bottom + ColorPickerView.BORDER_WIDTH_PX, this.mBorderPaint);
		}

		this.mAlphaPattern.draw(canvas);

		final float[] hsv = new float[]{this.mHue, this.mSat, this.mVal};
		final int color = Color.HSVToColor(hsv);
		final int acolor = Color.HSVToColor(0, hsv);

		this.mAlphaShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, color, acolor, TileMode.CLAMP);

		this.mAlphaPaint.setShader(this.mAlphaShader);

		canvas.drawRect(rect, this.mAlphaPaint);

		if (this.mAlphaSliderText != null && !this.mAlphaSliderText.equals(""))
		{
			canvas.drawText(this.mAlphaSliderText, rect.centerX(), rect.centerY() + 4 * ColorPickerView.mDensity, this.mAlphaTextPaint);
		}

		final float rectWidth = 4 * ColorPickerView.mDensity / 2;

		final Point p = alphaToPoint(this.mAlpha);

		final RectF r = new RectF();
		r.left = p.x - rectWidth;
		r.right = p.x + rectWidth;
		r.top = rect.top - this.RECTANGLE_TRACKER_OFFSET;
		r.bottom = rect.bottom + this.RECTANGLE_TRACKER_OFFSET;

		canvas.drawRoundRect(r, 2, 2, this.mHueAlphaTrackerPaint);
	}

	@NonNull
	private Point hueToPoint(final float hue)
	{
		final RectF rect = this.mHueRect;
		final float height = rect.height();

		final Point p = new Point();

		p.y = (int) (height - hue * height / 360f + rect.top);
		p.x = (int) rect.left;

		return p;
	}

	@NonNull
	private Point satValToPoint(final float sat, final float val)
	{
		final RectF rect = this.mSatValRect;
		final float height = rect.height();
		final float width = rect.width();

		final Point p = new Point();

		p.x = (int) (sat * width + rect.left);
		p.y = (int) ((1f - val) * height + rect.top);

		return p;
	}

	@NonNull
	private Point alphaToPoint(final int alpha)
	{

		final RectF rect = this.mAlphaRect;
		final float width = rect.width();

		final Point p = new Point();

		p.x = (int) (width - alpha * width / 0xff + rect.left);
		p.y = (int) rect.top;

		return p;
	}

	@NonNull
	private float[] pointToSatVal(final float x0, final float y0)
	{
		float x = x0;
		float y = y0;
		final RectF rect = this.mSatValRect;
		final float[] result = new float[2];

		final float width = rect.width();
		final float height = rect.height();

		if (x < rect.left)
		{
			x = 0f;
		}
		else if (x > rect.right)
		{
			x = width;
		}
		else
		{
			x = x - rect.left;
		}

		if (y < rect.top)
		{
			y = 0f;
		}
		else if (y > rect.bottom)
		{
			y = height;
		}
		else
		{
			y = y - rect.top;
		}

		result[0] = 1.f / width * x;
		result[1] = 1.f - 1.f / height * y;

		return result;
	}

	private float pointToHue(final float y0)
	{
		float y = y0;
		final RectF rect = this.mHueRect;

		final float height = rect.height();

		if (y < rect.top)
		{
			y = 0f;
		}
		else if (y > rect.bottom)
		{
			y = height;
		}
		else
		{
			y = y - rect.top;
		}

		return 360f - y * 360f / height;
	}

	private int pointToAlpha(final int x0)
	{
		int x = x0;
		final RectF rect = this.mAlphaRect;
		final int width = (int) rect.width();

		if (x < rect.left)
		{
			x = 0;
		}
		else if (x > rect.right)
		{
			x = width;
		}
		else
		{
			x = x - (int) rect.left;
		}
		return 0xff - x * 0xff / width;

	}

	@Override
	public boolean onTrackballEvent(@NonNull final MotionEvent event)
	{
		final float x = event.getX();
		final float y = event.getY();

		boolean update = false;

		if (event.getAction() == MotionEvent.ACTION_MOVE)
		{

			switch (this.mLastTouchedPanel)
			{
				case PANEL_SAT_VAL:

					float sat, val;

					sat = this.mSat + x / 50f;
					val = this.mVal - y / 50f;

					if (sat < 0f)
					{
						sat = 0f;
					}
					else if (sat > 1f)
					{
						sat = 1f;
					}

					if (val < 0f)
					{
						val = 0f;
					}
					else if (val > 1f)
					{
						val = 1f;
					}

					this.mSat = sat;
					this.mVal = val;

					update = true;

					break;

				case PANEL_HUE:

					float hue = this.mHue - y * 10f;

					if (hue < 0f)
					{
						hue = 0f;
					}
					else if (hue > 360f)
					{
						hue = 360f;
					}

					this.mHue = hue;

					update = true;

					break;

				case PANEL_ALPHA:

					if (!this.mShowAlphaPanel || this.mAlphaRect == null)
					{
						update = false;
					}
					else
					{

						int alpha = (int) (this.mAlpha - x * 10);

						if (alpha < 0)
						{
							alpha = 0;
						}
						else if (alpha > 0xff)
						{
							alpha = 0xff;
						}

						this.mAlpha = alpha;

						update = true;
					}

					break;
				default:
					break;
			}
		}

		if (update)
		{

			if (this.mListener != null)
			{
				this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
			}

			invalidate();
			return true;
		}

		return super.onTrackballEvent(event);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(@NonNull final MotionEvent event)
	{
		boolean update = false;

		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				this.mStartTouchPoint = new Point((int) event.getX(), (int) event.getY());
				update = moveTrackersIfNeeded(event);
				break;

			case MotionEvent.ACTION_MOVE:
				update = moveTrackersIfNeeded(event);
				break;
			case MotionEvent.ACTION_UP:
				this.mStartTouchPoint = null;
				update = moveTrackersIfNeeded(event);
				break;
			default:
				break;
		}

		if (update)
		{
			if (this.mListener != null)
			{
				this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
			}
			invalidate();
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			performClick();
		}

		return super.onTouchEvent(event);
	}

	private boolean moveTrackersIfNeeded(@NonNull final MotionEvent event)
	{
		if (this.mStartTouchPoint == null)
		{
			return false;
		}

		boolean update = false;

		final int startX = this.mStartTouchPoint.x;
		final int startY = this.mStartTouchPoint.y;

		if (this.mHueRect.contains(startX, startY))
		{
			this.mLastTouchedPanel = ColorPickerView.PANEL_HUE;

			this.mHue = pointToHue(event.getY());

			update = true;
		}
		else if (this.mSatValRect.contains(startX, startY))
		{

			this.mLastTouchedPanel = ColorPickerView.PANEL_SAT_VAL;

			final float[] result = pointToSatVal(event.getX(), event.getY());

			this.mSat = result[0];
			this.mVal = result[1];

			update = true;
		}
		else if (this.mAlphaRect != null && this.mAlphaRect.contains(startX, startY))
		{
			this.mLastTouchedPanel = ColorPickerView.PANEL_ALPHA;

			this.mAlpha = pointToAlpha((int) event.getX());

			update = true;
		}

		return update;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		int finalWidth = 0;
		int finalHeight = 0;

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		final int widthAllowed = MeasureSpec.getSize(widthMeasureSpec);
		final int heightAllowed = MeasureSpec.getSize(heightMeasureSpec);

		// Log.d("color-picker-view", "widthMode: " + modeToString(widthMode) + " heightMode: " + modeToString(heightMode) + " widthAllowed: " + widthAllowed +
		// " heightAllowed: " + heightAllowed);

		if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY)
		{
			// A exact value has been set in either direction, we need to stay within this size.

			if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY)
			{
				// The with has been specified exactly, we need to adopt the height to fit.
				int h = (int) (widthAllowed - this.PANEL_SPACING - this.HUE_PANEL_WIDTH);

				if (this.mShowAlphaPanel)
				{
					h += this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT;
				}

				if (h > heightAllowed)
				{
					// We can't fit the view in this container, set the size to whatever was allowed.
					finalHeight = heightAllowed;
				}
				else
				{
					finalHeight = h;
				}

				finalWidth = widthAllowed;
			}
			else //noinspection ConstantConditions
				if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY)
				{
					// The height has been specified exactly, we need to stay within this height and adopt the width.

					int w = (int) (heightAllowed + this.PANEL_SPACING + this.HUE_PANEL_WIDTH);

					if (this.mShowAlphaPanel)
					{
						w -= this.PANEL_SPACING - this.ALPHA_PANEL_HEIGHT;
					}

					if (w > widthAllowed)
					{
						// we can't fit within this container, set the size to whatever was allowed.
						finalWidth = widthAllowed;
					}
					else
					{
						finalWidth = w;
					}

					finalHeight = heightAllowed;
				}
				else
				{
					// If we get here the dev has set the width and height to exact sizes. For example match_parent or 300dp.
					// This will mean that the sat/val panel will not be square but it doesn't matter. It will work anyway.
					// In all other scenarios our goal is to make that panel square.

					// We set the sizes to exactly what we were told.
					finalWidth = widthAllowed;
					finalHeight = heightAllowed;
				}
		}
		else
		{
			// If no exact size has been set we try to make our view as big as possible
			// within the allowed space.

			// Calculate the needed with to layout the view based on the allowed height.
			int widthNeeded = (int) (heightAllowed + this.PANEL_SPACING + this.HUE_PANEL_WIDTH);
			// Calculate the needed height to layout the view based on the allowed width.
			int heightNeeded = (int) (widthAllowed - this.PANEL_SPACING - this.HUE_PANEL_WIDTH);

			if (this.mShowAlphaPanel)
			{
				widthNeeded -= this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT;
				heightNeeded += this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT;
			}

			if (widthNeeded <= widthAllowed)
			{
				finalWidth = widthNeeded;
				finalHeight = heightAllowed;
			}
			else if (heightNeeded <= heightAllowed)
			{
				finalHeight = heightNeeded;
				finalWidth = widthAllowed;
			}
		}

		// Log.d("mColorPicker", "Final Size: " + finalWidth + "x" + finalHeight);

		setMeasuredDimension(finalWidth, finalHeight);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		this.mDrawingRect = new RectF();
		this.mDrawingRect.left = this.mDrawingOffset + getPaddingLeft();
		this.mDrawingRect.right = w - this.mDrawingOffset - getPaddingRight();
		this.mDrawingRect.top = this.mDrawingOffset + getPaddingTop();
		this.mDrawingRect.bottom = h - this.mDrawingOffset - getPaddingBottom();

		// The need to be recreated because they depend on the size of the view.
		this.mValShader = null;
		this.mSatShader = null;
		this.mHueShader = null;
		this.mAlphaShader = null;

		setUpSatValRect();
		setUpHueRect();
		setUpAlphaRect();
	}

	private void setUpSatValRect()
	{
		// Calculate the size for the big color rectangle.
		final RectF dRect = this.mDrawingRect;

		final float left = dRect.left + ColorPickerView.BORDER_WIDTH_PX;
		final float top = dRect.top + ColorPickerView.BORDER_WIDTH_PX;
		float bottom = dRect.bottom - ColorPickerView.BORDER_WIDTH_PX;
		final float right = dRect.right - ColorPickerView.BORDER_WIDTH_PX - this.PANEL_SPACING - this.HUE_PANEL_WIDTH;

		if (this.mShowAlphaPanel)
		{
			bottom -= this.ALPHA_PANEL_HEIGHT + this.PANEL_SPACING;
		}

		this.mSatValRect = new RectF(left, top, right, bottom);
	}

	private void setUpHueRect()
	{
		// Calculate the size for the hue slider on the left.
		final RectF dRect = this.mDrawingRect;

		final float left = dRect.right - this.HUE_PANEL_WIDTH + ColorPickerView.BORDER_WIDTH_PX;
		final float top = dRect.top + ColorPickerView.BORDER_WIDTH_PX;
		final float bottom = dRect.bottom - ColorPickerView.BORDER_WIDTH_PX - (this.mShowAlphaPanel ? this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT : 0);
		final float right = dRect.right - ColorPickerView.BORDER_WIDTH_PX;

		this.mHueRect = new RectF(left, top, right, bottom);
	}

	private void setUpAlphaRect()
	{
		if (!this.mShowAlphaPanel)
		{
			return;
		}

		final RectF dRect = this.mDrawingRect;

		final float left = dRect.left + ColorPickerView.BORDER_WIDTH_PX;
		final float top = dRect.bottom - this.ALPHA_PANEL_HEIGHT + ColorPickerView.BORDER_WIDTH_PX;
		final float bottom = dRect.bottom - ColorPickerView.BORDER_WIDTH_PX;
		final float right = dRect.right - ColorPickerView.BORDER_WIDTH_PX;

		this.mAlphaRect = new RectF(left, top, right, bottom);

		this.mAlphaPattern = new AlphaPatternDrawable((int) (5 * ColorPickerView.mDensity));
		this.mAlphaPattern.setBounds(Math.round(this.mAlphaRect.left), Math.round(this.mAlphaRect.top), Math.round(this.mAlphaRect.right), Math.round(this.mAlphaRect.bottom));
	}

	/**
	 * Set a OnColorChangedListener to get notified when the color selected by the user has changed.
	 *
	 * @param listener change listener
	 */
	public void setOnColorChangedListener(final OnColorChangedListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * Get the current color this view is showing.
	 *
	 * @return the current color.
	 */
	public int getColor()
	{
		return Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal});
	}

	/**
	 * Set the color the view should show.
	 *
	 * @param color The color that should be selected.
	 */
	public void setColor(final int color)
	{
		setColor(color, false);
	}

	/**
	 * Set the color this view should show.
	 *
	 * @param color    The color that should be selected.
	 * @param callback If you want to get a callback to your OnColorChangedListener.
	 */
	public void setColor(final int color, final boolean callback)
	{
		final int alpha = Color.alpha(color);
		final int red = Color.red(color);
		final int blue = Color.blue(color);
		final int green = Color.green(color);

		final float[] hsv = new float[3];

		Color.RGBToHSV(red, green, blue, hsv);

		this.mAlpha = alpha;
		this.mHue = hsv[0];
		this.mSat = hsv[1];
		this.mVal = hsv[2];

		if (callback && this.mListener != null)
		{
			this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
		}

		invalidate();
	}

	/**
	 * Get the drawing offset of the color picker view. The drawing offset is the distance from the side of a panel to the side of the view minus the padding.
	 * Useful if you want to have your own panel below showing the currently selected color and want to align it perfectly.
	 *
	 * @return The offset in pixels.
	 */
	public float getDrawingOffset()
	{
		return this.mDrawingOffset;
	}

	/**
	 * Set if the user is allowed to adjust the alpha panel. Default is false. If it is set to false no alpha will be set.
	 *
	 * @param visible true if slider is to be visible
	 */
	public void setAlphaSliderVisible(final boolean visible)
	{
		if (this.mShowAlphaPanel != visible)
		{
			this.mShowAlphaPanel = visible;

			/*
			 * Reset all shader to force a recreation. Otherwise they will not look right after the size of the view has changed.
			 */
			this.mValShader = null;
			this.mSatShader = null;
			this.mHueShader = null;
			this.mAlphaShader = null;

			requestLayout();
		}
	}

	/**
	 * Set the color of the tracker slider on the hue and alpha panel.
	 *
	 * @param color tracker color
	 */
	public void setSliderTrackerColor(final int color)
	{
		this.mSliderTrackerColor = color;
		this.mHueAlphaTrackerPaint.setColor(this.mSliderTrackerColor);
		invalidate();
	}

	/**
	 * Get color of the tracker slider on the hue and alpha panel.
	 *
	 * @return slider tracker color
	 */
	public int getSliderTrackerColor()
	{
		return this.mSliderTrackerColor;
	}

	/**
	 * Set the color of the border surrounding all panels.
	 *
	 * @param color border color
	 */
	public void setBorderColor(final int color)
	{
		this.mBorderColor = color;
		invalidate();
	}

	/**
	 * Get the color of the border surrounding all panels.
	 */
	public int getBorderColor()
	{
		return this.mBorderColor;
	}

	/**
	 * Set the text that should be shown in the alpha slider. Set to null to disable text.
	 *
	 * @param res string resource id.
	 */
	public void setAlphaSliderText(final int res)
	{
		final String text = getContext().getString(res);
		setAlphaSliderText(text);
	}

	/**
	 * Set the text that should be shown in the alpha slider. Set to null to disable text.
	 *
	 * @param text Text that should be shown.
	 */
	public void setAlphaSliderText(final String text)
	{
		this.mAlphaSliderText = text;
		invalidate();
	}

	/**
	 * Get the current value of the text that will be shown in the alpha slider.
	 *
	 * @return alpha slider text
	 */
	@Nullable
	public String getAlphaSliderText()
	{
		return this.mAlphaSliderText;
	}

	private class BitmapCache
	{
		public Canvas canvas;
		public Bitmap bitmap;
		public float value;
	}
}
