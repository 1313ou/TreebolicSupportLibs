/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

plugins {
    alias(libs.plugins.androidLibrary)
}

android {

    namespace = "org.treebolic.download"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
    implementation(project(":commonLib"))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.compress)

    implementation(libs.core.ktx)
    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))
    coreLibraryDesugaring(libs.desugar)
}
