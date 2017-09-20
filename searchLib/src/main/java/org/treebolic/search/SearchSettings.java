package org.treebolic.search;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import org.treebolic.wheel.AbstractWheel;
import org.treebolic.wheel.OnWheelChangedListener;
import org.treebolic.wheel.OnWheelScrollListener;
import org.treebolic.wheel.WheelView;
import org.treebolic.wheel.adapters.AbstractWheelTextAdapter;
import org.treebolic.wheel.adapters.WheelViewAdapter;

/**
 * Search settings
 *
 * @author Bernard Bou
 */
public class SearchSettings extends AppCompatDialogFragment
{
	protected static final String TAG = "SearchSettings";

	static public final String PREF_SEARCH_SCOPE = "pref_search_scope";

	static public final String PREF_SEARCH_MODE = "pref_search_mode";

	static public final String SCOPE_SOURCE = "SOURCE";

	static public final String SCOPE_LABEL = "LABEL";

	static public final String SCOPE_CONTENT = "CONTENT";

	static public final String SCOPE_LINK = "LINK";

	static public final String SCOPE_ID = "ID";

	static public final String MODE_STARTSWITH = "STARTSWITH";

	static public final String MODE_EQUALS = "EQUALS";

	static public final String MODE_INCLUDES = "INCLUDES";

	static public final String MODE_IS = "IS";

	private boolean scrolling = false;

	private WheelView scopeWheel;

	private WheelView modeWheel;

	private Adapter modeAdapter;

	private Adapter sourceAdapter;

	private String[] modes;

	private String[] scopes;

	private String[] sources;

	private int sourceModeIndex;

	public static SearchSettings newInstance()
	{
		return new SearchSettings();
	}

