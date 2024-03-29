/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Abstract spinnerwheel adapter provides common functionality for adapters.
 */
public abstract class AbstractWheelTextAdapter extends AbstractWheelAdapter
{
	/**
	 * Text view resource. Used as a default view for adapter.
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int TEXT_VIEW_ITEM_RESOURCE = -1;

	/**
	 * No resource constant.
	 */
	protected static final int NO_RESOURCE = 0;

	/**
	 * Default text color
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int DEFAULT_TEXT_COLOR = 0xFF101010;

	/**
	 * Default text size
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int DEFAULT_TEXT_SIZE = 24;

	// / Custom text typeface
	private Typeface textTypeface;

	// Text settings
	private int textColor = DEFAULT_TEXT_COLOR;
	private int textSize = DEFAULT_TEXT_SIZE;

	// Current context
	@NonNull
	@SuppressWarnings("WeakerAccess")
	protected final Context context;

	// Layout inflater
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected final LayoutInflater inflater;

	// Items resources
	@SuppressWarnings("WeakerAccess")
	protected int itemResourceId;
	@SuppressWarnings("WeakerAccess")
	protected int itemTextResourceId;

	// Empty items resources
	@SuppressWarnings("WeakerAccess")
	protected int emptyItemResourceId;

	/**
	 * Constructor
	 *
	 * @param context0 the current context
	 */
	@SuppressWarnings("WeakerAccess")
	protected AbstractWheelTextAdapter(@NonNull Context context0)
	{
		this(context0, TEXT_VIEW_ITEM_RESOURCE);
	}

	/**
	 * Constructor
	 *
	 * @param context0     the current context
	 * @param itemResource the resource ID for a layout file containing a TextView to use when instantiating items views
	 */
	@SuppressWarnings("WeakerAccess")
	protected AbstractWheelTextAdapter(@NonNull Context context0, @SuppressWarnings("SameParameterValue") int itemResource)
	{
		this(context0, itemResource, NO_RESOURCE);
	}

