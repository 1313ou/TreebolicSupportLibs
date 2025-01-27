/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.colors.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import org.treebolic.colors.R
import org.treebolic.colors.view.ColorPanelView
import org.treebolic.colors.view.ColorPickerView
import org.treebolic.colors.view.ColorPickerView.OnColorChangedListener

class ColorPickerDialog(context: Context, initialColor: Int?, private val listener: OnColorChangedListener?) : AlertDialog(context), OnColorChangedListener {

    val color: Int
        get() = colorPicker.color

    private lateinit var colorPicker: ColorPickerView

    private lateinit var newColorView: ColorPanelView

    constructor(context: Context, initialColor: Int?) : this(context, initialColor, null)

    init {
        init(context, initialColor)
    }

    private fun init(context: Context, color: Int?) {
        val window = window
        window?.setFormat(PixelFormat.RGBA_8888)
        setUp(context, color)
    }

    @SuppressLint("InflateParams")
    private fun setUp(context: Context, color: Int?) {
        var isLandscapeLayout = false

        setTitle(R.string.dialog_title)

        // custom view
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.dialog_color_picker, null)
        setView(layout)

        // components
        colorPicker = layout.findViewById(R.id.color_picker_view)
        newColorView = layout.findViewById(R.id.color_panel_new)
        val oldColorView = layout.findViewById<ColorPanelView>(R.id.color_panel_old)

        // padding
        val landscapeLayout = layout.findViewById<LinearLayout>(R.id.dialog_color_picker_extra_layout_landscape)
        if (landscapeLayout != null) {
            isLandscapeLayout = true
        }
        if (!isLandscapeLayout) {
            (oldColorView.parent as LinearLayout).setPadding(Math.round(colorPicker.getDrawingOffset()), 0, Math.round(colorPicker.getDrawingOffset()), 0)
        } else {
            landscapeLayout!!.setPadding(0, 0, Math.round(colorPicker.getDrawingOffset()), 0)
            setTitle(null)
        }

        // listener
        colorPicker.setOnColorChangedListener(this)

        // set colors
        val newColor = color ?: START_COLOR
        oldColorView.setValue(color)
        newColorView.color = newColor
        colorPicker.setColor(newColor, true)
    }

    override fun onColorChanged(newColor: Int) {
        newColorView.color = newColor
        listener?.onColorChanged(newColor)
    }

    fun setAlphaSliderVisible(visible: Boolean) {
        colorPicker.setAlphaSliderVisible(visible)
    }

    companion object {

        private const val START_COLOR = Color.GRAY
    }
}
