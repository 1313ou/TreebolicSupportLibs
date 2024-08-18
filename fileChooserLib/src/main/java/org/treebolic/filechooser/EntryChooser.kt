/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.filechooser

import android.content.Context
import android.content.DialogInterface
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.io.IOException
import java.util.function.Consumer
import java.util.zip.ZipFile

/**
 * Zip entry chooser
 *
 * @property context  context
 * @property list     list of entries
 * @property listener click listener
 *
 * @author Bernard Bou
 */
class EntryChooser(
    private val context: Context,
    private val list: List<String>,
    private val listener: DialogInterface.OnClickListener
) {

    /**
     * Show dialog
     */
    fun show() {
        val adapter = ArrayAdapter(context, R.layout.filechooser_entries_zip, list)

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.chooseEntry)
        builder.setAdapter(adapter, listener)
        val alert = builder.create()
        alert.show()
    }

    companion object {

        /**
         * Get archive entries
         *
         * @param archive        zip archive
         * @param negativeFilter negative filter
         * @param positiveFilter positive filter
         * @return list of entries
         * @throws IOException io exception
         */
        @Throws(IOException::class)
        private fun getZipEntries(archive: File, @Suppress("SameParameterValue") negativeFilter: String?, @Suppress("SameParameterValue") positiveFilter: String?): List<String> {
            ZipFile(archive).use { zipFile ->
                val result: MutableList<String> = ArrayList()
                val zipEntries = zipFile.entries()
                while (zipEntries.hasMoreElements()) {
                    val zipEntry = zipEntries.nextElement()
                    val name = zipEntry.name
                    if (negativeFilter != null && name.matches(negativeFilter.toRegex())) {
                        continue
                    }
                    if (positiveFilter == null || name.matches(positiveFilter.toRegex())) {
                        result.add(name)
                    }
                }
                return result
            }
        }

        /**
         * Choose entry convenience method
         *
         * @param context  context
         * @param archive  zip archive
         * @param consumer selection consumer
         * @throws IOException io exception
         */
        @JvmStatic
        @Throws(IOException::class)
        fun choose(context: Context, archive: File, consumer: Consumer<String>) {
            val list = getZipEntries(archive, "(.*gif|.*png|.*jpg|.*properties|.*MF|.*/)", ".*")
            val listener = DialogInterface.OnClickListener { _: DialogInterface?, which: Int ->
                // The 'which' argument contains the index position of the selected item
                consumer.accept(list[which])
            }
            EntryChooser(context, list, listener).show()
        }
    }
}
