/*
 * Copyright 2023, Mohamed Ben Rejeb and the Compose Dnd project contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    id("module.publication")
    id("module.spotless")
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    js(IR) {
        browser()
    }

    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets.commonMain.dependencies {
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
    }

    sourceSets.named("desktopTest").dependencies {
        implementation(libs.compose.ui.test)
        implementation(compose.desktop.currentOs)
    }

    sourceSets.androidInstrumentedTest.dependencies {
        implementation(libs.compose.ui.test)
        implementation(libs.androidx.ui.test.junit4)
        implementation(libs.androidx.runner)
    }
}

android {
    namespace = "com.mohamedrejeb.compose.dnd"
    compileSdk = libs.versions.android.compileSdk
        .get()
        .toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk
            .get()
            .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        // Without this the test APK targets minSdk and Android 13+ covers it with a permission prompt.
        targetSdk = libs.versions.android.compileSdk
            .get()
            .toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
