/**
 * build.gradle.kts (project root)
 *
 * Top-level build configuration for Limitless Companion Android project.
 *
 * This file configures:
 * - Plugin versions
 * - Project-wide repositories
 * - Common build settings
 *
 * TODO(milestone-2): Add build scanning and caching configuration
 * TODO(milestone-3): Add CI/CD specific configurations
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.android.library") version "8.2.0" apply false
    kotlin("kapt") version "1.9.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}