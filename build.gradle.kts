buildscript {
    dependencies {
        classpath(libs.build.gradle)
        classpath(libs.google.services)
        classpath(libs.crashlytics.gradle)
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidLibrary) apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}

subprojects {
    apply(plugin = "com.google.devtools.ksp")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}