	/**
	 * Constructor
	 *
	 * @param context0         the current context
	 * @param itemResource     the resource ID for a layout file containing a TextView to use when instantiating items views
	 * @param itemTextResource the resource ID for a text view in the item layout
	 */
	protected AbstractWheelTextAdapter(@NonNull Context context0, int itemResource, @SuppressWarnings("SameParameterValue") int itemTextResource)
	{
		this.context = context0;
		this.itemResourceId = itemResource;
		this.itemTextResourceId = itemTextResource;

		this.inflater = (LayoutInflater) context0.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Gets text color
	 *
	 * @return the text color
	 */
	public int getTextColor()
	{
		return this.textColor;
	}

	/**
	 * Sets text color
	 *
	 * @param textColor0 the text color to set
	 */
	public void setTextColor(int textColor0)
	{
		this.textColor = textColor0;
	}

	/**
	 * Sets text typeface
	 *
	 * @param typeface typeface to set
	 */
	public void setTextTypeface(Typeface typeface)
	{
		this.textTypeface = typeface;
	}

	/**
	 * Gets text size
	 *
	 * @return the text size
	 */
	public int getTextSize()
	{
		return this.textSize;
	}

	/**
	 * Sets text size
	 *
	 * @param textSize0 the text size to set
	 */
	public void setTextSize(int textSize0)
	{
		this.textSize = textSize0;
	}

	/**
	 * Gets resource Id for items views
	 *
	 * @return the item resource Id
	 */
	public int getItemResource()
	{
		return this.itemResourceId;
	}

	/**
	 * Sets resource Id for items views
	 *
	 * @param itemResourceId0 the resource Id to set
	 */
	public void setItemResource(int itemResourceId0)
	{
		this.itemResourceId = itemResourceId0;
	}

	/**
	 * Gets resource Id for text view in item layout
	 *
	 * @return the item text resource Id
	 */
	public int getItemTextResource()
	{
		return this.itemTextResourceId;
	}

	/**
	 * Sets resource Id for text view in item layout
	 *
	 * @param itemTextResId the item text resource Id to set
	 */
	@SuppressWarnings("WeakerAccess")
	public void setItemTextResource(@IdRes int itemTextResId)
	{
		this.itemTextResourceId = itemTextResId;
	}

	/**
	 * Gets resource Id for empty items views
	 *
	 * @return the empty item resource Id
	 */
	public int getEmptyItemResource()
	{
		return this.emptyItemResourceId;
	}

	/**
	 * Sets resource Id for empty items views
	 *
	 * @param emptyItemResourceId0 the empty item resource Id to set
	 */
	public void setEmptyItemResource(int emptyItemResourceId0)
	{
		this.emptyItemResourceId = emptyItemResourceId0;
	}

	/**
	 * Returns text for specified item
	 *
	 * @param index the item index
	 * @return the text of specified items
	 */
	@Nullable
	protected abstract CharSequence getItemText(int index);

	@Nullable
	@Override
	public View getItem(int index, View convertView0, ViewGroup parent)
	{
		View convertView = convertView0;
		if (index >= 0 && index < getItemsCount())
		{
			if (convertView == null)
			{
				convertView = getView(this.itemResourceId, parent);
			}
			TextView textView = getTextView(convertView, this.itemTextResourceId);
			if (textView != null)
			{
				CharSequence text = getItemText(index);
				if (text == null)
				{
					text = "";
				}
				textView.setText(text);
				configureTextView(textView);
			}
			return convertView;
		}
		return null;
	}

	@Nullable
	@Override
	public View getEmptyItem(View convertView0, ViewGroup parent)
	{
		View convertView = convertView0;
		if (convertView == null)
		{
			convertView = getView(this.emptyItemResourceId, parent);
		}
		if (convertView instanceof TextView)
		{
			configureTextView((TextView) convertView);
		}

		return convertView;
	}

	/**
	 * Configures text view. Is called for the TEXT_VIEW_ITEM_RESOURCE views.
	 *
	 * @param view the text view to be configured
	 */
	@SuppressWarnings("WeakerAccess")
	protected void configureTextView(@NonNull TextView view)
	{
		if (this.itemResourceId == TEXT_VIEW_ITEM_RESOURCE)
		{
			view.setTextColor(this.textColor);
			view.setGravity(Gravity.CENTER);
			view.setTextSize(this.textSize);
			view.setLines(1);
		}
		if (this.textTypeface != null)
		{
			view.setTypeface(this.textTypeface);
		}
		else
		{
			view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		}
	}

	/**
	 * Loads a text view from view
	 *
	 * @param view      the text view or layout containing it
	 * @param textResId the text resource Id in layout
	 * @return the loaded text view
	 */
	@Nullable
	private static TextView getTextView(View view, @IdRes int textResId)
	{
		TextView text = null;
		try
		{
			if (textResId == NO_RESOURCE && view instanceof TextView)
			{
				text = (TextView) view;
			}
			else if (textResId != NO_RESOURCE)
			{
				text = view.findViewById(textResId);
			}
		}
		catch (ClassCastException e)
		{
			Log.e("AbstractWheelAdapter", "You must supply a resource ID for a TextView");
			throw new IllegalStateException("AbstractWheelAdapter requires the resource ID to be a TextView", e);
		}

		return text;
	}

	/**
	 * Loads view from resources
	 *
	 * @param layoutRes the resource Id
	 * @return the loaded view or null if resource is not set
	 */
	@Nullable
	private View getView(@LayoutRes int layoutRes, ViewGroup parent)
	{
		switch (layoutRes)
		{
			case NO_RESOURCE:
				return null;
			case TEXT_VIEW_ITEM_RESOURCE:
				return new TextView(this.context);
			default:
				assert this.inflater != null;
				return this.inflater.inflate(layoutRes, parent, false);
		}
	}
}
