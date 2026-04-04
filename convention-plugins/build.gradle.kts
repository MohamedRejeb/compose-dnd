plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.vanniktech.maven.publish)
    implementation(libs.spotless.gradle)
    // hack to access version catalogue https://github.com/gradle/gradle/issues/15383
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
