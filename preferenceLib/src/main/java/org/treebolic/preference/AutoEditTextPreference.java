/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;

/**
 * AutoEditTextPreference
 *
 * @author Bernard Bou
 */
public class AutoEditTextPreference extends DialogPreference
{
	/**
	 * Possible values
	 */
	private CharSequence[] values;

	/**
	 * Value
	 */
	@Nullable
	private String value;

	/**
	 * Constructor
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	public AutoEditTextPreference(@NonNull final Context context, @NonNull final AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs);

		setDialogLayoutResource(R.layout.dialog_autoedittext_pref);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setDialogIcon(null);
	}

	/**
	 * Initialize
	 *
	 * @param attrs attributes
	 */
	private void init(@NonNull final AttributeSet attrs)
	{
		// obtain values through styled attributes
		final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.AutoEditTextPreference);
		this.values = array.getTextArray(R.styleable.AutoEditTextPreference_values);
		array.recycle();

		// ensure not null
		if (this.values == null)
		{
			this.values = new CharSequence[0];
		}
	}

	/**
	 * Values Getter
	 *
	 * @return values
	 */
	public CharSequence[] getValues()
	{
		return this.values;
	}

	/**
	 * Values Setter
	 *
	 * @param values values
	 */
	public void setValues(CharSequence[] values)
	{
		this.values = values;
	}

	// V A L U E

	@Nullable
	@Override
	protected Object onGetDefaultValue(@NonNull final TypedArray array, final int index)
	{
		return array.getString(index);
	}

	@Override
	protected void onSetInitialValue(final Object initialValue)
	{
		// set default state from the XML attribute
		this.value = (String) initialValue;
		persistString(this.value);
	}

	protected void setValue(final String newValue)
	{
		this.value = newValue;
		persistString(newValue);
		notifyChanged();
	}

	// D I A L O G  F R A G M E N T

	static public class AutoEditTextDialog extends PreferenceDialogFragmentCompat
	{
		static public AutoEditTextPreference.AutoEditTextDialog newInstance(final AutoEditTextPreference pref)
		{
			final AutoEditTextPreference.AutoEditTextDialog fragment = new AutoEditTextDialog();
			final Bundle args = new Bundle();
			args.putString(ARG_KEY, pref.getKey());
			fragment.setArguments(args);
			return fragment;
		}

		private AutoCompleteTextView editView;

		@Override
		protected void onBindDialogView(View view)
		{
			super.onBindDialogView(view);

			// pref
			final AutoEditTextPreference pref = (AutoEditTextPreference) getPreference();

			// edit text
			editView = view.findViewById(R.id.autoedit_text);
			assert editView != null;
			editView.requestFocus();

			// populate with value
			if (pref.value != null)
			{
				editView.setText(pref.value);
				// place cursor at the end
				editView.setSelection(pref.value.length());
			}

			// populate with possible values
			final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, pref.values);
			editView.setAdapter(adapter);
		}

		@Override
		public void onDialogClosed(final boolean positiveResult)
		{
			// when the user selects "OK", persist the new value
			if (positiveResult)
			{
				final Editable editable = editView.getText();
				final AutoEditTextPreference pref = (AutoEditTextPreference) getPreference();
				final String newValue = editable == null ? null : editable.toString();
				if (pref.callChangeListener(newValue))
				{
					pref.setValue(newValue);
				}
			}
		}
	}

	private static final String DIALOG_FRAGMENT_TAG = "AutoEditTextPreference";

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

		if (preference instanceof AutoEditTextPreference)
		{
			final DialogFragment dialogFragment = AutoEditTextDialog.newInstance((AutoEditTextPreference) preference);
			dialogFragment.setTargetFragment(prefFragment, 0);
			dialogFragment.show(manager, DIALOG_FRAGMENT_TAG);
			return true;
		}
		return false;
	}

	// S T A T E

	@Override
	protected Parcelable onSaveInstanceState()
	{
		final Parcelable superState = super.onSaveInstanceState();

		// check whether this Preference is persistent (continually saved)
		if (isPersistent())
		// no need to save instance state since it's persistent, use superclass state
		{
			return superState;
		}

		// create instance of custom BaseSavedState
		final StringSavedState state = new StringSavedState(superState);

		// set the state's value with the class member that holds current setting value
		state.value = this.value;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(@Nullable final Parcelable state)
	{
		// check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(StringSavedState.class))
		{
			// didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// cast state to custom BaseSavedState and pass to superclass
		final StringSavedState savedState = (StringSavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		// set this preference's widget to reflect the restored state
		setValue(savedState.value);
	}
}
