package org.treebolic.search;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Tint
 *
 * @author Bernard Bou
 */
public class Tint
{
	static private final String TAG = "Tint";

	static public void tint(final int iconTint, final Menu menu, final int... menuItemIds)
	{
		for (int menuItemId : menuItemIds)
		{
			final MenuItem menuItem = menu.findItem(menuItemId);
			final Drawable drawable = menuItem.getIcon();
			if (drawable != null)
			{
				tint(iconTint, drawable);
			}
		}
	}

	static public void tint(int iconTint, final Drawable drawable)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			drawable.setTint(iconTint);
		}
		else
		{
			DrawableCompat.setTint(DrawableCompat.wrap(drawable), iconTint);
		}
	}

	static public int fetchColor(final Context context, int attr)
	{
		dumpActionBarColor(context);
		final Resources.Theme theme = context.getTheme();
		final TypedValue typedValue = new TypedValue();
		theme.resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}

	/**
	 * Get color from style
	 *
	 * @param context context
	 * @param styleId style id (R.style.MyTheme)
	 * @param attr    attr id (R.attr.editTextColor)
	 * @return color
	 */
	static public int fetchColorFromStyle(final Context context, int styleId, int attr)
	{
		final TypedArray array = context.getTheme().obtainStyledAttributes(styleId, new int[]{attr});
		int intColor = array.getColor(0 /* index */, 0 /* defaultVal */);
		Log.d(TAG, "style resid=" + Integer.toHexString(styleId) + "color=" + Integer.toHexString(intColor));
		array.recycle();
		return intColor;
	}

	/**
	 * Get color from theme
	 *
	 * @param context     context
	 * @param styleId     style id (ex: R.style.MyTheme)
	 * @param colorAttrId attr id (ex: R.attr.editTextColor)
	 * @return color
	 */
	static public int getColorFromTheme(final Context context, int styleId, int colorAttrId)
	{
		final Resources.Theme theme = context.getTheme();
		// theme.dump(Log.DEBUG, TAG, "theme");

		// res id of style pointed to from actionBarStyle
		final TypedValue typedValue = new TypedValue();
		theme.resolveAttribute(styleId, typedValue, true);
		final int resId = typedValue.resourceId;
		Log.d(TAG, "actionBarStyle=" + Integer.toHexString(resId));

		// now get action bar style values
		final int[] attrs = new int[]{colorAttrId};
		final TypedArray style = theme.obtainStyledAttributes(resId, attrs);

		//
		try
		{
			final int intColor = style.getColor(0 /* index */, 0xCCCCCCCC /* defaultVal */);
			Log.d(TAG, theme + " attr=" + Integer.toHexString(attrs[0]) + " value=" + Integer.toHexString(intColor));
			return intColor;
		}
		finally
		{
			style.recycle();
		}
	}

	/**
	 * Get actionbar fore color from theme
	 *
	 * @param context context
	 * @return color
	 */
	static public int getActionBarForegroundColorFromTheme(final Context context)
	{
		int color =  getColorFromTheme(context, R.attr.actionBarTheme, android.R.attr.textColorPrimary);
		Log.d(TAG, "getActionBarForegroundColorFromTheme=0x" + Integer.toHexString(color));
		return color;
	}

	/**
	 * Dump background and fore colors from theme
	 *
	 * @param context context
	 */
	@SuppressWarnings("ResourceType")
	static private void dumpActionBarColor(final Context context)
	{
		final Resources.Theme theme = context.getTheme();
		theme.dump(Log.DEBUG, TAG, "theme");

		// res id of style pointed to from actionBarStyle
		final TypedValue typedValue = new TypedValue();
		theme.resolveAttribute(android.R.attr.actionBarStyle, typedValue, true);
		final int resId = typedValue.resourceId;
		Log.d(TAG, "actionBarStyle=" + Integer.toHexString(resId));

		// now get action bar style values
		final int[] attrs = new int[]{android.R.attr.background, android.R.attr.colorForeground};
		final TypedArray style = theme.obtainStyledAttributes(resId, attrs);

		//
		try
		{
			final Drawable drawable = style.getDrawable(0);
			for (int i = 1; i < attrs.length; i++)
			{
				final int intColor = style.getColor(i /* index */, (int) 0xCCCCCCCC /* defaultVal */);
				Log.d(TAG, theme + " attr=" + Integer.toHexString(attrs[i]) + " value=" + Integer.toHexString(intColor));
			}
		}
		finally
		{
			style.recycle();
		}
	}
}
