/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Scanner

val buildTime: String = SimpleDateFormat("yyyy-MM-dd_HH:mm").format(Date())

fun getGitHash(): String {
    return try {
        val process = Runtime.getRuntime().exec("git rev-parse --short HEAD")
        val scanner = Scanner(process.inputStream).useDelimiter("\\A")
        if (scanner.hasNext()) scanner.next().trim() else "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

plugins {
    alias(libs.plugins.androidLibrary)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }
private val vCode by lazy { rootProject.extra["versionCode"] as Int }
private val vName by lazy { rootProject.extra["versionName"] as String }

android {

    namespace = "org.treebolic.common"

    compileSdk = vCompileSdk

    defaultConfig {
        minSdk = vMinSdk
        multiDexEnabled = true

        // BuildConfig fields
        buildConfigField("int", "VERSION_CODE", vCode.toString())
        buildConfigField("String", "VERSION_NAME", "\"$vName\"")
        buildConfigField("boolean", "DROP_DATA", "false")
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
        buildConfigField("String", "GIT_HASH", "\"${getGitHash()}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":theming"))
    implementation(project(":preferenceLib"))

    implementation(libs.appcompat)
    implementation(libs.preference.ktx)
    implementation(libs.material)

    implementation(libs.core.ktx)
    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))
    coreLibraryDesugaring(libs.desugar)
}