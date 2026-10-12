plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.spotless) apply false
}

val composeDndVersion = System.getenv("VERSION") ?: "1.0.0-alpha02"

allprojects {
    group = "com.mohamedrejeb.dnd"
    version = composeDndVersion
}
