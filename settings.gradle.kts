/*
 * Copyright (c) 2022. Bernard Bou <1313ou@gmail.com>
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenLocal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

include(":commonLib", ":guideLib", ":fileChooserLib", ":downloadLib", ":preferenceLib", ":wheelLib", ":searchLib", ":colorLib", ":storageLib", ":testLib")
