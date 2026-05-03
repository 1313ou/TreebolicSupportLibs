package org.treebolic

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.SpannableStringBuilder

object Version {

    fun buildTime(value: String, repo: String): CharSequence = "$repo build time: $value\n"

    fun gitHash(value: String, repo: String) = "$repo git commit hash: $value\n"

    fun appVersion(context: Context): SpannableStringBuilder {
        val sb = SpannableStringBuilder()
        sb.apply {
            val packageName = context.applicationInfo.packageName
            append(packageName)
            append('\n')
            val pInfo: PackageInfo
            try {
                pInfo = context.packageManager.getPackageInfo(packageName, 0)
                val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else @Suppress("DEPRECATION") pInfo.versionCode.toLong()
                append("version: ")
                append(code.toString())
                append('\n')
            } catch (e: PackageManager.NameNotFoundException) {
                append("package info: ")
                append(e.message)
                append('\n')
            }
            append("api: ")
            append(Build.VERSION.SDK_INT.toString())
            append(' ')
            append(Build.VERSION.CODENAME)
            append('\n')
        }
        return sb
    }
}