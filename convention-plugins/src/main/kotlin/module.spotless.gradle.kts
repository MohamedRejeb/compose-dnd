plugins {
    id("com.diffplug.spotless")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint().setEditorConfigPath("${project.rootDir}/.editorconfig")
        licenseHeaderFile(File("${project.rootDir}/spotless/spotless.license.kt"))
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        ktlint().setEditorConfigPath("${project.rootDir}/.editorconfig")
        licenseHeaderFile(rootProject.file("spotless/spotless.license.kt"), "(^(?![\\/ ]\\*).*$)")
    }
    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml")
        licenseHeaderFile(rootProject.file("spotless/spotless.license.xml"), "(<[^!?])")
    }
}
