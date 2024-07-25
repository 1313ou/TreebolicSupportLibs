/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.filechooser

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import org.treebolic.AppCompatCommonActivity
import java.io.File
import java.io.FileFilter
import java.util.Locale

/**
 * File chooser
 *
 * @author Bernard Bou
 */
@SuppressLint("Registered")
class FileChooserActivity : AppCompatCommonActivity(), OnItemLongClickListener, OnItemClickListener {

    /**
     * File entry
     *
     * @property name   name
     * @property data   data
     * @property path   path
     * @property isFolder is folder
     * @property isParent is parent
     * */
    data class Entry(
        val name: String,
        val data: String,
        val path: String,
        val isFolder: Boolean,
        val isParent: Boolean,
        val isNone: Boolean
    ) : Comparable<Entry> {

        constructor(name: String, data: String, path: String, isFolder: Boolean, isParent: Boolean) : this(name, data, path, isFolder, isParent, false)

        override fun compareTo(other: Entry): Int {
            val name2 = other.name
            return name.compareTo(name2, ignoreCase = true)
        }
    }

    /**
     * Entry to list adapter
     * @param context   context
     * @param id        text view resource id
     * @param items     items
     */
    class FileArrayAdapter(
        private val context: Context,
        @param:LayoutRes private val id: Int,
        private val items: List<Entry>
    ) : ArrayAdapter<Entry>(context, id, items) {

        override fun getItem(i: Int): Entry {
            return items[i]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                val inflater = checkNotNull(context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                view = inflater.inflate(this.id, null)
            }
            val entry = items[position]
            val image = view!!.findViewById<ImageView>(R.id.typeImage)
            val containerLabel = view.findViewById<TextView>(R.id.containerLabel)
            val containerName = view.findViewById<TextView>(R.id.containerName)

            if (entry.isNone) {
                image.setImageResource(R.drawable.filechooser_cancel)
            } else if (entry.isParent) {
                image.setImageResource(R.drawable.filechooser_back)
            } else if (entry.isFolder) {
                image.setImageResource(R.drawable.filechooser_folder)
            } else {
                var name = entry.name
                name = name.lowercase(Locale.getDefault())
                if (name.endsWith(".zip")) {
                    image.setImageResource(R.drawable.filechooser_zip)
                } else {
                    image.setImageResource(R.drawable.filechooser_file)
                }
            }
            if (containerLabel != null) {
                if (entry.isNone) {
                    containerLabel.setText(R.string.cancel)
                } else {
                    containerLabel.text = entry.name
                }
            }
            if (containerName != null) {
                if (entry.isNone) {
                    containerName.setText(R.string.none)
                } else {
                    containerName.text = entry.data
                }
            }
            return view
        }
    }

    /**
     * List view
     */
    private lateinit var listView: ListView

    /**
     * Current directory
     */
    private var currentDir: File? = null

    /**
     * Type
     */
    private var chooseDir = false

    /**
     * Adapter
     */
    private var adapter: FileArrayAdapter? = null

    /**
     * File filter
     */
    private var fileFilter: FileFilter? = null

