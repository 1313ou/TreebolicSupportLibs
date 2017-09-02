package org.treebolic.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

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
	 * Default value
	 */
	private String defaultValue;

	/**
	 * Value
	 */
	private String value;

	/**
	 * Auto-complete text view
	 */
	private AutoCompleteTextView editView;

	/**
	 * Constructor
	 *
	 * @param context
	 *            context
	 * @param attrs
	 *            attributes
	 */
	public AutoEditTextPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);

		setDialogLayoutResource(R.layout.dialog_autoedittext_pref);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setDialogIcon(null);
	}

	/**
	 * Initialize
	 *
	 * @param attrs
	 *            attributes
	 */
	private void init(final Context context, final AttributeSet attrs)
	{
		// obtain default value
		final int id = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "defaultValue", -1);
		this.defaultValue = id == -1 ? null : context.getResources().getString(id);

		// obtain values through styled attributes
		final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.AutoEditTextPreference);
		this.values = array.getTextArray(R.styleable.AutoEditTextPreference_values);
		array.recycle();

		// ensure not null
		if (this.values == null)
			this.values = new CharSequence[0];
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
	 * @param values0
	 *            values
	 */
	public void setValues(CharSequence[] values0)
	{
		this.values = values0;
	}

	@Override
	protected void onBindDialogView(final View view)
	{
		super.onBindDialogView(view);

		// get editView
		this.editView = (AutoCompleteTextView) view.findViewById(R.id.autoedittext);
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
	protected void onDialogClosed(final boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);

		// when the user selects "OK", persist the new value
		if (positiveResult)
		{
			final Editable editable = this.editView.getText();
			final String value0 = editable == null ? null : editable.toString();
			if (callChangeListener(value0))
			{
				// set value
				this.value = value0;
				persistString(value0);
				notifyChanged();
			}
		}
	}

	@Override
	protected void onSetInitialValue(final boolean restorePersistedValue, final Object defaultValue0)
	{
		if (restorePersistedValue)
		{
			this.value = getPersistedString(this.defaultValue);
		}
		else
		{
			// set default state from the XML attribute
			this.value = (String) defaultValue0;
			persistString(this.value);
		}
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray array, final int index)
	{
		return array.getString(index);
	}

	// S T A T E

	/**
	 * Saved state
	 */
	private static class SavedState extends BaseSavedState
	{
		// member that holds the setting's value
		private String value;

		/**
		 * Constructor from superstate
		 *
		 * @param superState
		 *            superstate
		 */
		public SavedState(final Parcelable superState)
		{
			super(superState);
		}

		/**
		 * Constructor from parcel
		 *
		 * @param source
		 *            source parcel
		 */
		public SavedState(final Parcel source)
		{
			super(source);

			// get the current preference's value
			this.value = source.readString();
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags)
		{
			super.writeToParcel(dest, flags);

			// write the preference's value
			dest.writeString(this.value);
		}

		/**
		 * Standard creator object using an instance of this class
		 */
		@SuppressWarnings("hiding")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
		{
			@Override
			public SavedState createFromParcel(final Parcel in)
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

	@SuppressWarnings("synthetic-access")
	@Override
	protected Parcelable onSaveInstanceState()
	{
		final Parcelable superState = super.onSaveInstanceState();

		// check whether this Preference is persistent (continually saved)
		if (isPersistent())
			// no need to save instance state since it's persistent, use superclass state
			return superState;

		// create instance of custom BaseSavedState
		final SavedState state = new SavedState(superState);

		// set the state's value with the class member that holds current setting value
		state.value = this.value;
		return state;
	}

	@SuppressWarnings("synthetic-access")
	@Override
	protected void onRestoreInstanceState(final Parcelable state0)
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
}
