/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.search

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import org.treebolic.wheel.AbstractWheel
import org.treebolic.wheel.OnWheelScrollListener
import org.treebolic.wheel.WheelView
import org.treebolic.wheel.adapters.AbstractWheelTextAdapter

/**
 * Search settings
 *
 * @author Bernard Bou
 */
class SearchSettings : AppCompatDialogFragment() {

    private var scrolling = false

    private var scopeWheel: WheelView? = null

    private var modeWheel: WheelView? = null

    private var modeAdapter: Adapter? = null

    private var sourceAdapter: Adapter? = null

    private lateinit var modes: Array<String>

    private lateinit var scopes: Array<String>

    private lateinit var sources: Array<String>

    private var sourceModeIndex = 0

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val resources = context.resources
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        // get strings
        val scopeLabels = resources.getStringArray(R.array.search_scope_labels)
        val modeLabels = resources.getStringArray(R.array.search_mode_labels)
        val sourceLabels = resources.getStringArray(R.array.search_source_labels)
        this.modes = resources.getStringArray(R.array.search_modes)
        this.scopes = resources.getStringArray(R.array.search_scopes)
        this.sources = resources.getStringArray(R.array.search_sources)
        this.sourceModeIndex = resources.getInteger(R.integer.search_scope_source_index)
        val defaultScopeIndex = resources.getInteger(R.integer.search_scope_default)
        val defaultModeIndex = resources.getInteger(R.integer.search_mode_default)

        // get icons
        val scopeIcons = intArrayOf(R.drawable.ic_search_scope_label, R.drawable.ic_search_scope_id, R.drawable.ic_search_scope_content, R.drawable.ic_search_scope_link, R.drawable.ic_search_scope_source)
        val modeIcons = intArrayOf(R.drawable.ic_search_mode_equals, R.drawable.ic_search_mode_startswith, R.drawable.ic_search_mode_includes)
        val sourceIcons = intArrayOf(R.drawable.ic_search_mode_equals)

        // wheel2 adapter
        this.modeAdapter = Adapter(context, R.layout.item_mode, modeLabels, modeIcons, modes.size, Adapter.Type.MODE)
        this.sourceAdapter = Adapter(context, R.layout.item_mode, sourceLabels, sourceIcons, sources.size, Adapter.Type.SOURCE)

        // initial values for scope
        var scopeIndex = defaultScopeIndex
        val scope = sharedPref.getString(PREF_SEARCH_SCOPE, null)
        Log.d(TAG, "Scope $scope")
        if (scope != null) {
            for (i in scopes.indices) {
                if (scope == scopes[i]) {
                    scopeIndex = i
                    break
                }
            }
        } else {
            val editor = sharedPref.edit().putString(PREF_SEARCH_SCOPE, scopes[defaultScopeIndex])
            tryCommit(editor)
        }
        // initial values for mode
        var modeIndex = defaultModeIndex
        if (scopeIndex < scopes.size - 1) // label id content link
        {
            modeIndex = 1
            val mode = sharedPref.getString(PREF_SEARCH_MODE, null)
            Log.d(TAG, "Mode $mode")
            if (mode != null) {
                for (i in modes.indices) {
                    if (mode == modes[i]) {
                        modeIndex = i
                        break
                    }
                }
            } else {
                val editor = sharedPref.edit().putString(PREF_SEARCH_MODE, modes[defaultModeIndex])
                tryCommit(editor)
            }
        } else  // source
        {
            if (sharedPref.contains(PREF_SEARCH_MODE)) {
                val editor = sharedPref.edit().remove(PREF_SEARCH_MODE)
                tryCommit(editor)
            }
        }

        // dialog

        // val dialog: Dialog  = AppCompatDialog(requireActivity())
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // or
        // dialog.setTitle(R.string.search_title)
        // dialog.setContentView(R.layout.dialog_search_settings)
        // dialog.window?.setBackgroundDrawableResource(R.drawable.bg_semitransparent_rounded)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_search_settings, null)

        // wheel 1abandon
        this.scopeWheel = view.findViewById(R.id.scope)
        checkNotNull(this.scopeWheel)
        scopeWheel!!.visibleItems = 4
        scopeWheel!!.viewAdapter = Adapter(context, R.layout.item_scope, scopeLabels, scopeIcons, scopes.size, Adapter.Type.SCOPE)

        // wheel 1 events
        scopeWheel!!.addChangingListener { _: AbstractWheel?, _: Int, newValue: Int ->
            Log.d(TAG, "Wheel 1 " + newValue + ' ' + scopes[newValue])
            val editor = sharedPref.edit().putString(PREF_SEARCH_SCOPE, scopes[newValue])
            tryCommit(editor)
            if (!this@SearchSettings.scrolling) {
                updateWheel2(newValue)
            }
        }
        scopeWheel!!.addScrollingListener(object : OnWheelScrollListener {
            override fun onScrollingStarted(wheel: AbstractWheel) {
                this@SearchSettings.scrolling = true
            }

            override fun onScrollingFinished(wheel: AbstractWheel) {
                this@SearchSettings.scrolling = false
                updateWheel2(scopeWheel!!.currentItem)
            }
        })

