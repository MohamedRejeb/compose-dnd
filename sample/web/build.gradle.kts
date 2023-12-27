plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets.jsMain.dependencies {
        implementation(projects.sample.common)

        implementation(compose.foundation)
    }
}

compose.experimental {
    web.application {}
}