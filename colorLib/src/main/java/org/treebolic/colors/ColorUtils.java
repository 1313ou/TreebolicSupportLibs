/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.colors;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import org.treebolic.common.R;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
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
	 * Get drawable
	 *
	 * @param context     context
	 * @param drawableRes drawable id
	 * @return drawable
	 */
	@Nullable
	@SuppressWarnings({"WeakerAccess"})
	static public Drawable getDrawable(@NonNull final Context context, @DrawableRes int drawableRes)
	{
		return ResourcesCompat.getDrawable(context.getResources(), drawableRes, context.getTheme());
	}

	/**
	 * Get tinted drawable
	 *
	 * @param context     context
	 * @param drawableRes drawable id
	 * @param iconTint    tint
	 * @return tinted drawable
	 */
	@NonNull
	static public Drawable getTintedDrawable(@NonNull final Context context, @DrawableRes final int drawableRes, @ColorInt final int iconTint)
	{
		Drawable drawable = getDrawable(context, drawableRes);
		assert drawable != null;
		ColorUtils.tint(iconTint, drawable);
		return drawable;
	}

	/**
	 * Fetch color from theme
	 *
	 * @param context context
	 * @param attr    color attr
	 * @return color
	 */
	static public int fetchColor(@NonNull final Context context, int attr)
	{
		final Resources.Theme theme = context.getTheme();
		final TypedValue typedValue = new TypedValue();
		theme.resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}

	/**
	 * Get color from style
	 *
	 * @param context  context
	 * @param styleRes style id (R.style.MyTheme)
	 * @param attr     attr id (R.attr.editTextColor)
	 * @return color
	 */
	static public int fetchColorFromStyle(@NonNull final Context context, @StyleRes int styleRes, int attr)
	{
		final TypedArray array = context.getTheme().obtainStyledAttributes(styleRes, new int[]{attr});
		int intColor = array.getColor(0 /* index */, 0 /* defaultVal */);
		// Log.d(TAG, "style resId=" + Integer.toHexString(styleId) + "color=" + Integer.toHexString(intColor));
		array.recycle();
		return intColor;
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
		final TypedArray array = theme.obtainStyledAttributes(resId, attrs);

		// get color
		try
		{
			// Log.d(TAG, theme + " attr=" + Integer.toHexString(attrs[0]) + " value=" + Integer.toHexString(intColor));
			return array.getColor(0 /* index */, 0xCCCCCCCC /* defaultVal */);
		}
		finally
		{
			array.recycle();
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

