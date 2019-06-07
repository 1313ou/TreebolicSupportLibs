package org.treebolic;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
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

	/**
	 * Tint menu items
	 *
	 * @param iconTint    tint
	 * @param menu        menu
	 * @param menuItemIds menu item ids
	 */
	static public void tint(final int iconTint, @NonNull final Menu menu, @NonNull final int... menuItemIds)
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
	@SuppressWarnings({"WeakerAccess"})
	static public void tint(int iconTint, @NonNull final Drawable drawable)
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
	 * @param context context
	 * @param resId   drawable id
	 * @return drawable
	 */
	@SuppressWarnings({"WeakerAccess"})
	static public Drawable getDrawable(@NonNull final Context context, int resId)
	{
		final Resources resources = context.getResources();
		Drawable drawable;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
		{
			final Resources.Theme theme = context.getTheme();
			drawable = resources.getDrawable(resId, theme);
		}
		else
		{
			drawable = resources.getDrawable(resId);
		}
		return drawable;
	}

	/*
	 * Get drawables
	 *
	 * @param context context
	 * @param resIds  drawable ids
	 * @return drawables
	 */
	/*
	@SuppressWarnings({"WeakerAccess", "deprecation"})
	static public Drawable[] getDrawables(final Context context, int... resIds)
	{
		final Resources resources = context.getResources();
		Drawable[] drawables = new Drawable[resIds.length];
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
		{
			final Resources.Theme theme = context.getTheme();
			for (int i = 0; i < resIds.length; i++)
			{
				drawables[i] = resources.getDrawable(resIds[i], theme);
			}
		}
		else
		{
			for (int i = 0; i < resIds.length; i++)
			{
				drawables[i] = resources.getDrawable(resIds[i]);
			}
		}
		return drawables;
	}
	*/

	/**
	 * Get tinted drawable
	 *
	 * @param context  context
	 * @param resId    drawable id
	 * @param iconTint tint
	 * @return tinted drawable
	 */
	static public Drawable getTintedDrawable(@NonNull final Context context, final int resId, final int iconTint)
	{
		Drawable drawable = getDrawable(context, resId);
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
	 * @param context context
	 * @param styleId style id (R.style.MyTheme)
	 * @param attr    attr id (R.attr.editTextColor)
	 * @return color
	 */
	static public int fetchColorFromStyle(@NonNull final Context context, int styleId, int attr)
	{
		final TypedArray array = context.getTheme().obtainStyledAttributes(styleId, new int[]{attr});
		int intColor = array.getColor(0 /* index */, 0 /* defaultVal */);
		// Log.d(TAG, "style resId=" + Integer.toHexString(styleId) + "color=" + Integer.toHexString(intColor));
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
	static public int getColorFromTheme(@NonNull final Context context, int styleId, @SuppressWarnings("SameParameterValue") int colorAttrId)
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

		// get color
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