    /**
     * Acceptable extensions
     */
    private var extensions: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        setContentView(R.layout.activity_choose_file)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }

        // list view
        listView = findViewById(android.R.id.list)

        // click listeners
        listView.onItemClickListener = this
        listView.setOnItemLongClickListener(this)

        // default
        this.currentDir = getExternalFilesDir(null)
        checkNotNull(this.currentDir)

        // extras
        val extras = intent.extras
        if (extras != null) {
            // initial
            var initialDirExtra = extras.getString(ARG_FILECHOOSER_INITIAL_DIR)
            if (initialDirExtra != null) {
                val uri = Uri.parse(initialDirExtra)
                if (uri != null && "file" == uri.scheme) {
                    val path = uri.path
                    if (path != null) {
                        initialDirExtra = path
                    }
                }
                this.currentDir = File(initialDirExtra)
            }

            // type
            this.chooseDir = extras.getBoolean(ARG_FILECHOOSER_CHOOSE_DIR, false)

            // extensions
            val extensionExtras = extras.getStringArray(ARG_FILECHOOSER_EXTENSION_FILTER)
            if (!extensionExtras.isNullOrEmpty()) {
                this.extensions = listOf(*extensionExtras)
                this.fileFilter = FileFilter { file: File ->
                    val name = file.name
                    val dot = name.lastIndexOf('.')
                    //
                    //
                    file.isDirectory || extensions == null || dot == -1 || extensions!!.contains(name.substring(dot + 1))
                }
            }
        }

        // initialize list
        fill(currentDir!!)

        // initialize list
        if (this.chooseDir) {
            Toast.makeText(this, R.string.howToSelect, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            checkNotNull(this.currentDir)
            if ( // !this.currentDir.getName().equals(ROOT) &&
                currentDir!!.parentFile != null) {
                currentDir = currentDir!!.parentFile
                fill(currentDir!!)
            }
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val entry = adapter!!.getItem(position)
        if (entry.isFolder || entry.isParent) {
            // if folder we move into it
            val path = entry.path
            currentDir = File(path)
            fill(currentDir!!)
        } else {
            // select
            if (!chooseDir || entry.isNone) {
                select(entry)
            }
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View, position: Int, id: Long): Boolean {
        val entry = adapter!!.getItem(position)
        if (this.chooseDir && (entry.isFolder || entry.isParent)) {
            // select
            select(entry)
            return true
        }
        return false
    }

    /**
     * Select and return entry
     *
     * @param entry entry
     */
    private fun select(entry: Entry) {
        // select
        // Toast.makeText(this, getResources().getText(R.string.selected) + " " + entry.getName(), Toast.LENGTH_SHORT).show();
        val resultIntent = Intent()
        if (!entry.isNone) {
            val fileUri = Uri.fromFile(File(entry.path))
            resultIntent.setDataAndType(fileUri, contentResolver.getType(fileUri))
        } else {
            resultIntent.setData(null)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    /**
     * Fill with entries from dir
     *
     * @param dirFile dir
     */
    private fun fill(dirFile: File) {
        val items = if (this.fileFilter != null) {
            dirFile.listFiles(this.fileFilter)
        } else {
            dirFile.listFiles()
        }

        this.title = getString(R.string.currentDir) + ": " + dirFile.name
        val dirs: MutableList<Entry> = ArrayList()
        val files: MutableList<Entry> = ArrayList()
        try {
            if (items != null) {
                for (item in items) {
                    if (item.isDirectory && !item.isHidden) {
                        dirs.add(Entry(item.name, getString(R.string.folder), item.absolutePath, isFolder = true, isParent = false))
                    } else {
                        if (!item.isHidden) {
                            files.add(Entry(item.name, getString(R.string.fileSize) + ": " + item.length(), item.absolutePath, isFolder = false, isParent = false))
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
            //
        }

        // sort
        dirs.sort()
        files.sort()
        dirs.addAll(files)
        dirs.add(Entry("", "", "", isFolder = false, isParent = false, isNone = true))

        // container
        run {
            if (dirFile.parentFile != null) {
                dirs.add(0, Entry(".. ${getString(R.string.parentDirectory)}", dirFile.absolutePath, dirFile.parent!!, isFolder = false, isParent = true))
            }
        }

        // adapter
        adapter = FileArrayAdapter(this@FileChooserActivity, R.layout.filechooser_entries_file, dirs)
        listView.adapter = adapter
    }

    companion object {

        // keys
        const val ARG_FILECHOOSER_EXTENSION_FILTER: String = "filechooser.extension_filter"

        const val ARG_FILECHOOSER_INITIAL_DIR: String = "filechooser.initial_dir"

        const val ARG_FILECHOOSER_CHOOSE_DIR: String = "filechooser.choose_dir"

        @JvmStatic
        @SuppressLint("CommitPrefEdits", "ApplySharedPref")
        fun setFolder(context: Context, key: String?, folder: String?) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val edit = prefs.edit()
            edit.putString(key, folder).commit()
        }

        @JvmStatic
        fun getFolder(context: Context, key: String?): File? {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val path = prefs.getString(key, null) ?: return null
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) {
                return null
            }
            return dir
        }
    }
}
