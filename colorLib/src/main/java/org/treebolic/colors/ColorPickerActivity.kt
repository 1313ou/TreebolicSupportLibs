/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.colors

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import org.treebolic.AppCompatCommonActivity
import org.treebolic.colors.view.ColorPanelView
import org.treebolic.colors.view.ColorPickerView
import org.treebolic.colors.view.ColorPickerView.OnColorChangedListener
import kotlin.math.roundToInt

@SuppressLint("Registered")
class ColorPickerActivity : AppCompatCommonActivity(), OnColorChangedListener, View.OnClickListener {

    private lateinit var colorPickerView: ColorPickerView

    private lateinit var newColorPanelView: ColorPanelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFormat(PixelFormat.RGBA_8888)
        setContentView(R.layout.activity_color_picker)

        colorPickerView = findViewById(R.id.color_picker_view)
        newColorPanelView = findViewById(R.id.color_panel_new)
        val oldColorPanelView = findViewById<ColorPanelView>(R.id.color_panel_old)
        (oldColorPanelView.parent as LinearLayout).setPadding(colorPickerView.drawingOffset.toFloat().roundToInt(), 0, colorPickerView.drawingOffset.toFloat().roundToInt(), 0)

        colorPickerView.setOnColorChangedListener(this)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val initialColor = sharedPrefs.getInt("color_3", -0x1000000)
        colorPickerView.setColor(initialColor, true)
        oldColorPanelView.color = initialColor

        val okButton = findViewById<Button>(R.id.okButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        okButton.setOnClickListener(this)
        cancelButton.setOnClickListener(this)
    }

    override fun onColorChanged(newColor: Int) {
        newColorPanelView.color = colorPickerView.color
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.okButton) {
            val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
            editor.putInt("color_3", colorPickerView.color)
            tryCommit(editor)
            finish()
        } else if (id == R.id.cancelButton) {
            finish()
        }
    }

    /**
     * Try to commit
     *
     * @param editor editor editor
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    private fun tryCommit(editor: SharedPreferences.Editor) {
        try {
            editor.apply()
        } catch (_: AbstractMethodError) {
            editor.commit()
        }
    }
}
