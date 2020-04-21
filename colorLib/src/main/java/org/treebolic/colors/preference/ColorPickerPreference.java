/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.colors.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import org.treebolic.colors.R;
import org.treebolic.colors.view.ColorPanelView;
import org.treebolic.colors.view.ColorPickerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceViewHolder;

public class ColorPickerPreference extends DialogPreference
{
	// V A L U E

	private static final int NULL_VALUE = 0;

	/**
	 * Color
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected Integer value;

	// S E T T I N G S

	@SuppressWarnings("WeakerAccess")
	protected boolean alphaChannelVisible = false;

	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected String alphaChannelText = null;

	@SuppressWarnings("WeakerAccess")
	protected boolean showDialogTitle = false;

	@SuppressWarnings("WeakerAccess")
	protected boolean showPreviewSelectedColorInList = true;

	@SuppressWarnings("WeakerAccess")
	protected int colorPickerSliderColor = -1;

	@SuppressWarnings("WeakerAccess")
	protected int colorPickerBorderColor = -1;

	// C O N S T R U C T O R

	/**
	 * Constructor
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	@SuppressWarnings("WeakerAccess")
	public ColorPickerPreference(@NonNull final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		this.value = null;

		// attributes
		init(context, attrs);

		// set up
		setup();
	}

	/**
	 * Constructor
	 *
	 * @param context  context
	 * @param attrs    attributes
	 * @param defStyle def style
	 */
	@SuppressWarnings("WeakerAccess")
	public ColorPickerPreference(@NonNull final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);

		// attributes
		init(context, attrs);

