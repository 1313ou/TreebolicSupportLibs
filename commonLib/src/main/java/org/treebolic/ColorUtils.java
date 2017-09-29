package org.treebolic;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import org.treebolic.common.R;

/**
 * Tint
 *
 * @author Bernard Bou
 */
public class ColorUtils
{
	// static private final String TAG = "ColorUtils";

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

	@SuppressWarnings("WeakerAccess")
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
		// Log.d(TAG, "style resid=" + Integer.toHexString(styleId) + "color=" + Integer.toHexString(intColor));
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
	@SuppressWarnings("WeakerAccess")
	static public int getColorFromTheme(final Context context, int styleId, @SuppressWarnings("SameParameterValue") int colorAttrId)
	{
		final Resources.Theme theme = context.getTheme();
		// theme.dump(Log.DEBUG, TAG, "theme");

		// res id of style pointed to from actionBarStyle
		final TypedValue typedValue = new TypedValue();
		theme.resolveAttribute(styleId, typedValue, true);
		final int resId = typedValue.resourceId;
		// Log.d(TAG, "actionBarStyle=" + Integer.toHexString(resId));

		// now get action bar style values
		final int[] attrs = new int[]{colorAttrId};
		final TypedArray style = theme.obtainStyledAttributes(resId, attrs);

		//
		try
		{
			// Log.d(TAG, theme + " attr=" + Integer.toHexString(attrs[0]) + " value=" + Integer.toHexString(intColor));
			return style.getColor(0 /* index */, 0xCCCCCCCC /* defaultVal */);
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
		// Log.d(TAG, "getActionBarForegroundColorFromTheme=0x" + Integer.toHexString(color));
		return getColorFromTheme(context, R.attr.actionBarTheme, android.R.attr.textColorPrimary);
	}

	/*
	 * Dump background and fore colors from theme
	 *
	 * @param context context
	 */
	/*
	@SuppressWarnings("ResourceType")
	static private void dumpActionBarColor(final Context context)
	{
		final Resources.Theme theme = context.getTheme();
		theme.dump(Log.DEBUG, TAG, "theme");

		// res id of style pointed to from actionBarStyle
		final TypedValue typedValue = new TypedValue();
		theme.resolveAttribute(android.R.attr.actionBarStyle, typedValue, true);
		final int resId = typedValue.resourceId;
		//Log.d(TAG, "actionBarStyle=" + Integer.toHexString(resId));

		// now get action bar style values
		final int[] attrs = new int[]{android.R.attr.background, android.R.attr.colorForeground};
		final TypedArray style = theme.obtainStyledAttributes(resId, attrs);

		//
		try
		{
			final Drawable drawable = style.getDrawable(0);
			//Log.d(TAG, theme + " attr=" + Integer.toHexString(attrs[0]) + " value=" + drawable);
			for (int i = 1; i < attrs.length; i++)
			{
				final int intColor = style.getColor(i , 0xCCCCCCCC); // index, defaultVal
				//Log.d(TAG, theme + " attr=" + Integer.toHexString(attrs[i]) + " value=" + Integer.toHexString(intColor));
			}
		}
		finally
		{
			style.recycle();
		}
	}
	*/
}

