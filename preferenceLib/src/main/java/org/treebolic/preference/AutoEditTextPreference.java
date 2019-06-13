/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceViewHolder;

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
	 * Auto-complete text view
	 */
	private AutoCompleteTextView editView;

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
	 * @param values0 values
	 */
	public void setValues(CharSequence[] values0)
	{
		this.values = values0;
	}

	@Override
	public void onBindViewHolder(@NonNull final PreferenceViewHolder viewHolder)
	{
		super.onBindViewHolder(viewHolder);

		// get editView
		this.editView = (AutoCompleteTextView) viewHolder.findViewById(R.id.autoedittext);
		if (this.editView != null)
		{
			// fill with value and possible values
			final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, this.values);
			this.editView.setAdapter(adapter);
			if (this.value != null)
			{
				this.editView.setText(this.value);
				this.editView.setSelection(this.value.length());
			}
		}
	}

	@Override
	protected void onSetInitialValue(final Object defaultValue0)
	{
		// set default state from the XML attribute
		this.value = (String) defaultValue0;
		persistString(this.value);
	}

	@Nullable
	@Override
	protected Object onGetDefaultValue(@NonNull final TypedArray array, final int index)
	{
		return array.getString(index);
	}

	// D I A L O G

	static public AutoEditTextPreference.AutoEditTextDialog newInstance(final AutoEditTextPreference pref)
	{
		final AutoEditTextPreference.AutoEditTextDialog fragment = pref.new AutoEditTextDialog();
		final Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	private class AutoEditTextDialog extends PreferenceDialogFragmentCompat
	{
		@Override
		public void onDialogClosed(final boolean positiveResult)
		{
			// when the user selects "OK", persist the new value
			if (positiveResult)
			{
				final Editable editable = AutoEditTextPreference.this.editView.getText();
				final String value0 = editable == null ? null : editable.toString();
				if (callChangeListener(value0))
				{
					// set value
					AutoEditTextPreference.this.value = value0;
					persistString(value0);
					notifyChanged();
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
			final DialogFragment dialogFragment = AutoEditTextPreference.newInstance((AutoEditTextPreference) preference);
			dialogFragment.setTargetFragment(prefFragment, 0);
			dialogFragment.show(manager, DIALOG_FRAGMENT_TAG);
			return true;
		}
		return false;
	}

	// S T A T E

	/**
	 * Saved state
	 */
	private static class SavedState extends BaseSavedState
	{
		// member that holds the setting's value
		@Nullable
		private String value;

		/**
		 * Constructor from superstate
		 *
		 * @param superState superstate
		 */
		@SuppressWarnings("WeakerAccess")
		public SavedState(final Parcelable superState)
		{
			super(superState);
		}

		/**
		 * Constructor from parcel
		 *
		 * @param source source parcel
		 */
		@SuppressWarnings("WeakerAccess")
		public SavedState(@NonNull final Parcel source)
		{
			super(source);

			// get the current preference's value
			this.value = source.readString();
		}

		@Override
		public void writeToParcel(@NonNull final Parcel dest, final int flags)
		{
			super.writeToParcel(dest, flags);

			// write the preference's value
			dest.writeString(this.value);
		}

		/**
		 * Standard creator object using an instance of this class
		 */
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
		{
			@Override
			public SavedState createFromParcel(@NonNull final Parcel in)
			{
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size)
			{
				return new SavedState[size];
			}
		};
	}

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
		final SavedState state = new SavedState(superState);

		// set the state's value with the class member that holds current setting value
		state.value = this.value;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(@Nullable final Parcelable state0)
	{
		// check whether we saved the state in onSaveInstanceState
		if (state0 == null || !state0.getClass().equals(SavedState.class))
		{
			// didn't save the state, so call superclass
			super.onRestoreInstanceState(state0);
			return;
		}

		// cast state to custom BaseSavedState and pass to superclass
		final SavedState state = (SavedState) state0;
		super.onRestoreInstanceState(state.getSuperState());

		// set this Preference's widget to reflect the restored state
		this.editView.setText(state.value);
	}

	class xxx extends EditTextPreference
	{
		public xxx(final Context context)
		{
			super(context);
		}
	}
}
