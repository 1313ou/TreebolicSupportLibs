/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.treebolic.colors.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import org.treebolic.colors.view.ColorPanelView;
import org.treebolic.colors.view.ColorPickerView;
import org.treebolic.colors.view.ColorPickerView.OnColorChangedListener;

public class ColorPickerDialog extends AlertDialog implements ColorPickerView.OnColorChangedListener
{
	private ColorPickerView mColorPicker;

	private ColorPanelView mNewColor;

	private final OnColorChangedListener mListener;

	public ColorPickerDialog(final Context context, final Integer initialColor)
	{
		this(context, initialColor, null);
		init(initialColor);
	}

	@SuppressWarnings("WeakerAccess")
	public ColorPickerDialog(final Context context, final Integer initialColor, @SuppressWarnings("SameParameterValue") final OnColorChangedListener listener)
	{
		super(context);
		this.mListener = listener;
		init(initialColor);
	}

	private void init(final Integer color)
	{
		// to fight color branding.
		final Window window = getWindow();
		if (window != null)
		{
			window.setFormat(PixelFormat.RGBA_8888);
		}
		setUp(color);
	}

	@SuppressLint("InflateParams")
	private void setUp(final Integer color)
	{
		boolean isLandscapeLayout = false;

		// custom view
		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		this.mColorPicker = layout.findViewById(R.id.color_picker_view);
		ColorPanelView mOldColor = layout.findViewById(R.id.color_panel_old);
		this.mNewColor = layout.findViewById(R.id.color_panel_new);

		if (!isLandscapeLayout)
		{
			((LinearLayout) mOldColor.getParent()).setPadding(Math.round(this.mColorPicker.getDrawingOffset()), 0, Math.round(this.mColorPicker.getDrawingOffset()), 0);
		}
		else
		{
			landscapeLayout.setPadding(0, 0, Math.round(this.mColorPicker.getDrawingOffset()), 0);
			setTitle(null);
		}

		this.mColorPicker.setOnColorChangedListener(this);

		if (color != null)
		{
			mOldColor.setColor(color);
			this.mColorPicker.setColor(color, true);
		}
	}

	@Override
	public void onColorChanged(final int color)
	{
		this.mNewColor.setColor(color);

		if (this.mListener != null)
		{
			this.mListener.onColorChanged(color);
		}
	}

	public void setAlphaSliderVisible(@SuppressWarnings("SameParameterValue") final boolean visible)
	{
		this.mColorPicker.setAlphaSliderVisible(visible);
	}

	public int getColor()
	{
		return this.mColorPicker.getColor();
	}
}
