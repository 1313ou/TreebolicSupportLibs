package org.treebolic.colors;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.colors.view.ColorPanelView;
import org.treebolic.colors.view.ColorPickerView;
import org.treebolic.colors.view.ColorPickerView.OnColorChangedListener;

@SuppressLint("Registered")
public class ColorPickerActivity extends AppCompatCommonActivity implements OnColorChangedListener, View.OnClickListener
{
	private ColorPickerView mColorPickerView;
	private ColorPanelView mNewColorPanelView;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		setContentView(R.layout.activity_color_picker);

		init();
	}

	private void init()
	{
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		final int initialColor = sharedPrefs.getInt("color_3", 0xFF000000);

		this.mColorPickerView = findViewById(R.id.color_picker_view);
		ColorPanelView mOldColorPanelView = findViewById(R.id.color_panel_old);
		this.mNewColorPanelView = findViewById(R.id.color_panel_new);

		Button mOkButton = findViewById(R.id.okButton);
		Button mCancelButton = findViewById(R.id.cancelButton);

		((LinearLayout) mOldColorPanelView.getParent()).setPadding(Math.round(this.mColorPickerView.getDrawingOffset()), 0, Math.round(this.mColorPickerView.getDrawingOffset()), 0);

		this.mColorPickerView.setOnColorChangedListener(this);
		this.mColorPickerView.setColor(initialColor, true);
		mOldColorPanelView.setColor(initialColor);

		mOkButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);

	}

	@Override
	public void onColorChanged(final int newColor)
	{
		this.mNewColorPanelView.setColor(this.mColorPickerView.getColor());
	}

	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	@Override
	public void onClick(final View v)
	{
		final int id = v.getId();
		if (id == R.id.okButton)
		{
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
			edit.putInt("color_3", this.mColorPickerView.getColor());
			edit.commit();
			finish();
		}
		else if (id == R.id.cancelButton)
		{
			finish();
		}
	}
}
