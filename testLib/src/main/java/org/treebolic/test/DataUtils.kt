/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.test

import android.os.Environment
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader

object DataUtils {

    private const val LIST_FILE = "tests/sqlunet.list"

    fun arrayToString(vararg a: Int): String {
        val sb = StringBuilder()
        sb.append('{')
        var first = true
        for (i in a) {
            if (first) {
                first = false
            } else {
                sb.append(',')
            }
            sb.append(i)
        }
        sb.append('}')
        return sb.toString()
    }

    // S A M P L E S

    val wordList: Array<String>?
        get() = readWordList()

    fun readWordList(): Array<String>? {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assets = context.resources.assets
        val list: MutableList<String> = ArrayList()
        try {
            assets.open(LIST_FILE).use { input ->
                InputStreamReader(input).use { reader ->
                    BufferedReader(reader).use { bufferedReader ->
                        var line: String
                        while ((bufferedReader.readLine().also { line = it }) != null) {
                            list.add(line.trim { it <= ' ' })
                        }
                        return list.toTypedArray<String>()
                    }
                }
            }
        } catch (e: IOException) {
            //Log.d("Read", "Error " + dataFile.getAbsolutePath(), e)
            Log.e("Read", "Error $LIST_FILE", e)
            return null
        }
    }

    private fun readWordListAlt(): Array<String>? {
        val list: MutableList<String> = ArrayList()
        val dataFile = File(Environment.getExternalStorageDirectory(), LIST_FILE)
        try {
            FileReader(dataFile).use { reader ->
                BufferedReader(reader).use { bufferedReader ->
                    var line: String
                    while ((bufferedReader.readLine().also { line = it }) != null) {
                        list.add(line.trim { it <= ' ' })
                    }
                    return list.toTypedArray<String>()
                }
            }
        } catch (e: IOException) {
            Log.d("Read", "Error " + dataFile.absolutePath, e)
        }
        return null
    }
}
