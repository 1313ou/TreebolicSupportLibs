/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.colors.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import org.treebolic.colors.R
import org.treebolic.colors.view.ColorPanelView
import org.treebolic.colors.view.ColorPickerView
import org.treebolic.colors.view.ColorPickerView.OnColorChangedListener

open class ColorPickerPreference : DialogPreference {

    /**
     * Color
     */
    protected var value: Int? = null

    // S E T T I N G S

    protected var alphaChannelVisible: Boolean = false

    protected var alphaChannelText: String? = null

    private var showDialogTitle: Boolean = false

    private var showPreviewSelectedColorInList: Boolean = true

    protected var colorPickerSliderColor: Int = -1

    protected var colorPickerBorderColor: Int = -1

    // C O N S T R U C T O R

    /**
     * Constructor
     *
     * @param context context
     * @param attrs   attributes
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.value = null

        // attributes
        init(context, attrs)

        // set up
        setup()
    }

    /**
     * Constructor
     *
     * @param context  context
     * @param attrs    attributes
     * @param defStyle def style
     */
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        // attributes
        init(context, attrs)

        // set up
        setup()
    }

    /**
     * Initialize
     *
     * @param context context
     * @param attrs   attributes
     */
    private fun init(context: Context, attrs: AttributeSet?) {
        // preference attributes
        context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference).use {
            showDialogTitle = it.getBoolean(R.styleable.ColorPickerPreference_showDialogTitle, false)
            showPreviewSelectedColorInList = it.getBoolean(R.styleable.ColorPickerPreference_showSelectedColorInList, true)
        }

        // view attributes
        context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView).use {
            alphaChannelVisible = it.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false)
            alphaChannelText = it.getString(R.styleable.ColorPickerView_alphaChannelText)
            colorPickerSliderColor = it.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, -1)
            colorPickerBorderColor = it.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, -1)
        }
    }

    /**
     * Set up
     */
    private fun setup() {
        // resource
        dialogLayoutResource = R.layout.dialog_color_picker

        // buttons
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)

        // persistence
        isPersistent = true

        // list
        if (this.showPreviewSelectedColorInList) {
            widgetLayoutResource = R.layout.preference_preview_layout
        }

        // title
        if (!this.showDialogTitle) {
            dialogTitle = null
        }
    }

    // V A L U E

    private fun persistValue(value: Int?): Boolean {
        //Log.d(TAG, "Persist " + this.value);
        if (value == null) {
            if (!shouldPersist()) {
                return false
            }

            val sharedPrefs = checkNotNull(sharedPreferences)
            val editor = sharedPrefs.edit()
            editor.remove(key)
            tryCommit(editor)
            return true
        } else {
            return persistInt(value)
        }
    }

    private fun getPersistedValue(defaultValue: Any?): Int? {
        val value = getPersistedInt(NULL_VALUE)
        if (value != NULL_VALUE) {
            return value
        }
        if (defaultValue != null) {
            return defaultValue as Int?
        }
        return null
    }

    override fun onGetDefaultValue(array: TypedArray, index: Int): Any? {
        return array.getInteger(index, NULL_VALUE)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setValue(getPersistedValue(defaultValue))
    }

    private fun setValue(value: Int?) {
        this.value = value
        persistValue(this.value)
        notifyChanged()
    }

    // B I N D

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val preview = holder.findViewById(R.id.preference_preview_color_panel) as ColorPanelView
        preview.setValue(this.value)
    }

    // D I A L O G   F R A G M E N T

    class ColorPickerPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat(), OnColorChangedListener {

        private lateinit var colorPickerView: ColorPickerView

        private var newColorView: ColorPanelView? = null

        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)

            // pref
            val pref = preference as ColorPickerPreference

            // view
            colorPickerView = view.findViewById(R.id.color_picker_view)
            colorPickerView.setOnColorChangedListener(this)
            newColorView = view.findViewById(R.id.color_panel_new)
            val oldColorView = view.findViewById<ColorPanelView>(R.id.color_panel_old)
            val preview = view.findViewById<ColorPanelView>(R.id.preference_preview_color_panel)

            // padding
            val landscapeLayout = view.findViewById<LinearLayout>(R.id.dialog_color_picker_extra_layout_landscape)
            val isLandscapeLayout = landscapeLayout != null
            if (isLandscapeLayout) {
                landscapeLayout!!.setPadding(0, 0, Math.round(colorPickerView.getDrawingOffset()), 0)
            } else {
                (oldColorView.parent as LinearLayout).setPadding(Math.round(colorPickerView.getDrawingOffset()), 0, Math.round(colorPickerView.getDrawingOffset()), 0)
            }

            // alpha
            colorPickerView.setAlphaSliderVisible(pref.alphaChannelVisible)
            colorPickerView.setAlphaSliderText(pref.alphaChannelText)

            // colors
            colorPickerView.setSliderTrackerColor(pref.colorPickerSliderColor)
            if (pref.colorPickerSliderColor != -1) {
                colorPickerView.setSliderTrackerColor(pref.colorPickerSliderColor)
            }
            if (pref.colorPickerBorderColor != -1) {
                colorPickerView.setBorderColor(pref.colorPickerBorderColor)
            }

            // old value
            oldColorView.setValue(pref.value)

            // new value
            var newColor = if (pref.value == null) START_COLOR else pref.value!!
            if (!pref.alphaChannelVisible) {
                newColor = newColor or -0x1000000
            }
            colorPickerView.setColor(newColor, true)

            // preview
            preview?.setValue(pref.value)
        }

        override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
            super.onPrepareDialogBuilder(builder)

            // Don't show the positive button; clicking a color will be the "positive" action
            val pref = preference as ColorPickerPreference

            // Neutral button to clear value
            builder.setNeutralButton(R.string.dialog_title_none) { _: DialogInterface?, _: Int ->
                if (pref.callChangeListener(null)) {
                    pref.setValue(null)
                }
                if (dialog != null) {
                    dialog!!.dismiss()
                }
            }
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            // when the user selects "OK", persist the new value
            if (positiveResult) {
                val newColor = colorPickerView.color
                val pref = preference as ColorPickerPreference
                if (pref.callChangeListener(newColor)) {
                    pref.setValue(newColor)
                }
            }
        }

        override fun onColorChanged(newColor: Int) {
            newColorView!!.setColor(newColor)
        }

        companion object {

            fun newInstance(pref: ColorPickerPreference): ColorPickerPreferenceDialogFragmentCompat {
                val fragment = ColorPickerPreferenceDialogFragmentCompat()
                val args = Bundle()
                args.putString(ARG_KEY, pref.key)
                fragment.arguments = args
                return fragment
            }
        }
    }

    // S A V E / R E S T O R E

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        // create instance of custom BaseSavedState
        val state = ColorSavedState(superState)

        // set the state's value with the class member that holds current setting value
        state.isNull = this.value == null
        state.value = if (this.value == null) 0 else value!!
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        // check whether we saved the state in onSaveInstanceState
        if (state == null || state.javaClass != ColorSavedState::class.java) {
            // didn't save the state, so call superclass
            super.onRestoreInstanceState(state)
            return
        }

        // cast state to custom BaseSavedState and pass to superclass
        val savedState = state as ColorSavedState
        super.onRestoreInstanceState(savedState.superState)

        // set this preference's widget to reflect the restored state
        setValue(if (savedState.isNull) null else savedState.value)
    }

    companion object {

        // V A L U E

        private const val NULL_VALUE = 0

        // V A L U E S

        private const val START_COLOR = Color.GRAY

        /**
         * Try to commit
         *
         * @param editor editor editor
         */
        @SuppressLint("CommitPrefEdits", "ApplySharedPref")
        private fun tryCommit(editor: SharedPreferences.Editor) {
            try {
                editor.apply()
            } catch (ignored: AbstractMethodError) {
                // The app injected its own pre-Gingerbread SharedPreferences.Editor implementation without an apply method.
                editor.commit()
            }
        }

        private const val DIALOG_FRAGMENT_TAG = "ColorPickerPreference"

        /**
         * onDisplayPreferenceDialog helper
         *
         * @param prefFragment preference fragment
         * @param preference   preference
         * @return false if not handled: call super.onDisplayPreferenceDialog(preference)
         */
        @JvmStatic
        fun onDisplayPreferenceDialog(prefFragment: PreferenceFragmentCompat, preference: Preference?): Boolean {
            val manager: FragmentManager
            try {
                manager = prefFragment.parentFragmentManager
            } catch (e: IllegalStateException) {
                return false
            }
            if (manager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
                return true
            }

            if (preference is ColorPickerPreference) {
                val dialogFragment: DialogFragment = ColorPickerPreferenceDialogFragmentCompat.newInstance(preference)
                dialogFragment.setTargetFragment(prefFragment, 0)
                dialogFragment.show(manager, DIALOG_FRAGMENT_TAG)
                return true
            }
            return false
        }

        /**
         * Summary provider for color
         */
        @JvmField
        val SUMMARY_PROVIDER: SummaryProvider<ColorPickerPreference> = SummaryProvider { preference: ColorPickerPreference ->
            val context = preference.context
            val value = preference.getPersistedValue(null)
            if (value == null) context.getString(R.string.pref_value_default) else Integer.toHexString(value)
        }
    }
}