		// set up
		setup();
	}

	/**
	 * Initialize
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	private void init(@NonNull final Context context, final AttributeSet attrs)
	{
		// preference attributes
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
		this.showDialogTitle = array.getBoolean(R.styleable.ColorPickerPreference_showDialogTitle, false);
		this.showPreviewSelectedColorInList = array.getBoolean(R.styleable.ColorPickerPreference_showSelectedColorInList, true);
		array.recycle();

		// view attributes
		array = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
		this.alphaChannelVisible = array.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false);
		this.alphaChannelText = array.getString(R.styleable.ColorPickerView_alphaChannelText);
		this.colorPickerSliderColor = array.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, -1);
		this.colorPickerBorderColor = array.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, -1);
		array.recycle();
	}

	/**
	 * Set up
	 */
	private void setup()
	{
		// resource
		setDialogLayoutResource(R.layout.dialog_color_picker);

		// buttons
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		// persistence
		setPersistent(true);

		// list
		if (this.showPreviewSelectedColorInList)
		{
			setWidgetLayoutResource(R.layout.preference_preview_layout);
		}

		// title
		if (!this.showDialogTitle)
		{
			setDialogTitle(null);
		}
	}

	// V A L U E S

	static private final int START_COLOR = Color.GRAY;

	// V A L U E

	@SuppressWarnings("UnusedReturnValue")
	private boolean persistValue(@Nullable Integer value)
	{
		//Log.d(TAG, "Persist " + this.value);
		if (value == null)
		{
			if (!shouldPersist())
			{
				return false;
			}

			final SharedPreferences.Editor editor = getSharedPreferences().edit();
			editor.remove(getKey());
			tryCommit(editor);
			return true;
		}
		else
		{
			return persistInt(value);
		}
	}

	@Nullable
	private Integer getPersistedValue(@Nullable final Object defaultValue)
	{
		int value = getPersistedInt(NULL_VALUE);
		if (value != NULL_VALUE)
		{
			return value;
		}
		if (defaultValue != null)
		{
			return (Integer) defaultValue;
		}
		return null;
	}

	/**
	 * Try to commit
	 *
	 * @param editor editor editor
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static private void tryCommit(@NonNull final SharedPreferences.Editor editor)
	{
		try
		{
			editor.apply();
		}
		catch (@NonNull final AbstractMethodError ignored)
		{
			// The app injected its own pre-Gingerbread SharedPreferences.Editor implementation without an apply method.
			editor.commit();
		}
	}

	@Nullable
	@Override
	protected Object onGetDefaultValue(@NonNull final TypedArray array, final int index)
	{
		return array.getInteger(index, NULL_VALUE);
	}

	@Override
	protected void onSetInitialValue(@Nullable final Object defaultValue)
	{
		setValue(getPersistedValue(defaultValue));
	}

	private void setValue(@Nullable Integer value)
	{
		this.value = value;
		persistValue(this.value);
		notifyChanged();
	}

	// B I N D

	@Override
	public void onBindViewHolder(@NonNull final PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);
		final ColorPanelView preview = (ColorPanelView) holder.findViewById(R.id.preference_preview_color_panel);
		if (preview != null)
		{
			preview.setValue(this.value);
		}
	}

	// D I A L O G   F R A G M E N T

	static public class ColorPickerPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat implements ColorPickerView.OnColorChangedListener
	{
		@NonNull
		@SuppressWarnings("WeakerAccess")
		static public ColorPickerPreferenceDialogFragmentCompat newInstance(@NonNull final ColorPickerPreference pref)
		{
			final ColorPickerPreferenceDialogFragmentCompat fragment = new ColorPickerPreferenceDialogFragmentCompat();
			final Bundle args = new Bundle();
			args.putString(ARG_KEY, pref.getKey());
			fragment.setArguments(args);
			return fragment;
		}

		private ColorPickerView colorPickerView;

		private ColorPanelView newColorView;

		@Override
		protected void onBindDialogView(@NonNull View view)
		{
			super.onBindDialogView(view);

			// pref
			final ColorPickerPreference pref = (ColorPickerPreference) getPreference();

			colorPickerView = view.findViewById(R.id.color_picker_view);
			colorPickerView.setOnColorChangedListener(this);

			newColorView = view.findViewById(R.id.color_panel_new);
			ColorPanelView oldColorView = view.findViewById(R.id.color_panel_old);
			ColorPanelView preview = view.findViewById(R.id.preference_preview_color_panel);
			final LinearLayout landscapeLayout = view.findViewById(R.id.dialog_color_picker_extra_layout_landscape);

			// padding
			boolean isLandscapeLayout = landscapeLayout != null;
			if (isLandscapeLayout)
			{
				landscapeLayout.setPadding(0, 0, Math.round(colorPickerView.getDrawingOffset()), 0);
			}
			else
			{
				((LinearLayout) oldColorView.getParent()).setPadding(Math.round(colorPickerView.getDrawingOffset()), 0, Math.round(colorPickerView.getDrawingOffset()), 0);
			}

			// alpha
			colorPickerView.setAlphaSliderVisible(pref.alphaChannelVisible);
			colorPickerView.setAlphaSliderText(pref.alphaChannelText);

			// colors
			colorPickerView.setSliderTrackerColor(pref.colorPickerSliderColor);
			if (pref.colorPickerSliderColor != -1)
			{
				colorPickerView.setSliderTrackerColor(pref.colorPickerSliderColor);
			}
			if (pref.colorPickerBorderColor != -1)
			{
				colorPickerView.setBorderColor(pref.colorPickerBorderColor);
			}

			// old value
			oldColorView.setValue(pref.value);

			// new value
			int newColor = pref.value == null ? START_COLOR : pref.value;
			if (!pref.alphaChannelVisible)
			{
				newColor |= 0xFF000000;
			}
			colorPickerView.setColor(newColor, true);

			// preview
			if (preview != null)
			{
				preview.setValue(pref.value);
			}
		}

		@Override
		protected void onPrepareDialogBuilder(@NonNull final AlertDialog.Builder builder)
		{
			super.onPrepareDialogBuilder(builder);

			// Don't show the positive button; clicking a color will be the "positive" action
			final ColorPickerPreference pref = (ColorPickerPreference) getPreference();

			// Neutral button to clear value
			builder.setNeutralButton(R.string.dialog_title_none, (dialog, which) -> {

				if (pref.callChangeListener(null))
				{
					pref.setValue(null);
				}
				if (getDialog() != null)
				{
					getDialog().dismiss();
				}
			});
		}

		@Override
		public void onDialogClosed(final boolean positiveResult)
		{
			// when the user selects "OK", persist the new value
			if (positiveResult)
			{
				int newColor = colorPickerView.getColor();
				final ColorPickerPreference pref = (ColorPickerPreference) getPreference();
				if (pref.callChangeListener(newColor))
				{
					pref.setValue(newColor);
				}
			}
		}

		@Override
		public void onColorChanged(final int newColor)
		{
			newColorView.setColor(newColor);
		}
	}

	private static final String DIALOG_FRAGMENT_TAG = "ColorPickerPreference";

	/**
	 * onDisplayPreferenceDialog helper
	 *
	 * @param prefFragment preference fragment
	 * @param preference   preference
	 * @return false if not handled: call super.onDisplayPreferenceDialog(preference)
	 */
	static public boolean onDisplayPreferenceDialog(@NonNull final PreferenceFragmentCompat prefFragment, final Preference preference)
	{
		final FragmentManager manager;
		try
		{
			manager = prefFragment.getParentFragmentManager();
		}
		catch (IllegalStateException e)
		{
			return false;
		}
		if (manager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null)
		{
			return true;
		}

		if (preference instanceof ColorPickerPreference)
		{
			final DialogFragment dialogFragment = ColorPickerPreferenceDialogFragmentCompat.newInstance((ColorPickerPreference) preference);
			dialogFragment.setTargetFragment(prefFragment, 0);
			dialogFragment.show(manager, DIALOG_FRAGMENT_TAG);
			return true;
		}
		return false;
	}

	// S A V E / R E S T O R E

	@NonNull
	@Override
	protected Parcelable onSaveInstanceState()
	{
		final Parcelable superState = super.onSaveInstanceState();

		// create instance of custom BaseSavedState
		final ColorSavedState state = new ColorSavedState(superState);

		// set the state's value with the class member that holds current setting value
		state.isNull = this.value == null;
		state.value = this.value == null ? 0 : this.value;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(@Nullable final Parcelable state)
	{
		// check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(ColorSavedState.class))
		{
			// didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// cast state to custom BaseSavedState and pass to superclass
		final ColorSavedState savedState = (ColorSavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		// set this preference's widget to reflect the restored state
		setValue(savedState.isNull ? null : savedState.value);
	}

	/**
	 * Summary provider for color
	 */
	static public final Preference.SummaryProvider<ColorPickerPreference> SUMMARY_PROVIDER = (preference) -> {

		final Context context = preference.getContext();
		final Integer value = preference.getPersistedValue(null);

		// set the summary to the value's hex string representation.
		return value == null ? context.getString(R.string.pref_value_default) : Integer.toHexString(value);
	};
}