	@NonNull
	@SuppressLint({"InflateParams", "ApplySharedPref"})
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState)
	{
		final Context context = getActivity();
		final Resources resources = context.getResources();
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

		// get strings
		final String[] scopeLabels = resources.getStringArray(R.array.search_scope_labels);
		final String[] modeLabels = resources.getStringArray(R.array.search_mode_labels);
		final String[] sourceLabels = resources.getStringArray(R.array.search_source_labels);
		this.modes = resources.getStringArray(R.array.search_modes);
		this.scopes = resources.getStringArray(R.array.search_scopes);
		this.sources = resources.getStringArray(R.array.search_sources);
		this.sourceModeIndex = resources.getInteger(R.integer.search_scope_source_index);
		final int defaultScopeIndex = resources.getInteger(R.integer.search_scope_default);
		final int defaultModeIndex = resources.getInteger(R.integer.search_mode_default);

		// get icons
		final int[] scopeIcons = new int[]{R.drawable.ic_search_scope_label, R.drawable.ic_search_scope_id, R.drawable.ic_search_scope_content, R.drawable.ic_search_scope_link, R.drawable.ic_search_scope_source};
		final int[] modeIcons = new int[]{R.drawable.ic_search_mode_equals, R.drawable.ic_search_mode_startswith, R.drawable.ic_search_mode_includes};
		final int[] sourceIcons = new int[]{R.drawable.ic_search_mode_equals};

		// wheel2 adapter
		this.modeAdapter = new Adapter(context, R.layout.item_mode, modeLabels, modeIcons, this.modes.length, Adapter.Type.MODE);
		this.sourceAdapter = new Adapter(context, R.layout.item_mode, sourceLabels, sourceIcons, this.sources.length, Adapter.Type.SOURCE);

		// initial values
		int scopeIndex = defaultScopeIndex;
		final String scope = sharedPref.getString(PREF_SEARCH_SCOPE, null);
		Log.d(TAG, "Scope " + scope);
		if (scope != null)
		{
			for (int i = 0; i < this.scopes.length; i++)
			{
				if (scope.equals(this.scopes[i]))
				{
					scopeIndex = i;
					break;
				}
			}
		}
		else
		{
			sharedPref.edit().putString(PREF_SEARCH_SCOPE, this.scopes[defaultScopeIndex]).commit();
		}
		int modeIndex = defaultModeIndex;
		if (scopeIndex < this.scopes.length - 1)
		{
			modeIndex = 1;
			final String mode = sharedPref.getString(PREF_SEARCH_MODE, null);
			Log.d(TAG, "Mode " + mode);
			if (mode != null)
			{
				for (int i = 0; i < this.modes.length; i++)
				{
					if (mode.equals(this.modes[i]))
					{
						modeIndex = i;
						break;
					}
				}
			}
		}
		else
		{
			sharedPref.edit().putString(PREF_SEARCH_MODE, this.modes[defaultModeIndex]).commit();
		}

		// dialog
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// dialog.setTitle(R.string.search_title);
		dialog.setContentView(R.layout.dialog_search_settings);

		// wheel 1
		this.scopeWheel = (WheelView) dialog.findViewById(R.id.scope);
		assert this.scopeWheel != null;
		this.scopeWheel.setVisibleItems(4);
		this.scopeWheel.setViewAdapter(new Adapter(context, R.layout.item_scope, scopeLabels, scopeIcons, this.scopes.length, Adapter.Type.SCOPE));

		// wheel 1 events
		this.scopeWheel.addChangingListener(new OnWheelChangedListener()
		{
			@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
			@SuppressWarnings("synthetic-access")
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue)
			{
				Log.d(TAG, "Wheel 1 " + newValue + ' ' + SearchSettings.this.scopes[newValue]);
				sharedPref.edit().putString(PREF_SEARCH_SCOPE, SearchSettings.this.scopes[newValue]).commit();
				if (!SearchSettings.this.scrolling)
				{
					updateWheel2(newValue);
				}
			}
		});
		this.scopeWheel.addScrollingListener(new OnWheelScrollListener()
		{
			@Override
			@SuppressWarnings("synthetic-access")
			public void onScrollingStarted(AbstractWheel wheel)
			{
				SearchSettings.this.scrolling = true;
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public void onScrollingFinished(AbstractWheel wheel)
			{
				SearchSettings.this.scrolling = false;
				updateWheel2(SearchSettings.this.scopeWheel.getCurrentItem());
			}
		});

		// wheel 2
		this.modeWheel = (WheelView) dialog.findViewById(R.id.mode);
		assert this.modeWheel != null;
		this.modeWheel.setVisibleItems(4);
		this.modeWheel.setViewAdapter(this.modeAdapter); //new Adapter(context, R.layout.item_mode, modeLabels, modeIcons, this.modes.length, Adapter.Type.MODE));

		// wheel 2 events
		this.modeWheel.addChangingListener(new OnWheelChangedListener()
		{
			@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
			@SuppressWarnings("synthetic-access")
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue)
			{
				WheelViewAdapter wheelViewAdapter = wheel.getViewAdapter();
				Adapter adapter = (Adapter) wheelViewAdapter;
				if (adapter.getType() == Adapter.Type.MODE)
				{
					Log.d(TAG, "Wheel 2 " + newValue + ' ' + SearchSettings.this.modes[newValue]);
					sharedPref.edit().putString(PREF_SEARCH_MODE, SearchSettings.this.modes[newValue]).commit();
				}
				else if (adapter.getType() == Adapter.Type.SOURCE)
				{
					Log.d(TAG, "Wheel 2 " + newValue + ' ' + SearchSettings.this.sources[newValue]);
					sharedPref.edit().putString(PREF_SEARCH_MODE, SearchSettings.this.sources[newValue]).commit();
				}
			}
		});

		// wheels initial
		this.scopeWheel.setCurrentItem(scopeIndex);
		this.modeWheel.setCurrentItem(modeIndex);

		// dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_semitransparent_rounded);

		// final ImageButton button = (ImageButton) dialog.findViewById(R.id.tip_dismiss);
		// button.setOnClickListener(new View.OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// dialog.cancel();
		// }
		// });
		return dialog;
	}

	/**
	 * Updates item_mode wheel depending on item_scope
	 */
	private int oldModeIndex = 1;

	private void updateWheel2(int scopeIndex)
	{
		int modeIndex;
		Adapter adapter;
		if (scopeIndex < this.sourceModeIndex) // item_scope != source
		{
			modeIndex = this.oldModeIndex;
			adapter = this.modeAdapter;
		}
		else // item_scope == source
		{
			modeIndex = 0;
			adapter = this.sourceAdapter;
			this.oldModeIndex = this.modeWheel.getCurrentItem();
		}
		this.modeWheel.setViewAdapter(adapter);
		this.modeWheel.setCurrentItem(modeIndex);
	}


	/**
	 * Adapter for scopes
	 */
	static private class Adapter extends AbstractWheelTextAdapter
	{
		public enum Type
		{
			SCOPE, MODE, SOURCE
		}

		final String[] labels;

		final int[] icons;

		final int len;

		final Type type;

		/**
		 * Constructor
		 */
		protected Adapter(final Context context0, final int layout0, final String[] texts0, final int[] icons0, final int len0, final Type type0)
		{
			super(context0, layout0, NO_RESOURCE);
			this.labels = texts0;
			this.icons = icons0;
			this.len = len0;
			this.type = type0;
			setItemTextResource(R.id.wheel_name);
		}

		@Override
		public View getItem(final int index, final View cachedView, final ViewGroup parent)
		{
			View view = super.getItem(index, cachedView, parent);
			ImageView img = (ImageView) view.findViewById(R.id.wheel_icon);
			img.setImageResource(this.icons[index]);
			return view;
		}

		@Override
		public int getItemsCount()
		{
			return this.len;
		}

		@Override
		protected CharSequence getItemText(int index)
		{
			return this.labels[index];
		}

		public Type getType()
		{
			return this.type;
		}
	}

	/**
	 * Show
	 */
	static public void show(final FragmentManager fragmentManager)
	{
		final AppCompatDialogFragment newFragment = SearchSettings.newInstance();
		newFragment.show(fragmentManager, "dialog");
	}
}
