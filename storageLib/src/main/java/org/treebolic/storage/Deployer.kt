/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.storage

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import org.treebolic.storage.Storage.getTreebolicStorage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipInputStream

object Deployer {

    private const val TAG = "StorageUtils"

    // C O P Y   A S S E T

    /**
     * Copy asset file
     *
     * @param context  context
     * @param fileName file in assets
     * @return uri of copied file
     */
    @JvmStatic
    fun copyAssetFile(context: Context, fileName: String): Uri? {
        val assetManager = context.assets
        val dir = getTreebolicStorage(context)

        dir.mkdirs()
        val file = File(dir, fileName)
        if (copyAsset(assetManager, fileName, file.absolutePath)) {
            return Uri.fromFile(file)
        }
        return null
    }

    /**
     * Copy asset file to path
     *
     * @param assetManager asset manager
     * @param assetPath    asset path
     * @param toPath       destination path
     * @return true if successful
     */
    private fun copyAsset(assetManager: AssetManager, assetPath: String, toPath: String): Boolean {
        try {
            File(toPath).createNewFile()
        } catch (e: IOException) {
            return false
        }
        try {
            assetManager.open(assetPath).use { input ->
                FileOutputStream(toPath).use { output ->
                    copyFile(input, output)
                    return true
                }
            }
        } catch (ignored: Exception) {
            return false
        }
    }

    /**
     * Copy file to path
     *
     * @param fromPath source path
     * @param toPath   destination path
     * @return true if successful
     */
    fun copyFile(fromPath: String, toPath: String): Boolean {
        try {
            File(toPath).createNewFile()
        } catch (e: IOException) {
            return false
        }

        try {
            FileInputStream(fromPath).use { input ->
                FileOutputStream(toPath).use { output ->
                    copyFile(input, output)
                    return true
                }
            }
        } catch (ignored: Exception) {
            return false
        }
    }

    /**
     * Copy in stream to out stream
     *
     * @param in  in stream
     * @param out out stream
     * @throws IOException io exception
     */
    @Throws(IOException::class)
    fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while ((`in`.read(buffer).also { read = it }) != -1) {
            out.write(buffer, 0, read)
        }
    }

    // E X P A N D   A S S E T

    /**
     * Expand asset file
     *
     * @param context  context
     * @param fileName zip file in assets
     * @return uri of dest dir
     */
    @JvmStatic
    fun expandZipAssetFile(context: Context, fileName: String): Uri? {
        val assetManager = context.assets
        val dir = getTreebolicStorage(context)
        dir.mkdirs()
        if (expandZipAsset(assetManager, fileName, dir.absolutePath)) {
            return Uri.fromFile(dir)
        }
        return null
    }

    /**
     * Expand asset file to path
     *
     * @param assetManager asset manager
     * @param assetPath    asset path
     * @param toPath       destination path
     * @return true if successful
     */
    private fun expandZipAsset(assetManager: AssetManager, assetPath: String, toPath: String): Boolean {
        try {
            assetManager.open(assetPath).use { input ->
                expandZip(input, null, File(toPath))
                return true
            }
        } catch (ignored: Exception) {
            return false
        }
    }

    /**
     * Expand zip stream to dir
     *
     * @param in                zip file input stream
     * @param pathPrefixFilter0 path prefix filter on entries
     * @param destDir           destination dir
     * @return dest dir
     */
    @Throws(IOException::class)
    private fun expandZip(`in`: InputStream, pathPrefixFilter0: String?, destDir: File): File {
        // prefix
        var pathPrefixFilter = pathPrefixFilter0
        if (!pathPrefixFilter.isNullOrEmpty() && pathPrefixFilter[0] == File.separatorChar) {
            pathPrefixFilter = pathPrefixFilter.substring(1)
        }

        // create output directory if not exists
        destDir.mkdir()

        ZipInputStream(`in`).use { zipInput ->
            // get the zipped file list entry
            val buffer = ByteArray(1024)
            var entry = zipInput.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val entryName = entry.name
                    if (!entryName.endsWith("MANIFEST.MF")) {
                        if (pathPrefixFilter.isNullOrEmpty() || entryName.startsWith(pathPrefixFilter)) {
                            // flatten zip hierarchy
                            val outFile = File(destDir.toString() + File.separator + File(entryName).name)

                            // create all non exists folders else you will hit FileNotFoundException for compressed folder
                            val parent = outFile.parent
                            if (parent != null) {
                                val dir = File(parent)
                                val created = dir.mkdirs()
                                Log.d(TAG, dir.toString() + " created=" + created + " exists=" + dir.exists())
                            }

                            FileOutputStream(outFile).use { output ->
                                var len: Int
                                while ((zipInput.read(buffer).also { len = it }) > 0) {
                                    output.write(buffer, 0, len)
                                }
                            }
                        }
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }
        return destDir
    }

    /**
     * Cleanup data storage
     *
     * @param context context
     */
    @JvmStatic
    fun cleanup(context: Context) {
        val dir = getTreebolicStorage(context)
        val dirContent = dir.listFiles()
        if (dirContent != null) {
            for (file in dirContent) {
                file.delete()
            }
        }
    }
}
