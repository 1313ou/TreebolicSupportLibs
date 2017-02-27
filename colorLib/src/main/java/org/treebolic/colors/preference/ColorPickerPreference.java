package org.treebolic.colors.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import org.treebolic.colors.R;
import org.treebolic.colors.view.ColorPanelView;
import org.treebolic.colors.view.ColorPickerView;

public class ColorPickerPreference extends DialogPreference implements ColorPickerView.OnColorChangedListener
{
	// V I E W S
	/**
	 * Picker view
	 */
	protected ColorPickerView mColorPickerView;

	/**
	 * Old color view
	 */
	protected ColorPanelView mOldColorView;

	/**
	 * New color view
	 */
	protected ColorPanelView mNewColorView;

	// C O L O R

	/**
	 * Color
	 */
	protected int mColor;

	/**
	 * S E T T I N G S
	 */
	protected boolean alphaChannelVisible = false;
	protected String alphaChannelText = null;
	protected boolean showDialogTitle = false;
	protected boolean showPreviewSelectedColorInList = true;
	protected int colorPickerSliderColor = -1;
	protected int colorPickerBorderColor = -1;

	/**
	 * Constructor
	 *
	 * @param context
	 *            context
	 * @param attrs
	 *            attributes
	 */
	public ColorPickerPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs);
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            context
	 * @param attrs
	 *            attributes
	 * @param defStyle
	 *            def style
	 */
	public ColorPickerPreference(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs);
	}

	/**
	 * Initialize
	 *
	 * @param attrs
	 *            attributes
	 */
	private void init(final AttributeSet attrs)
	{
		// preference attributes
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
		this.showDialogTitle = a.getBoolean(R.styleable.ColorPickerPreference_showDialogTitle, false);
		this.showPreviewSelectedColorInList = a.getBoolean(R.styleable.ColorPickerPreference_showSelectedColorInList, true);
		a.recycle();

		// view attributes
		a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
		this.alphaChannelVisible = a.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false);
		this.alphaChannelText = a.getString(R.styleable.ColorPickerView_alphaChannelText);
		this.colorPickerSliderColor = a.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, -1);
		this.colorPickerBorderColor = a.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, -1);
		a.recycle();

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

		// resource
		setDialogLayoutResource(R.layout.dialog_color_picker);

		// buttons
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		// persistence
		setPersistent(true);
	}

	// B I N D

	@Override
	protected void onBindView(final View view)
	{
		super.onBindView(view);

		final ColorPanelView preview = (ColorPanelView) view.findViewById(R.id.preference_preview_color_panel);
		if (preview != null)
		{
			preview.setColor(this.mColor);
		}
	}

	@Override
	protected void onBindDialogView(final View layout)
	{
		super.onBindDialogView(layout);

		this.mColorPickerView = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
		this.mOldColorView = (ColorPanelView) layout.findViewById(R.id.color_panel_old);
		this.mNewColorView = (ColorPanelView) layout.findViewById(R.id.color_panel_new);

		// padding
		boolean isLandscapeLayout = false;
		final LinearLayout landscapeLayout = (LinearLayout) layout.findViewById(R.id.dialog_color_picker_extra_layout_landscape);
		if (landscapeLayout != null)
		{
			isLandscapeLayout = true;
		}
		if (!isLandscapeLayout)
		{
			((LinearLayout) this.mOldColorView.getParent()).setPadding(Math.round(this.mColorPickerView.getDrawingOffset()), 0,
					Math.round(this.mColorPickerView.getDrawingOffset()), 0);
		}
		else
		{
			landscapeLayout.setPadding(0, 0, Math.round(this.mColorPickerView.getDrawingOffset()), 0);
		}

		// alpha
		this.mColorPickerView.setAlphaSliderVisible(this.alphaChannelVisible);
		this.mColorPickerView.setAlphaSliderText(this.alphaChannelText);

		// colors
		this.mColorPickerView.setSliderTrackerColor(this.colorPickerSliderColor);
		if (this.colorPickerSliderColor != -1)
		{
			this.mColorPickerView.setSliderTrackerColor(this.colorPickerSliderColor);
		}
		if (this.colorPickerBorderColor != -1)
		{
			this.mColorPickerView.setBorderColor(this.colorPickerBorderColor);
		}

		// listener
		this.mColorPickerView.setOnColorChangedListener(this);

		// Log.d("mColorPicker", "setting initial color!");
		int color0 = this.mColor;
		if (!this.alphaChannelVisible)
		{
			color0 |= 0xFF000000;
		}
		this.mOldColorView.setColor(this.mColor);
		this.mColorPickerView.setColor(color0, true);
	}

	// C L O S E

	@SuppressWarnings("boxing")
	@Override
	protected void onDialogClosed(final boolean positiveResult)
	{
		if (positiveResult)
		{
			this.mColor = this.mColorPickerView.getColor();
			if (callChangeListener(this.mColor))
			{
				persistInt(this.mColor);
				notifyChanged();
			}
		}
	}

	// V A L U E S

	@SuppressWarnings("boxing")
	@Override
	protected void onSetInitialValue(final boolean restorePersistedValue, final Object defaultValue)
	{
		if (restorePersistedValue)
		{
			this.mColor = getPersistedInt(0xFF000000);
			// Log.d("mColorPicker", "Load saved color: " + mColor);
		}
		else
		{
			this.mColor = (Integer) defaultValue;
			persistInt(this.mColor);
		}
	}

	@SuppressWarnings("boxing")
	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index)
	{
		return a.getInteger(index, 0xFF000000);
	}

	@Override
	public void onColorChanged(final int newColor)
	{
		this.mNewColorView.setColor(newColor);
	}

	// S A V E / R E S T O R E

	@Override
	protected Parcelable onSaveInstanceState()
	{
		final Parcelable superState = super.onSaveInstanceState();

		// Create instance of custom BaseSavedState
		final SavedState myState = new SavedState(superState);
		// Set the state's value with the class member that holds current setting value

		if (getDialog() != null && this.mColorPickerView != null)
		{
			myState.currentColor = this.mColorPickerView.getColor();
		}
		else
		{
			myState.currentColor = 0;
		}
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(final Parcelable state)
	{
		// Check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(SavedState.class))
		{
			// Didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// Cast state to custom BaseSavedState and pass to superclass
		final SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());

		// Set this Preference's widget to reflect the restored state
		if (getDialog() != null && this.mColorPickerView != null)
		{
			// Log.d("mColorPicker", "Restoring color!");
			this.mColorPickerView.setColor(myState.currentColor, true);
		}
	}

	private static class SavedState extends BaseSavedState
	{
		// Member that holds the setting's value
		int currentColor;

		public SavedState(final Parcelable superState)
		{
			super(superState);
		}

		public SavedState(final Parcel source)
		{
			super(source);
			// Get the current preference's value
			this.currentColor = source.readInt();
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags)
		{
			super.writeToParcel(dest, flags);
			// Write the preference's value
			dest.writeInt(this.currentColor);
		}

		// Standard creator object using an instance of this class
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
}
