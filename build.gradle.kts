plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    id("root.publication")
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.spotless) apply false
}
