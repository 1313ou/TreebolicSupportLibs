/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.colors.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import org.treebolic.colors.R;
import org.treebolic.colors.view.ColorPanelView;
import org.treebolic.colors.view.ColorPickerView;
import org.treebolic.colors.view.ColorPickerView.OnColorChangedListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class ColorPickerDialog extends AlertDialog implements ColorPickerView.OnColorChangedListener
{
	static private final int START_COLOR = Color.GRAY;

	private ColorPickerView colorPicker;

	private ColorPanelView newColorView;

	private final OnColorChangedListener listener;

	public ColorPickerDialog(@NonNull final Context context, final Integer initialColor)
	{
		this(context, initialColor, null);
	}

	@SuppressWarnings("WeakerAccess")
	public ColorPickerDialog(@NonNull final Context context, @Nullable final Integer initialColor, @SuppressWarnings("SameParameterValue") final OnColorChangedListener listener)
	{
		super(context);
		this.listener = listener;
		init(context, initialColor);
	}

	private void init(@NonNull final Context context, @Nullable final Integer color)
	{
		// to fight value branding.
		final Window window = getWindow();
		if (window != null)
		{
			window.setFormat(PixelFormat.RGBA_8888);
		}
		setUp(context, color);
	}

	@SuppressLint("InflateParams")
	private void setUp(@NonNull final Context context, @Nullable final Integer color)
	{
		boolean isLandscapeLayout = false;

		// custom view
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert inflater != null;
		final View layout = inflater.inflate(R.layout.dialog_color_picker, null);
		setView(layout);

		setTitle(R.string.dialog_title);
		// setIcon(android.R.drawable.ic_dialog_info);

		final LinearLayout landscapeLayout = layout.findViewById(R.id.dialog_color_picker_extra_layout_landscape);
		if (landscapeLayout != null)
		{
			isLandscapeLayout = true;
		}

		this.colorPicker = layout.findViewById(R.id.color_picker_view);
		this.newColorView = layout.findViewById(R.id.color_panel_new);
		ColorPanelView oldColorView = layout.findViewById(R.id.color_panel_old);

		if (!isLandscapeLayout)
		{
			((LinearLayout) oldColorView.getParent()).setPadding(Math.round(this.colorPicker.getDrawingOffset()), 0, Math.round(this.colorPicker.getDrawingOffset()), 0);
		}
		else
		{
			landscapeLayout.setPadding(0, 0, Math.round(this.colorPicker.getDrawingOffset()), 0);
			setTitle(null);
		}

		this.colorPicker.setOnColorChangedListener(this);

		// set colors
		int newColor = color != null ? color : START_COLOR;
		oldColorView.setValue(color);
		this.newColorView.setColor(newColor);
		this.colorPicker.setColor(newColor, true);
	}

	@Override
	public void onColorChanged(final int color)
	{
		this.newColorView.setColor(color);

		if (this.listener != null)
		{
			this.listener.onColorChanged(color);
		}
	}

	public void setAlphaSliderVisible(@SuppressWarnings("SameParameterValue") final boolean visible)
	{
		this.colorPicker.setAlphaSliderVisible(visible);
	}

	public int getColor()
	{
		return this.colorPicker.getColor();
	}
}
