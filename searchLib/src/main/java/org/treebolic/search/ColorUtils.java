/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.search;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * Tint
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class ColorUtils
{
	// static private final String TAG = "ColorUtils";

	/**
	 * Tint menu items
	 *
	 * @param iconTint    tint
	 * @param menu        menu
	 * @param menuItemIds menu item ids
	 */
	static public void tint(@ColorInt final int iconTint, @NonNull final Menu menu, @NonNull final int... menuItemIds)
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

	/**
	 * Tint drawable
	 *
	 * @param iconTint tint
	 * @param drawable drawable
	 */
	static public void tint(@ColorInt int iconTint, @NonNull final Drawable drawable)
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

	/**
	 * Get color from theme
	 *
	 * @param context     context
	 * @param style       style id (ex: R.style.MyTheme)
	 * @param colorAttrId attr id (ex: R.attr.editTextColor)
	 * @return color
	 */
	@SuppressWarnings("WeakerAccess")
	static public int getColorFromTheme(@NonNull final Context context, @AttrRes int style, @AttrRes @SuppressWarnings("SameParameterValue") int colorAttrId)
	{
		final Resources.Theme theme = context.getTheme();
		// theme.dump(Log.DEBUG, TAG, "theme");

		// res id of style pointed to from actionBarStyle
		final TypedValue typedValue = new TypedValue();
		theme.resolveAttribute(style, typedValue, true);
		final int resId = typedValue.resourceId;
		// Log.d(TAG, "actionBarStyle=" + Integer.toHexString(resId));

		// now get action bar style values
		final int[] attrs = new int[]{colorAttrId};

		// get color
		// try (final TypedArray array = theme.obtainStyledAttributes(resId, attrs))
		TypedArray array = null;
		try
		{
			array = theme.obtainStyledAttributes(resId, attrs);
			// Log.d(TAG, theme + " attr=" + Integer.toHexString(attrs[0]) + " value=" + Integer.toHexString(intColor));
			return array.getColor(0 /* index */, 0xCCCCCCCC /* defaultVal */);
		}
		finally
		{
			if (array != null)
			{
				array.recycle();
			}
		}
	}

	/**
	 * Get actionbar fore color from theme
	 *
	 * @param context context
	 * @return color
	 */
	static public int getActionBarForegroundColorFromTheme(@NonNull final Context context)
	{
		// Log.d(TAG, "getActionBarForegroundColorFromTheme=0x" + Integer.toHexString(color));
		return getColorFromTheme(context, android.R.attr.actionBarTheme, android.R.attr.textColorPrimary);
	}
}

