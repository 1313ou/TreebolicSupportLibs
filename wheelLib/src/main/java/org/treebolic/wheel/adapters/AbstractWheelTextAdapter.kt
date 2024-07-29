/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel.adapters

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes

/**
 * Abstract spinnerwheel adapter provides common functionality for adapters.
 */
abstract class AbstractWheelTextAdapter protected constructor(
    protected val context: Context,
    private var itemResource: Int = TEXT_VIEW_ITEM_RESOURCE,
    var itemTextResource: Int = NO_RESOURCE

) : AbstractWheelAdapter() {

    private var textTypeface: Typeface? = null
    private var textColor: Int = DEFAULT_TEXT_COLOR
    private var textSize: Int = DEFAULT_TEXT_SIZE

    /**
     * Returns text for specified item
     *
     * @param index the item index
     * @return the text of specified items
     */
    protected abstract fun getItemText(index: Int): CharSequence?

    override fun getItem(index: Int, convertView: View, parent: ViewGroup): View? {
        if (index in 0 until itemsCount) {
            val textView = getTextView(convertView, itemTextResource)
            if (textView != null) {
                var text = getItemText(index)
                if (text == null) {
                    text = ""
                }
                textView.text = text
                configureTextView(textView)
            }
            return convertView
        }
        return null
    }

    override fun getEmptyItem(convertView: View, parent: ViewGroup): View? {
        if (convertView is TextView) {
            configureTextView(convertView)
        }
        return convertView
    }

    /**
     * Configures text view. Is called for the TEXT_VIEW_ITEM_RESOURCE views.
     *
     * @param view the text view to be configured
     */
    private fun configureTextView(view: TextView) {
        if (itemResource == TEXT_VIEW_ITEM_RESOURCE) {
            view.setTextColor(textColor)
            view.gravity = Gravity.CENTER
            view.textSize = textSize.toFloat()
            view.setLines(1)
        }
        if (textTypeface != null) {
            view.typeface = textTypeface
        } else {
            view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
        }
    }

    companion object {

        /**
         * Text view resource. Used as a default view for adapter.
         */
        const val TEXT_VIEW_ITEM_RESOURCE: Int = -1

        /**
         * No resource constant.
         */
        const val NO_RESOURCE: Int = 0

        /**
         * Default text color
         */
        const val DEFAULT_TEXT_COLOR: Int = -0xefeff0

        /**
         * Default text size
         */
        const val DEFAULT_TEXT_SIZE: Int = 24

        /**
         * Loads a text view from view
         *
         * @param view      the text view or layout containing it
         * @param textResId the text resource Id in layout
         * @return the loaded text view
         */
        private fun getTextView(view: View?, @IdRes textResId: Int): TextView? {
            var text: TextView? = null
            try {
                if (textResId == NO_RESOURCE && view is TextView) {
                    text = view
                } else if (textResId != NO_RESOURCE) {
                    text = view!!.findViewById(textResId)
                }
            } catch (e: ClassCastException) {
                Log.e("AbstractWheelAdapter", "You must supply a resource ID for a TextView")
                throw IllegalStateException("AbstractWheelAdapter requires the resource ID to be a TextView", e)
            }
            return text
        }
    }
}
