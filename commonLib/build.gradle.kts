/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

plugins {
    alias(libs.plugins.androidLibrary)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.treebolic.common"

    compileSdk = vCompileSdk

    defaultConfig {
        minSdk = vMinSdk
        multiDexEnabled = true
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
    implementation(project(":preferenceLib"))

    implementation(libs.appcompat)
    implementation(libs.preference.ktx)
    implementation(libs.material)

    implementation(libs.core.ktx)
    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))
    coreLibraryDesugaring(libs.desugar)
}