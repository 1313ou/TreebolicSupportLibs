/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.treebolic.AppCompatCommonUtils.getThemePref
import org.treebolic.common.R

abstract class AppCompatCommonPreferenceActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        // theme
        val themeId = getThemePref(this)
        if (themeId != null) {
            setTheme(themeId)
        }

        // super
        super.onCreate(savedInstanceState)

        // content view
        setContentView(R.layout.activity_settings)

        // fragment manager
        val fm = supportFragmentManager

        // fragment
        if (savedInstanceState == null) {
            var fragmentClassName: String? = null
            val args = intent.extras
            if (args != null && args.containsKey(INITIAL_ARG)) {
                fragmentClassName = args.getString(INITIAL_ARG)
            }
            val fragment = if (fragmentClassName == null) {
                HeaderFragment()
            } else {
                supportFragmentManager.fragmentFactory.instantiate(classLoader, fragmentClassName)
            }
            fm.beginTransaction().replace(R.id.settings, fragment).commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        fm.addOnBackStackChangedListener {
            if (fm.backStackEntryCount == 0) {
                setTitle(R.string.title_settings)
            } else {
                var title: CharSequence? = null
                val fragments = fm.fragments
                if (fragments.isNotEmpty()) {
                    val fragment = fragments[0] // only one at a time
                    val preferenceFragment = fragment as PreferenceFragmentCompat
                    title = preferenceFragment.preferenceScreen.title
                }
                if (title == null || title!!.isEmpty()) {
                    setTitle(R.string.title_settings)
                } else {
                    setTitle(title)
                }
            }
        }

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }
    }

    // U T I L S

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val className = checkNotNull(pref.fragment)
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, className)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)

        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction().replace(R.id.settings, fragment).addToBackStack(null).commit()
        return true
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.preferences, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_reset_settings) {
            resetSettings()
            restart()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // U P

    override fun onSupportNavigateUp(): Boolean {
        val fm = supportFragmentManager
        if (fm.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    // H E A D E R   F R A G M E N T

    class HeaderFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_headers, rootKey)
        }
    }

    // U T I L S

    /**
     * Reset settings
     */
    private fun resetSettings() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPrefs.edit()
        editor.clear()
        tryCommit(editor)
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
     * Restart app
     */
    private fun restart() {
        val restartIntent = checkNotNull(packageManager.getLaunchIntentForPackage(packageName))
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(restartIntent)
    }

    companion object {

        private const val TITLE_TAG = "settingsActivityTitle"

        const val INITIAL_ARG: String = "settings_initial"
    }
}
