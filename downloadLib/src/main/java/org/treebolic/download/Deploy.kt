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
     * @param input     input stream
     * @param toFile dest file
     * @throws IOException io exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: InputStream, toFile: File) {
        FileOutputStream(toFile).use { output ->
            val buffer = ByteArray(1024)
            var read: Int
            while ((input.read(buffer).also { read = it }) != -1) {
                output.write(buffer, 0, read)
            }
        }
    }

    /**
     * Expand archive stream to dir
     *
     * @param input input stream
     * @param toDir to directory
     * @param asTarGz is tar gz type
     * @throws IOException io exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun expand(input: InputStream, toDir: File, asTarGz: Boolean) {
        if (asTarGz) {
            extractTarGz(input, toDir, true, ".*", null)
            return
        }
        expandZip(input, toDir, true, ".*", "META-INF.*")
    }

    /**
     * Expand zip stream to dir
     *
     * @param input zip file input stream
     * @param destDir destination dir
     * @param include include regexp filter
     * @param exclude exclude regexp filter
     * @return dest dir
     */
    @JvmStatic
    @Throws(IOException::class)
    fun expandZip(input: InputStream?, destDir: File, flat: Boolean, include: String?, exclude: String?): File {
        // patterns
        val includePattern = if (include == null) null else Pattern.compile(include)
        val excludePattern = if (exclude == null) null else Pattern.compile(exclude)

        // create output directory is not exists
        destDir.mkdir()

        // buffer
        val buffer = ByteArray(1024)

        ZipInputStream(input).use { zipInput ->

            // loop through entries
            var zipEntry = zipInput.nextEntry
            while (zipEntry != null) {
                var entryName = zipEntry.name
                Log.d(TAG, "Entry $entryName")

                // include
                if (includePattern != null) {
                    if (!includePattern.matcher(entryName).matches()) {
                        zipInput.closeEntry()
                        zipEntry = zipInput.nextEntry
                        continue
                    }
                }

                // exclude
                if (excludePattern != null) {
                    if (excludePattern.matcher(entryName).matches()) {
                        zipInput.closeEntry()
                        zipEntry = zipInput.nextEntry
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

                    BufferedOutputStream(FileOutputStream(destFile)).use { output ->
                        var len = zipInput.read(buffer)
                        while (len != -1) {
                            output.write(buffer, 0, len)
                            len = zipInput.read(buffer)
                        }
                    }
                }
                zipEntry = zipInput.nextEntry
            }
            zipInput.closeEntry()
        }
        return destDir
    }

    /**
     * Extract tar.gz stream
     *
     * @param input input stream
     * @param destDir destination dir
     * @param flat flatten
     * @param include include regexp filter
     * @param exclude exclude regexp filter
     * @return dest dir
     * @throws IOException io exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun extractTarGz(input: InputStream, destDir: File, flat: Boolean, include: String?, exclude: String?): File {
        val includePattern = if (include == null) null else Pattern.compile(include)
        val excludePattern = if (exclude == null) null else Pattern.compile(exclude)

        // create output directory is not exists
        destDir.mkdirs()

        // buffer
        val buffer = ByteArray(1024)

        TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(input))).use { tarInput ->

            // loop through entries
            var tarEntry = tarInput.nextEntry
            while (tarEntry != null) {
                var entryName = tarEntry.name

                // include
                if (includePattern != null) {
                    if (!includePattern.matcher(entryName).matches()) {
                        tarEntry = tarInput.nextEntry
                        continue
                    }
                }

                // exclude
                if (excludePattern != null) {
                    if (excludePattern.matcher(entryName).matches()) {
                        tarEntry = tarInput.nextEntry
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

                    BufferedOutputStream(FileOutputStream(destFile)).use { output ->
                        var len = tarInput.read(buffer)
                        while (len != -1) {
                            output.write(buffer, 0, len)
                            len = tarInput.read(buffer)
                        }
                    }
                }
                tarEntry = tarInput.nextEntry
            }
        }
        return destDir
    }
}
