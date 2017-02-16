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
package org.treebolic.wheel.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Abstract spinnerwheel adapter provides common functionality for adapters.
 */
public abstract class AbstractWheelTextAdapter extends AbstractWheelAdapter
{
	/** Text view resource. Used as a default view for adapter. */
	public static final int TEXT_VIEW_ITEM_RESOURCE = -1;

	/** No resource constant. */
	protected static final int NO_RESOURCE = 0;

	/** Default text color */
	public static final int DEFAULT_TEXT_COLOR = 0xFF101010;

	/** Default text size */
	public static final int DEFAULT_TEXT_SIZE = 24;

	// / Custom text typeface
	private Typeface textTypeface;

	// Text settings
	private int textColor = DEFAULT_TEXT_COLOR;
	private int textSize = DEFAULT_TEXT_SIZE;

	// Current context
	protected final Context context;
	// Layout inflater
	protected final LayoutInflater inflater;

	// Items resources
	protected int itemResourceId;
	protected int itemTextResourceId;

	// Empty items resources
	protected int emptyItemResourceId;

	/**
	 * Constructor
	 * 
	 * @param context0
	 *            the current context
	 */
	protected AbstractWheelTextAdapter(Context context0)
	{
		this(context0, TEXT_VIEW_ITEM_RESOURCE);
	}

	/**
	 * Constructor
	 * 
	 * @param context0
	 *            the current context
	 * @param itemResource
	 *            the resource ID for a layout file containing a TextView to use when instantiating items views
	 */
	protected AbstractWheelTextAdapter(Context context0, int itemResource)
	{
		this(context0, itemResource, NO_RESOURCE);
	}

	/**
	 * Constructor
	 * 
	 * @param context0
	 *            the current context
	 * @param itemResource
	 *            the resource ID for a layout file containing a TextView to use when instantiating items views
	 * @param itemTextResource
	 *            the resource ID for a text view in the item layout
	 */
	protected AbstractWheelTextAdapter(Context context0, int itemResource, int itemTextResource)
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
	 * @param textColor0
	 *            the text color to set
	 */
	public void setTextColor(int textColor0)
	{
		this.textColor = textColor0;
	}

	/**
	 * Sets text typeface
	 * 
	 * @param typeface
	 *            typeface to set
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
	 * @param textSize0
	 *            the text size to set
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
	 * @param itemResourceId0
	 *            the resource Id to set
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
	 * @param itemTextResourceId0
	 *            the item text resource Id to set
	 */
	public void setItemTextResource(int itemTextResourceId0)
	{
		this.itemTextResourceId = itemTextResourceId0;
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
	 * @param emptyItemResourceId0
	 *            the empty item resource Id to set
	 */
	public void setEmptyItemResource(int emptyItemResourceId0)
	{
		this.emptyItemResourceId = emptyItemResourceId0;
	}

	/**
	 * Returns text for specified item
	 * 
	 * @param index
	 *            the item index
	 * @return the text of specified items
	 */
	protected abstract CharSequence getItemText(int index);

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
					text = ""; //$NON-NLS-1$
				}
				textView.setText(text);
				configureTextView(textView);
			}
			return convertView;
		}
		return null;
	}

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
	 * @param view
	 *            the text view to be configured
	 */
	protected void configureTextView(TextView view)
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
	 * @param view
	 *            the text view or layout containing it
	 * @param textResource
	 *            the text resource Id in layout
	 * @return the loaded text view
	 */
	private static TextView getTextView(View view, int textResource)
	{
		TextView text = null;
		try
		{
			if (textResource == NO_RESOURCE && view instanceof TextView)
			{
				text = (TextView) view;
			}
			else if (textResource != NO_RESOURCE)
			{
				text = (TextView) view.findViewById(textResource);
			}
		}
		catch (ClassCastException e)
		{
			Log.e("AbstractWheelAdapter", "You must supply a resource ID for a TextView"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new IllegalStateException("AbstractWheelAdapter requires the resource ID to be a TextView", e); //$NON-NLS-1$
		}

		return text;
	}

	/**
	 * Loads view from resources
	 * 
	 * @param resource
	 *            the resource Id
	 * @return the loaded view or null if resource is not set
	 */
	private View getView(int resource, ViewGroup parent)
	{
		switch (resource)
		{
		case NO_RESOURCE:
			return null;
		case TEXT_VIEW_ITEM_RESOURCE:
			return new TextView(this.context);
		default:
			return this.inflater.inflate(resource, parent, false);
		}
	}
}
