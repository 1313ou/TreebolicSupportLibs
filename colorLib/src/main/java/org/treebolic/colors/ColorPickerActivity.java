/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

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

import androidx.annotation.NonNull;

@SuppressLint("Registered")
public class ColorPickerActivity extends AppCompatCommonActivity implements OnColorChangedListener, View.OnClickListener
{
	private ColorPickerView colorPickerView;

	private ColorPanelView newColorPanelView;

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

		this.colorPickerView = findViewById(R.id.color_picker_view);
		this.newColorPanelView = findViewById(R.id.color_panel_new);
		ColorPanelView oldColorPanelView = findViewById(R.id.color_panel_old);

		Button okButton = findViewById(R.id.okButton);
		Button cancelButton = findViewById(R.id.cancelButton);

		((LinearLayout) oldColorPanelView.getParent()).setPadding(Math.round(this.colorPickerView.getDrawingOffset()), 0, Math.round(this.colorPickerView.getDrawingOffset()), 0);

		this.colorPickerView.setOnColorChangedListener(this);
		this.colorPickerView.setColor(initialColor, true);
		oldColorPanelView.setColor(initialColor);

		okButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

	}

	@Override
	public void onColorChanged(final int newColor)
	{
		this.newColorPanelView.setColor(this.colorPickerView.getColor());
	}

	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	@Override
	public void onClick(@NonNull final View v)
	{
		final int id = v.getId();
		if (id == R.id.okButton)
		{
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
			edit.putInt("color_3", this.colorPickerView.getColor());
			edit.commit();
			finish();
		}
		else if (id == R.id.cancelButton)
		{
			finish();
		}
	}
}
