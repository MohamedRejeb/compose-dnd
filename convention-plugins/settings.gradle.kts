@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    versionCatalogs {
        val libs by registering {
            from(files("../gradle/libs.versions.toml"))
        }
    }

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "convention-plugins"