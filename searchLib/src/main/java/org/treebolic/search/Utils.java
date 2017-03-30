package org.treebolic.search;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Search settings
 *
 * @author Bernard Bou
 */
public class Utils
{
	static private int fetchColor(final Context context, int attr)
	{
		final TypedValue typedValue = new TypedValue();
		final Resources.Theme theme = context.getTheme();
		theme.resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}

	static public void tint(final Context context, final Menu menu, final int... menuItemIds)
	{
		final int iconTint = fetchColor(context, R.attr.treebolic_actionbar_icon_color);
		for (int menuItemId : menuItemIds)
		{
			final MenuItem menuItem = menu.findItem(menuItemId);
			final Drawable drawable = menuItem.getIcon();
			DrawableCompat.setTint(drawable, iconTint);
		}
	}
}
