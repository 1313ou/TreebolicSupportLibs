package org.treebolic

import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window

object Dump {

    /**
     * Dump view hierarchy
     * e.g. dump(dialog.window, res)
     */
    fun dump(w: Window?, resources: Resources) {
        w?.decorView?.let { root ->
            fun dump(v: View, indent: Int = 0) {
                val indentStr = " ".repeat(indent)
                val name = v.javaClass.simpleName
                val bg = v.background?.javaClass?.simpleName
                val id = runCatching { resources.getResourceEntryName(v.id) }.getOrDefault("none")
                Log.d("DUMP", "${indentStr}$name id=$id bg=$bg")

                if (v is ViewGroup) for (i in 0 until v.childCount) dump(v.getChildAt(i), indent + 2)
            }
            // post to ensure hierarchy is fully laid out
            root.post { dump(root) }
        }
    }
}