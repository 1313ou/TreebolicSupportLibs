/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.sqlunet.test"

    compileSdk = vCompileSdk

    defaultConfig {
        minSdk = vMinSdk
        multiDexEnabled = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.test:core:1.6.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.test.espresso:espresso-contrib:3.6.1")

    implementation(libs.core.ktx)
    coreLibraryDesugaring(libs.desugar)
}
