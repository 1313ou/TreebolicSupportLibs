/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.colors.preference;

import android.content.Context;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceViewHolder;

public class ColorPickerPreference extends DialogPreference
{
	// C O L O R

	/**
	 * Color
	 */
	@SuppressWarnings("WeakerAccess")
	protected int value;

	/**
	 * S E T T I N G S
	 */
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

	/**
	 * Constructor
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	@SuppressWarnings("WeakerAccess")
	public ColorPickerPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs);
	}

	/**
	 * Constructor
	 *
	 * @param context  context
	 * @param attrs    attributes
	 * @param defStyle def style
	 */
	@SuppressWarnings("WeakerAccess")
	public ColorPickerPreference(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs);
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

	/**
	 * Initialize
	 *
	 * @param attrs attributes
	 */
	private void init(final AttributeSet attrs)
	{
		// preference attributes
		TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
		this.showDialogTitle = array.getBoolean(R.styleable.ColorPickerPreference_showDialogTitle, false);
		this.showPreviewSelectedColorInList = array.getBoolean(R.styleable.ColorPickerPreference_showSelectedColorInList, true);
		array.recycle();

		// view attributes
		array = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
		this.alphaChannelVisible = array.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false);
		this.alphaChannelText = array.getString(R.styleable.ColorPickerView_alphaChannelText);
		this.colorPickerSliderColor = array.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, -1);
		this.colorPickerBorderColor = array.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, -1);
		array.recycle();

	}

	// B I N D

	@Override
	public void onBindViewHolder(final PreferenceViewHolder viewHolder)
	{
		super.onBindViewHolder(viewHolder);

		final ColorPickerView colorPickerView = (ColorPickerView) viewHolder.findViewById(R.id.color_picker_view);
		final ColorPanelView oldColorView = (ColorPanelView) viewHolder.findViewById(R.id.color_panel_old);
		final ColorPanelView newColorView = (ColorPanelView) viewHolder.findViewById(R.id.color_panel_new);
		final LinearLayout landscapeLayout = (LinearLayout) viewHolder.findViewById(R.id.dialog_color_picker_extra_layout_landscape);
		final ColorPanelView preview = (ColorPanelView) viewHolder.findViewById(R.id.preference_preview_color_panel);

		// padding
		boolean isLandscapeLayout = false;
		if (landscapeLayout != null)
		{
			isLandscapeLayout = true;
		}
		if (!isLandscapeLayout)
		{
			((LinearLayout) oldColorView.getParent()).setPadding(Math.round(colorPickerView.getDrawingOffset()), 0, Math.round(colorPickerView.getDrawingOffset()), 0);
		}
		else
		{
			landscapeLayout.setPadding(0, 0, Math.round(colorPickerView.getDrawingOffset()), 0);
		}

		// alpha
		colorPickerView.setAlphaSliderVisible(this.alphaChannelVisible);
		colorPickerView.setAlphaSliderText(this.alphaChannelText);

		// colors
		colorPickerView.setSliderTrackerColor(this.colorPickerSliderColor);
		if (this.colorPickerSliderColor != -1)
		{
			colorPickerView.setSliderTrackerColor(this.colorPickerSliderColor);
		}
		if (this.colorPickerBorderColor != -1)
		{
			colorPickerView.setBorderColor(this.colorPickerBorderColor);
		}

		// old value
		oldColorView.setColor(this.value);

		// new value
		int newColor = this.value;
		if (newColor == 0) // unset value (=transparent black)
		{
			newColor = Color.GRAY;
		}
		if (!this.alphaChannelVisible)
		{
			newColor |= 0xFF000000;
		}
		colorPickerView.setColor(newColor, true);

		// preview
		if (preview != null)
		{
			preview.setColor(this.value);
		}

	}

	// V A L U E S

	static private final int DEFAULTCOLOR = 0xFF000000;

	@SuppressWarnings("boxing")
	@Override
	protected Object onGetDefaultValue(@NonNull final TypedArray array, final int index)
	{
		return array.getInteger(index, DEFAULTCOLOR);
	}

	@Override
	protected void onSetInitialValue(final Object defaultValue)
	{
		this.value = (Integer) defaultValue;
		persistInt(this.value);
	}

	protected void setValue(int value)
	{
		this.value = value;
		persistInt(this.value);
		notifyChanged();
	}

	static public class ColorPickerDialog extends PreferenceDialogFragmentCompat implements ColorPickerView.OnColorChangedListener
	{
		static public ColorPickerPreference.ColorPickerDialog newInstance(final ColorPickerPreference pref)
		{
			final ColorPickerPreference.ColorPickerDialog fragment = new ColorPickerDialog();
			final Bundle args = new Bundle();
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		protected View onCreateDialogView(final Context context)
		{
			final View view = super.onCreateDialogView(context);

			// listener
			final ColorPickerView colorPickerView = view.findViewById(R.id.color_picker_view);
			colorPickerView.setOnColorChangedListener(this);

			return view;
		}

		@Override
		public void onDialogClosed(final boolean positiveResult)
		{
			// when the user selects "OK", persist the new value
			if (positiveResult)
			{
				final ColorPickerPreference pref = (ColorPickerPreference) getPreference();
				final ColorPickerView colorPickerView = getView().findViewById(R.id.color_picker_view);
				int color = colorPickerView.getColor();
				if (pref.callChangeListener(color))
				{
					pref.setValue(color);
				}
			}
		}

		@Override
		public void onColorChanged(final int newColor)
		{
			final ColorPanelView newColorView = getView().findViewById(R.id.color_panel_new);
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
	static public boolean onDisplayPreferenceDialog(final PreferenceFragmentCompat prefFragment, final Preference preference)
	{
		final FragmentManager manager = prefFragment.getFragmentManager();
		if (manager == null)
		{
			return false;
		}
		if (manager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null)
		{
			return true;
		}

		if (preference instanceof ColorPickerPreference)
		{
			final DialogFragment dialogFragment = ColorPickerDialog.newInstance((ColorPickerPreference) preference);
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
		state.value = this.value;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(@Nullable final Parcelable state)
	{
		// Check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(ColorSavedState.class))
		{
			// Didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// Cast state to custom BaseSavedState and pass to superclass
		final ColorSavedState savedState = (ColorSavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		// set this preference's widget to reflect the restored state
		setValue(savedState.value);
	}
}
