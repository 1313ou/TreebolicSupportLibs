/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.download

import android.util.Log
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern
import java.util.zip.ZipInputStream

/**
 * Deployer
 *
 * @author Bernard Bou
 */
object Deploy {

    private const val TAG = "Deploy"

    /**
     * Copy stream to file
     *
     * @param in     input stream
     * @param toFile dest file
     * @throws IOException io exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(`in`: InputStream, toFile: File) {
        FileOutputStream(toFile).use { out ->
            val buffer = ByteArray(1024)
            var read: Int
            while ((`in`.read(buffer).also { read = it }) != -1) {
                out.write(buffer, 0, read)
            }
        }
    }

    /**
     * Expand archive stream to dir
     *
     * @param in      input stream
     * @param toDir   to directory
     * @param asTarGz is tar gz type
     * @throws IOException io exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun expand(`in`: InputStream, toDir: File, asTarGz: Boolean) {
        if (asTarGz) {
            extractTarGz(`in`, toDir, true, ".*", null)
            return
        }
        expandZip(`in`, toDir, true, ".*", "META-INF.*")
    }

    /**
     * Expand zip stream to dir
     *
     * @param in      zip file input stream
     * @param destDir destination dir
     * @param include include regexp filter
     * @param exclude exclude regexp filter
     * @return dest dir
     */
    @JvmStatic
    @Throws(IOException::class)
    fun expandZip(`in`: InputStream?, destDir: File, flat: Boolean, include: String?, exclude: String?): File {
        // patterns
        val includePattern = if (include == null) null else Pattern.compile(include)
        val excludePattern = if (exclude == null) null else Pattern.compile(exclude)

        // create output directory is not exists
        destDir.mkdir()

        // buffer
        val buffer = ByteArray(1024)

        ZipInputStream(`in`).use { zipIn ->

            // loop through entries
            var zipEntry = zipIn.nextEntry
            while (zipEntry != null) {
                var entryName = zipEntry.name
                Log.d(TAG, "Entry $entryName")

                // include
                if (includePattern != null) {
                    if (!includePattern.matcher(entryName).matches()) {
                        zipIn.closeEntry()
                        zipEntry = zipIn.nextEntry
                        continue
                    }
                }

                // exclude
                if (excludePattern != null) {
                    if (excludePattern.matcher(entryName).matches()) {
                        zipIn.closeEntry()
                        zipEntry = zipIn.nextEntry
                        continue
                    }
                }

                // expand this entry
                if (zipEntry.isDirectory) {
                    // create dir if we don't flatten
                    if (!flat) {
                        File(destDir, entryName).mkdirs()
                    }
                } else {
                    // flatten zip hierarchy
                    if (flat) {
                        val index = entryName.lastIndexOf('/')
                        if (index != -1) {
                            entryName = entryName.substring(index + 1)
                        }
                    }

                    // create destination
                    val destFile = File(destDir, entryName)
                    Log.d(TAG, "Unzip to " + destFile.canonicalPath)
                    destFile.createNewFile()

                    BufferedOutputStream(FileOutputStream(destFile)).use { bout ->
                        var len = zipIn.read(buffer)
                        while (len != -1) {
                            bout.write(buffer, 0, len)
                            len = zipIn.read(buffer)
                        }
                    }
                }
                zipEntry = zipIn.nextEntry
            }
            zipIn.closeEntry()
        }
        return destDir
    }

    /**
     * Extract tar.gz stream
     *
     * @param in      input stream
     * @param destDir destination dir
     * @param flat    flatten
     * @param include include regexp filter
     * @param exclude exclude regexp filter
     * @return dest dir
     * @throws IOException io exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun extractTarGz(`in`: InputStream, destDir: File, flat: Boolean, include: String?, exclude: String?): File {
        val includePattern = if (include == null) null else Pattern.compile(include)
        val excludePattern = if (exclude == null) null else Pattern.compile(exclude)

        // create output directory is not exists
        destDir.mkdirs()

        // buffer
        val buffer = ByteArray(1024)

        TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(`in`))).use { tarIn ->

            // loop through entries
            var tarEntry = tarIn.nextEntry
            while (tarEntry != null) {
                var entryName = tarEntry.name

                // include
                if (includePattern != null) {
                    if (!includePattern.matcher(entryName).matches()) {
                        tarEntry = tarIn.nextEntry
                        continue
                    }
                }

                // exclude
                if (excludePattern != null) {
                    if (excludePattern.matcher(entryName).matches()) {
                        tarEntry = tarIn.nextEntry
                        continue
                    }
                }

                // expand this entry
                if (tarEntry.isDirectory) {
                    // create dir if we don't flatten
                    if (!flat) {
                        File(destDir, entryName).mkdirs()
                    }
                } else {
                    // flatten tar hierarchy
                    if (flat) {
                        val index = entryName.lastIndexOf('/')
                        if (index != -1) {
                            entryName = entryName.substring(index + 1)
                        }
                    }

                    // create destination file with same name as entry
                    val destFile = File(destDir, entryName)
                    Log.d(TAG, "Untar to " + destFile.canonicalPath)
                    destFile.createNewFile()

                    BufferedOutputStream(FileOutputStream(destFile)).use { bout ->
                        var len = tarIn.read(buffer)
                        while (len != -1) {
                            bout.write(buffer, 0, len)
                            len = tarIn.read(buffer)
                        }
                    }
                }
                tarEntry = tarIn.nextEntry
            }
        }
        return destDir
    }
}
