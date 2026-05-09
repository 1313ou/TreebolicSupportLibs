/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

import java.text.SimpleDateFormat
import java.util.Date

val buildTime: String = SimpleDateFormat("yyyy-MM-dd_HH:mm").format(Date())

fun getGitHash(workingDir: File): String? {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()
        val result = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()
        if (exitCode == 0) result else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

plugins {
    alias(libs.plugins.androidLibrary)
}

android {

    namespace = "org.treebolic.common"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        multiDexEnabled = true

        // BuildConfig fields
        buildConfigField("int", "VERSION_CODE", libs.versions.versionCode.get())
        buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionCode.get()}\"")
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
        buildConfigField("String", "GIT_HASH", "\"${getGitHash(File("TreebolicSupportLibs"))}\"")
        // buildConfigField("boolean", "DROP_DATA", "false")
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