/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.colors.view;

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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.treebolic.colors.R;
import org.treebolic.colors.drawable.AlphaPatternDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * Displays a value picker to the user and allow them to select a value. A slider for the alpha channel is also available. Enable it by setting
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
	 * The width in pixels of the border surrounding all value panels.
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
	 * The distance in dp between the different value panels.
	 */
	private float PANEL_SPACING = 10f;

	/**
	 * The radius in dp of the value palette tracker circle.
	 */
	private float PALETTE_CIRCLE_TRACKER_RADIUS = 5f;

	/**
	 * The dp which the tracker of the hue or alpha panel will extend outside of its bounds.
	 */
	private float RECTANGLE_TRACKER_OFFSET = 2f;

	/**
	 * Density
	 */
	private static float density = 1f;

	// P A I N T S

	/**
	 * Saturation paint
	 */
	private Paint satValPaint;

	/**
	 * Saturation tracker paint
	 */
	private Paint satValTrackerPaint;

	/**
	 * Hue paint
	 */
	private Paint huePaint;

	/**
	 * Hue tracker paint
	 */
	private Paint hueAlphaTrackerPaint;

	/**
	 * Alpha paint
	 */
	private Paint alphaPaint;

	/**
	 * Alpha text paint
	 */
	private Paint alphaTextPaint;

	/**
	 * Border paint
	 */
	private Paint borderPaint;

	// S H A D E R S

	/**
	 * Value shader
	 */
	@Nullable
	private Shader valShader;

	/**
	 * Saturation shader
	 */
	@Nullable
	private Shader satShader;

	/**
	 * Hue shader
	 */
	@Nullable
	private Shader hueShader;

	/**
	 * Alpha shader
	 */
	@Nullable
	private Shader alphaShader;

	/*
	 * We cache a bitmap of the sat/val panel which is expensive to draw each time. We can reuse it when the user is sliding the circle picker as long as the hue isn't changed.
	 */
	private BitmapCache satValBackgroundCache;

	// V A L U E S

	private int alpha = 0xff;

	private float hue = 360f;

	private float sat = 0f;

	private float val = 0f;

	// S E T T I N G S
	/**
	 * Alpha slider text
	 */
	@Nullable
	private String alphaSliderText = null;

	/**
	 * Show alpha panel
	 */
	private boolean showAlphaPanel = false;

	/**
	 * Slider tracker value
	 */
	private int sliderTrackerColor = 0xFFBDBDBD;

	/**
	 * Border value
	 */
	private int borderColor = 0xFF6E6E6E;

	// S T A T E

	/**
	 * To remember which panel that has the "focus" when processing hardware button data.
	 */
	private int lastTouchedPanel = ColorPickerView.PANEL_SAT_VAL;

	/**
	 * Start touch point
	 */
	@Nullable
	private Point startTouchPoint = null;

	/**
	 * Offset from the edge we must have or else the finger tracker will get clipped when it is drawn outside of the view.
	 */
	private int drawingOffset;

	// L I S T E N E R

	/**
	 * Listener
	 */
	private OnColorChangedListener listener;

	// R E C T A N G L E S

	/**
	 * Distance form the edges of the view of where we are allowed to draw.
	 */
	private RectF drawingRect;

	/**
	 * Saturation rectangle
	 */
	private RectF satValRect;

	/**
	 * Hue rectangle
	 */
	private RectF hueRect;

	/**
	 * Alpha rectangle
	 */
	private RectF alphaRect;

	// P A T T E R N

	/**
	 * Alpha pattern
	 */
	private AlphaPatternDrawable alphaPattern;

	/**
	 * Constructor
	 *
	 * @param context context
	 */
	public ColorPickerView(@NonNull final Context context)
	{
		this(context, null);
	}

	/**
	 * Constructor
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	public ColorPickerView(@NonNull final Context context, final AttributeSet attrs)
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
	public ColorPickerView(@NonNull final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	/**
	 * Common init
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	private void init(@NonNull final Context context, final AttributeSet attrs)
	{
		// Load those if set in xml resource file.
		final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
		this.showAlphaPanel = array.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false);
		this.alphaSliderText = array.getString(R.styleable.ColorPickerView_alphaChannelText);
		this.sliderTrackerColor = array.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, 0xFFBDBDBD);
		this.borderColor = array.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, 0xFF6E6E6E);
		array.recycle();

		ColorPickerView.density = context.getResources().getDisplayMetrics().density;
		this.PALETTE_CIRCLE_TRACKER_RADIUS *= ColorPickerView.density;
		this.RECTANGLE_TRACKER_OFFSET *= ColorPickerView.density;
		this.HUE_PANEL_WIDTH *= ColorPickerView.density;
		this.ALPHA_PANEL_HEIGHT *= ColorPickerView.density;
		this.PANEL_SPACING = this.PANEL_SPACING * ColorPickerView.density;

		this.drawingOffset = calculateRequiredOffset();

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
		this.satValPaint = new Paint();
		this.satValTrackerPaint = new Paint();
		this.huePaint = new Paint();
		this.hueAlphaTrackerPaint = new Paint();
		this.alphaPaint = new Paint();
		this.alphaTextPaint = new Paint();
		this.borderPaint = new Paint();

		this.satValTrackerPaint.setStyle(Style.STROKE);
		this.satValTrackerPaint.setStrokeWidth(2f * ColorPickerView.density);
		this.satValTrackerPaint.setAntiAlias(true);

		this.hueAlphaTrackerPaint.setColor(this.sliderTrackerColor);
		this.hueAlphaTrackerPaint.setStyle(Style.STROKE);
		this.hueAlphaTrackerPaint.setStrokeWidth(2f * ColorPickerView.density);
		this.hueAlphaTrackerPaint.setAntiAlias(true);

		this.alphaTextPaint.setColor(0xff1c1c1c);
		this.alphaTextPaint.setTextSize(14f * ColorPickerView.density);
		this.alphaTextPaint.setAntiAlias(true);
		this.alphaTextPaint.setTextAlign(Align.CENTER);
		this.alphaTextPaint.setFakeBoldText(true);
	}

	private int calculateRequiredOffset()
	{
		float offset = Math.max(this.PALETTE_CIRCLE_TRACKER_RADIUS, this.RECTANGLE_TRACKER_OFFSET);
		offset = Math.max(offset, ColorPickerView.BORDER_WIDTH_PX * ColorPickerView.density);

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
		if (this.drawingRect.width() <= 0 || this.drawingRect.height() <= 0)
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

		final RectF rect = this.satValRect;

		//noinspection ConstantConditions
		if (ColorPickerView.BORDER_WIDTH_PX > 0)
		{
			this.borderPaint.setColor(this.borderColor);
			canvas.drawRect(this.drawingRect.left, this.drawingRect.top, rect.right + ColorPickerView.BORDER_WIDTH_PX, rect.bottom + ColorPickerView.BORDER_WIDTH_PX, this.borderPaint);
		}

		if (this.valShader == null)
		{
			// Black gradient has either not been created or the view has been resized.
			this.valShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, 0xffffffff, 0xff000000, TileMode.CLAMP);
		}

		// If the hue has changed we need to recreate the cache.
		if (this.satValBackgroundCache == null || this.satValBackgroundCache.value != this.hue)
		{

			if (this.satValBackgroundCache == null)
			{
				this.satValBackgroundCache = new BitmapCache();
			}

			// We create our bitmap in the cache if it doesn't exist.
			if (this.satValBackgroundCache.bitmap == null)
			{
				this.satValBackgroundCache.bitmap = Bitmap.createBitmap((int) rect.width(), (int) rect.height(), Config.ARGB_8888);
			}

			// We create the canvas once so we can draw on our bitmap and the hold on to it.
			if (this.satValBackgroundCache.canvas == null)
			{
				this.satValBackgroundCache.canvas = new Canvas(this.satValBackgroundCache.bitmap);
			}

			final int rgb = Color.HSVToColor(new float[]{this.hue, 1f, 1f});

			this.satShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, 0xffffffff, rgb, TileMode.CLAMP);

			final ComposeShader mShader = new ComposeShader(this.valShader, this.satShader, PorterDuff.Mode.MULTIPLY);
			this.satValPaint.setShader(mShader);

			// Finally we draw on our canvas, the result will be stored in our bitmap which is already in the cache.
			// Since this is drawn on a canvas not rendered on screen it will automatically not be using the hardware acceleration.
			// And this was the code that wasn't supported by hardware acceleration which mean there is no need to turn it of anymore.
			// The rest of the view will still be hardware accelerated!!
			this.satValBackgroundCache.canvas.drawRect(0, 0, this.satValBackgroundCache.bitmap.getWidth(), this.satValBackgroundCache.bitmap.getHeight(), this.satValPaint);

			// We set the hue value in our cache to which hue it was drawn with,
			// then we know that if it hasn't changed we can reuse our cached bitmap.
			this.satValBackgroundCache.value = this.hue;

		}

		// We draw our bitmap from the cached, if the hue has changed
		// then it was just recreated otherwise the old one will be used.
		canvas.drawBitmap(this.satValBackgroundCache.bitmap, null, rect, null);

		final Point p = satValToPoint(this.sat, this.val);

		this.satValTrackerPaint.setColor(0xff000000);
		canvas.drawCircle(p.x, p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS - 1f * ColorPickerView.density, this.satValTrackerPaint);

		this.satValTrackerPaint.setColor(0xffdddddd);
		canvas.drawCircle(p.x, p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS, this.satValTrackerPaint);
	}

	private void drawHuePanel(@NonNull final Canvas canvas)
	{
		/*
		 * Drawn with hw acceleration, very fast.
		 */
		final RectF rect = this.hueRect;

		//noinspection ConstantConditions
		if (ColorPickerView.BORDER_WIDTH_PX > 0)
		{
			this.borderPaint.setColor(this.borderColor);
			canvas.drawRect(rect.left - ColorPickerView.BORDER_WIDTH_PX, rect.top - ColorPickerView.BORDER_WIDTH_PX, rect.right + ColorPickerView.BORDER_WIDTH_PX, rect.bottom + ColorPickerView.BORDER_WIDTH_PX, this.borderPaint);
		}

		if (this.hueShader == null)
		{
			// The hue shader has either not yet been created or the view has been resized.
			this.hueShader = new LinearGradient(0, 0, 0, rect.height(), ColorPickerView.buildHueColorArray(), null, TileMode.CLAMP);
			this.huePaint.setShader(this.hueShader);
		}

		canvas.drawRect(rect, this.huePaint);

		final float rectHeight = 4 * ColorPickerView.density / 2;

		final Point p = hueToPoint(this.hue);

		final RectF r = new RectF();
		r.left = rect.left - this.RECTANGLE_TRACKER_OFFSET;
		r.right = rect.right + this.RECTANGLE_TRACKER_OFFSET;
		r.top = p.y - rectHeight;
		r.bottom = p.y + rectHeight;

		canvas.drawRoundRect(r, 2, 2, this.hueAlphaTrackerPaint);
	}

	private void drawAlphaPanel(@NonNull final Canvas canvas)
	{
		/*
		 * Will be drawn with hw acceleration, very fast.
		 */

		if (!this.showAlphaPanel || this.alphaRect == null || this.alphaPattern == null)
		{
			return;
		}

		final RectF rect = this.alphaRect;

		//noinspection ConstantConditions
		if (ColorPickerView.BORDER_WIDTH_PX > 0)
		{
			this.borderPaint.setColor(this.borderColor);
			canvas.drawRect(rect.left - ColorPickerView.BORDER_WIDTH_PX, rect.top - ColorPickerView.BORDER_WIDTH_PX, rect.right + ColorPickerView.BORDER_WIDTH_PX, rect.bottom + ColorPickerView.BORDER_WIDTH_PX, this.borderPaint);
		}

		this.alphaPattern.draw(canvas);

		final float[] hsv = new float[]{this.hue, this.sat, this.val};
		final int color = Color.HSVToColor(hsv);
		final int acolor = Color.HSVToColor(0, hsv);

		this.alphaShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, color, acolor, TileMode.CLAMP);

		this.alphaPaint.setShader(this.alphaShader);

		canvas.drawRect(rect, this.alphaPaint);

		if (this.alphaSliderText != null && !this.alphaSliderText.equals(""))
		{
			canvas.drawText(this.alphaSliderText, rect.centerX(), rect.centerY() + 4 * ColorPickerView.density, this.alphaTextPaint);
		}

		final float rectWidth = 4 * ColorPickerView.density / 2;

		final Point p = alphaToPoint(this.alpha);

		final RectF r = new RectF();
		r.left = p.x - rectWidth;
		r.right = p.x + rectWidth;
		r.top = rect.top - this.RECTANGLE_TRACKER_OFFSET;
		r.bottom = rect.bottom + this.RECTANGLE_TRACKER_OFFSET;

		canvas.drawRoundRect(r, 2, 2, this.hueAlphaTrackerPaint);
	}

	@NonNull
	private Point hueToPoint(final float hue)
	{
		final RectF rect = this.hueRect;
		final float height = rect.height();

		final Point p = new Point();

		p.y = (int) (height - hue * height / 360f + rect.top);
		p.x = (int) rect.left;

		return p;
	}

	@NonNull
	private Point satValToPoint(final float sat, final float val)
	{
		final RectF rect = this.satValRect;
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

		final RectF rect = this.alphaRect;
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
		final RectF rect = this.satValRect;
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
		final RectF rect = this.hueRect;

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
		final RectF rect = this.alphaRect;
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

			switch (this.lastTouchedPanel)
			{
				case PANEL_SAT_VAL:

					float sat, val;

					sat = this.sat + x / 50f;
					val = this.val - y / 50f;

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

					this.sat = sat;
					this.val = val;

					update = true;

					break;

				case PANEL_HUE:

					float hue = this.hue - y * 10f;

					if (hue < 0f)
					{
						hue = 0f;
					}
					else if (hue > 360f)
					{
						hue = 360f;
					}

					this.hue = hue;

					update = true;

					break;

				case PANEL_ALPHA:

					if (!this.showAlphaPanel || this.alphaRect == null)
					{
						update = false;
					}
					else
					{

						int alpha = (int) (this.alpha - x * 10);

						if (alpha < 0)
						{
							alpha = 0;
						}
						else if (alpha > 0xff)
						{
							alpha = 0xff;
						}

						this.alpha = alpha;

						update = true;
					}

					break;
				default:
					break;
			}
		}

		if (update)
		{

			if (this.listener != null)
			{
				this.listener.onColorChanged(Color.HSVToColor(this.alpha, new float[]{this.hue, this.sat, this.val}));
			}

			invalidate();
			return true;
		}

		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onTouchEvent(@NonNull final MotionEvent event)
	{
		boolean update = false;

		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				this.startTouchPoint = new Point((int) event.getX(), (int) event.getY());
				update = moveTrackersIfNeeded(event);
				break;

			case MotionEvent.ACTION_MOVE:
				update = moveTrackersIfNeeded(event);
				break;
			case MotionEvent.ACTION_UP:
				this.startTouchPoint = null;
				update = moveTrackersIfNeeded(event);
				break;
			default:
				break;
		}

		if (update)
		{
			if (this.listener != null)
			{
				this.listener.onColorChanged(Color.HSVToColor(this.alpha, new float[]{this.hue, this.sat, this.val}));
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

	/*
	@Override
	public boolean performClick()
	{
		return super.performClick();
	}
	*/

	private boolean moveTrackersIfNeeded(@NonNull final MotionEvent event)
	{
		if (this.startTouchPoint == null)
		{
			return false;
		}

		boolean update = false;

		final int startX = this.startTouchPoint.x;
		final int startY = this.startTouchPoint.y;

		if (this.hueRect.contains(startX, startY))
		{
			this.lastTouchedPanel = ColorPickerView.PANEL_HUE;

			this.hue = pointToHue(event.getY());

			update = true;
		}
		else if (this.satValRect.contains(startX, startY))
		{

			this.lastTouchedPanel = ColorPickerView.PANEL_SAT_VAL;

			final float[] result = pointToSatVal(event.getX(), event.getY());

			this.sat = result[0];
			this.val = result[1];

			update = true;
		}
		else if (this.alphaRect != null && this.alphaRect.contains(startX, startY))
		{
			this.lastTouchedPanel = ColorPickerView.PANEL_ALPHA;

			this.alpha = pointToAlpha((int) event.getX());

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

		// Log.d("value-picker-view", "widthMode: " + modeToString(widthMode) + " heightMode: " + modeToString(heightMode) + " widthAllowed: " + widthAllowed +
		// " heightAllowed: " + heightAllowed);

		if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY)
		{
			// A exact value has been set in either direction, we need to stay within this size.

			if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY)
			{
				// The with has been specified exactly, we need to adopt the height to fit.
				int h = (int) (widthAllowed - this.PANEL_SPACING - this.HUE_PANEL_WIDTH);

				if (this.showAlphaPanel)
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

					if (this.showAlphaPanel)
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

			if (this.showAlphaPanel)
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
	protected void onSizeChanged(final int w, final int h, final int oldW, final int oldH)
	{
		super.onSizeChanged(w, h, oldW, oldH);

		this.drawingRect = new RectF();
		this.drawingRect.left = this.drawingOffset + getPaddingLeft();
		this.drawingRect.right = w - this.drawingOffset - getPaddingRight();
		this.drawingRect.top = this.drawingOffset + getPaddingTop();
		this.drawingRect.bottom = h - this.drawingOffset - getPaddingBottom();

		// The need to be recreated because they depend on the size of the view.
		this.valShader = null;
		this.satShader = null;
		this.hueShader = null;
		this.alphaShader = null;

		setUpSatValRect();
		setUpHueRect();
		setUpAlphaRect();
	}

	private void setUpSatValRect()
	{
		// Calculate the size for the big value rectangle.
		final RectF dRect = this.drawingRect;

		final float left = dRect.left + ColorPickerView.BORDER_WIDTH_PX;
		final float top = dRect.top + ColorPickerView.BORDER_WIDTH_PX;
		float bottom = dRect.bottom - ColorPickerView.BORDER_WIDTH_PX;
		final float right = dRect.right - ColorPickerView.BORDER_WIDTH_PX - this.PANEL_SPACING - this.HUE_PANEL_WIDTH;

		if (this.showAlphaPanel)
		{
			bottom -= this.ALPHA_PANEL_HEIGHT + this.PANEL_SPACING;
		}

		this.satValRect = new RectF(left, top, right, bottom);
	}

	private void setUpHueRect()
	{
		// Calculate the size for the hue slider on the left.
		final RectF dRect = this.drawingRect;

		final float left = dRect.right - this.HUE_PANEL_WIDTH + ColorPickerView.BORDER_WIDTH_PX;
		final float top = dRect.top + ColorPickerView.BORDER_WIDTH_PX;
		final float bottom = dRect.bottom - ColorPickerView.BORDER_WIDTH_PX - (this.showAlphaPanel ? this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT : 0);
		final float right = dRect.right - ColorPickerView.BORDER_WIDTH_PX;

		this.hueRect = new RectF(left, top, right, bottom);
	}

	private void setUpAlphaRect()
	{
		if (!this.showAlphaPanel)
		{
			return;
		}

		final RectF dRect = this.drawingRect;

		final float left = dRect.left + ColorPickerView.BORDER_WIDTH_PX;
		final float top = dRect.bottom - this.ALPHA_PANEL_HEIGHT + ColorPickerView.BORDER_WIDTH_PX;
		final float bottom = dRect.bottom - ColorPickerView.BORDER_WIDTH_PX;
		final float right = dRect.right - ColorPickerView.BORDER_WIDTH_PX;

		this.alphaRect = new RectF(left, top, right, bottom);

		this.alphaPattern = new AlphaPatternDrawable((int) (5 * ColorPickerView.density));
		this.alphaPattern.setBounds(Math.round(this.alphaRect.left), Math.round(this.alphaRect.top), Math.round(this.alphaRect.right), Math.round(this.alphaRect.bottom));
	}

	/**
	 * Set a OnColorChangedListener to get notified when the value selected by the user has changed.
	 *
	 * @param listener change listener
	 */
	public void setOnColorChangedListener(final OnColorChangedListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Get the current value this view is showing.
	 *
	 * @return the current value.
	 */
	public int getColor()
	{
		return Color.HSVToColor(this.alpha, new float[]{this.hue, this.sat, this.val});
	}

	/**
	 * Set the value the view should show.
	 *
	 * @param color The value that should be selected.
	 */
	public void setColor(final int color)
	{
		setColor(color, false);
	}

	/**
	 * Set the value this view should show.
	 *
	 * @param color    The value that should be selected.
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

		this.alpha = alpha;
		this.hue = hsv[0];
		this.sat = hsv[1];
		this.val = hsv[2];

		if (callback && this.listener != null)
		{
			this.listener.onColorChanged(Color.HSVToColor(this.alpha, new float[]{this.hue, this.sat, this.val}));
		}

		invalidate();
	}

	/**
	 * Get the drawing offset of the value picker view. The drawing offset is the distance from the side of a panel to the side of the view minus the padding.
	 * Useful if you want to have your own panel below showing the currently selected value and want to align it perfectly.
	 *
	 * @return The offset in pixels.
	 */
	public float getDrawingOffset()
	{
		return this.drawingOffset;
	}

	/**
	 * Set if the user is allowed to adjust the alpha panel. Default is false. If it is set to false no alpha will be set.
	 *
	 * @param visible true if slider is to be visible
	 */
	public void setAlphaSliderVisible(final boolean visible)
	{
		if (this.showAlphaPanel != visible)
		{
			this.showAlphaPanel = visible;

			/*
			 * Reset all shader to force a recreation. Otherwise they will not look right after the size of the view has changed.
			 */
			this.valShader = null;
			this.satShader = null;
			this.hueShader = null;
			this.alphaShader = null;

			requestLayout();
		}
	}

	/**
	 * Set the value of the tracker slider on the hue and alpha panel.
	 *
	 * @param color tracker value
	 */
	public void setSliderTrackerColor(final int color)
	{
		this.sliderTrackerColor = color;
		this.hueAlphaTrackerPaint.setColor(this.sliderTrackerColor);
		invalidate();
	}

	/**
	 * Get value of the tracker slider on the hue and alpha panel.
	 *
	 * @return slider tracker value
	 */
	public int getSliderTrackerColor()
	{
		return this.sliderTrackerColor;
	}

	/**
	 * Set the value of the border surrounding all panels.
	 *
	 * @param color border value
	 */
	public void setBorderColor(final int color)
	{
		this.borderColor = color;
		invalidate();
	}

	/**
	 * Get the value of the border surrounding all panels.
	 */
	public int getBorderColor()
	{
		return this.borderColor;
	}

	/**
	 * Set the text that should be shown in the alpha slider. Set to null to disable text.
	 *
	 * @param stringRes string resource id.
	 */
	public void setAlphaSliderText(@StringRes final int stringRes)
	{
		final String text = getContext().getString(stringRes);
		setAlphaSliderText(text);
	}

	/**
	 * Set the text that should be shown in the alpha slider. Set to null to disable text.
	 *
	 * @param text Text that should be shown.
	 */
	public void setAlphaSliderText(final String text)
	{
		this.alphaSliderText = text;
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
		return this.alphaSliderText;
	}

	private class BitmapCache
	{
		@SuppressWarnings("WeakerAccess")
		public Canvas canvas;
		@SuppressWarnings("WeakerAccess")
		public Bitmap bitmap;
		@SuppressWarnings("WeakerAccess")
		public float value;
	}
}