        // wheel 2
        this.modeWheel = view.findViewById(R.id.mode)
        checkNotNull(this.modeWheel)
        modeWheel!!.visibleItems = 4
        modeWheel!!.viewAdapter = this.modeAdapter //new Adapter(context, R.layout.item_mode, modeLabels, modeIcons, this.modes.length, Adapter.Type.MODE));

        // wheel 2 events
        modeWheel!!.addChangingListener { wheel: AbstractWheel, _: Int, newValue: Int ->
            val wheelViewAdapter = wheel.viewAdapter
            val adapter = wheelViewAdapter as Adapter
            if (adapter.type == Adapter.Type.MODE) {
                Log.d(TAG, "Wheel 2 " + newValue + ' ' + modes[newValue])
                val editor = sharedPref.edit().putString(PREF_SEARCH_MODE, modes[newValue])
                tryCommit(editor)
            } else if (adapter.type == Adapter.Type.SOURCE) {
                Log.d(TAG, "Wheel 2 " + newValue + ' ' + sources[newValue])
                val editor = sharedPref.edit().putString(PREF_SEARCH_MODE, sources[newValue])
                tryCommit(editor)
            }
        }

        // wheels initial
        scopeWheel!!.currentItem = scopeIndex
        modeWheel!!.currentItem = modeIndex

        //final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        val builder = AlertDialog.Builder(ContextThemeWrapper(requireActivity(), R.style.AlertDialogCustom))
        return builder //
            .setView(view).setPositiveButton(R.string.title_yes) { dialog2: DialogInterface, _: Int -> dialog2.dismiss() } //
            .create()
    }

    /**
     * Updates item_mode wheel depending on item_scope
     */
    private var oldModeIndex = 1

    private fun updateWheel2(scopeIndex: Int) {
        val modeIndex: Int
        val adapter: Adapter?
        if (scopeIndex < this.sourceModeIndex) // item_scope != source
        {
            modeIndex = this.oldModeIndex
            adapter = this.modeAdapter
        } else  // item_scope == source
        {
            modeIndex = 0
            adapter = this.sourceAdapter
            this.oldModeIndex = modeWheel!!.currentItem
        }
        modeWheel!!.viewAdapter = adapter
        modeWheel!!.currentItem = modeIndex
    }

    /**
     * Adapter for scopes
     */
    private class Adapter(context0: Context, layout0: Int, val labels: Array<String?>, val icons: IntArray, val len: Int, val type: Type) : AbstractWheelTextAdapter(context0, layout0, NO_RESOURCE) {

        enum class Type {
            SCOPE, MODE, SOURCE
        }

        /**
         * Constructor
         */
        init {
            itemTextResource = R.id.wheel_name
        }

        override fun getItem(index: Int, cachedView: View, parent: ViewGroup): View {
            val view = checkNotNull(super.getItem(index, cachedView, parent))
            val img = view.findViewById<ImageView>(R.id.wheel_icon)
            img.setImageResource(icons[index])
            return view
        }

        override fun getItemsCount(): Int {
            return this.len
        }

        override fun getItemText(index: Int): CharSequence? {
            return labels[index]
        }
    }

    companion object {

        private const val TAG = "SearchSettings"

        const val PREF_SEARCH_SCOPE: String = "pref_search_scope"

        const val PREF_SEARCH_MODE: String = "pref_search_mode"

        const val SCOPE_SOURCE: String = "SOURCE"

        const val SCOPE_LABEL: String = "LABEL"

        const val SCOPE_CONTENT: String = "CONTENT"

        const val SCOPE_LINK: String = "LINK"

        const val SCOPE_ID: String = "ID"

        const val MODE_STARTSWITH: String = "STARTSWITH"

        const val MODE_EQUALS: String = "EQUALS"

        const val MODE_INCLUDES: String = "INCLUDES"

        const val MODE_IS: String = "IS"

        private fun newInstance(): SearchSettings {
            return SearchSettings()
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
            } catch (ignored: AbstractMethodError) {
                // The app injected its own pre-Gingerbread SharedPreferences.Editor implementation without an apply method.
                editor.commit()
            }
        }

        /**
         * Show
         */
        @JvmStatic
        fun show(fragmentManager: FragmentManager) {
            val newFragment: AppCompatDialogFragment = newInstance()
            newFragment.show(fragmentManager, "dialog")
        }
    }
}
