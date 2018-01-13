package org.treebolic.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * OpenEditTextPreference
 *
 * @author Bernard Bou
 */
public class OpenEditTextPreference extends DialogPreference
{
	/**
	 * Possible values
	 */
	private CharSequence[] values;

	/**
	 * Possible labels
	 */
	private CharSequence[] labels;

	/**
	 * Possible entry enable
	 */
	private boolean[] enable;

	/**
	 * Default value
	 */
	private String defaultValue;

	/**
	 * Value
	 */
	private String value;

	/**
	 * Edit text
	 */
	private EditText editView;

	/**
	 * Radio group
	 */
	private RadioGroup optionsView;

	/**
	 * Constructor
	 *
	 * @param context context
	 * @param attrs   attributes
	 */
	public OpenEditTextPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);

		setDialogLayoutResource(R.layout.dialog_openedittext_pref);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setDialogIcon(null);
	}

	/**
	 * Initialize
	 *
	 * @param attrs attributes
	 */
	private void init(final Context context, final AttributeSet attrs)
	{
		// obtain default value
		final int id = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "defaultValue", -1);
		this.defaultValue = id == -1 ? null : context.getResources().getString(id);

		// obtain values through styled attributes
		final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.OpenEditTextPreference);
		this.values = array.getTextArray(R.styleable.OpenEditTextPreference_values);
		this.labels = array.getTextArray(R.styleable.OpenEditTextPreference_labels);
		array.recycle();

		// ensure not null
		if (this.values == null)
		{
			this.values = new CharSequence[0];
		}
		if (this.labels == null)
		{
			this.labels = new CharSequence[0];
		}

		// enable all
		this.enable = new boolean[this.values.length];
		for (int i = 0; i < this.enable.length; i++)
		{
			this.enable[i] = true;
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
	 * Labels Setter
	 *
	 * @param values0 values
	 */
	public void setValues(CharSequence[] values0)
	{
		this.values = values0;
	}

	/**
	 * Labels Getter
	 *
	 * @return labels
	 */
	public CharSequence[] getLabels()
	{
		return this.labels;
	}

	/**
	 * Values Setter
	 *
	 * @param labels0 labels
	 */
	public void setLabels(CharSequence[] labels0)
	{
		this.values = labels0;
	}

	/**
	 * Enable Setter
	 *
	 * @param enable0 enable flags
	 */
	public void setEnables(boolean[] enable0)
	{
		this.enable = enable0;
	}

	@Override
	protected void onBindDialogView(final View view)
	{
		super.onBindDialogView(view);

		// edit text
		this.editView = view.findViewById(R.id.edit);
		if (this.editView != null)
		{
			if (this.value != null)
			{
				this.editView.setText(this.value);
				this.editView.setSelection(this.value.length());
			}
		}

		// options
		this.optionsView = view.findViewById(R.id.options);
		if (this.optionsView != null)
		{
			// populate
			final Context context = getContext();
			this.optionsView.removeAllViews();
			for (int i = 0; i < this.values.length && i < this.labels.length && i < this.enable.length; i++)
			{
				final CharSequence value = this.values[i];
				final CharSequence label = this.labels[i];
				final boolean enable = this.enable[i];

				final RadioButton radioButton = new RadioButton(context);
				radioButton.setText(label);
				radioButton.setTag(value);
				radioButton.setEnabled(enable);
				this.optionsView.addView(radioButton);
			}

			// check listener
			this.optionsView.setOnCheckedChangeListener((group, checkedId) ->
			{
				if (checkedId == -1)
				{
					OpenEditTextPreference.this.editView.setText("");
				}
				else
				{
					final RadioButton radioButton = OpenEditTextPreference.this.optionsView.findViewById(checkedId);
					final String tag = radioButton.getTag().toString();
					OpenEditTextPreference.this.editView.setText(tag);
					OpenEditTextPreference.this.editView.setSelection(tag.length());
				}
			});
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
		 * @param superState superstate
		 */
		public SavedState(final Parcelable superState)
		{
			super(superState);
		}

		/**
		 * Constructor from parcel
		 *
		 * @param source source parcel
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
		public static final Creator<SavedState> CREATOR = new Creator<SavedState>()
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
