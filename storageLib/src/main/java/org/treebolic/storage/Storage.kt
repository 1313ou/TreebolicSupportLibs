/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.storage

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import java.io.File

object Storage {

    /**
     * Treebolic storage preference name
     */
    private const val PREF_TREEBOLIC_STORAGE: String = "pref_storage"

    /**
     * Cached treebolic storage
     */
    private var treebolicStorage: File? = null

    /**
     * Get treebolic storage
     *
     * @return treebolic storage directory
     */
    @JvmStatic
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun getTreebolicStorage(context: Context): File {

        // if cached return cache
        if (treebolicStorage != null) {
            return treebolicStorage!!
        }

        // test if already discovered in this context
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val pref = sharedPref.getString(PREF_TREEBOLIC_STORAGE, null)
        if (pref != null) {
            treebolicStorage = File(pref)
            if (qualifies(treebolicStorage)) {
                return treebolicStorage!!
            }
        }

        // discover
        treebolicStorage = discoverTreebolicStorage(context)
        checkNotNull(treebolicStorage)
        val path = treebolicStorage!!.absolutePath

        // flag as discovered
        sharedPref.edit().putString(PREF_TREEBOLIC_STORAGE, path).commit()

        return treebolicStorage!!
    }

    /**
     * Discover Treebolic storage
     *
     * @param context context
     * @return Treebolic storage
     */
    private fun discoverTreebolicStorage(context: Context): File? {

        // application-specific secondary or primary storage
        try {
            val dirs = ContextCompat.getExternalFilesDirs(context, null)

            // preferably secondary storage
            for (i in 1 until dirs.size) {
                if (qualifies(dirs[i])) {
                    return dirs[i]
                }
            }
            // fall back on primary storage
            if (qualifies(dirs[0])) {
                return dirs[0]
            }
        } catch (ignored: Throwable) {
            //
        }

        // internal private storage
        return context.filesDir
    }

    /**
     * Whether the dir qualifies as treebolic storage
     *
     * @param dir candidate dir
     * @return true if it qualifies
     */
    private fun qualifies(dir: File?): Boolean {
        if (dir == null) {
            return false
        }

        // either mkdirs() creates dir or it is already a dir
        return dir.mkdirs() || dir.isDirectory // || dir.canWrite())
    }
}
