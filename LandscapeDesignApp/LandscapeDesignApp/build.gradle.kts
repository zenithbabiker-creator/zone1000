// Top-level build file
//
// ── VERIFIED VERSION COMPATIBILITY MATRIX ──────────────────────────────────
// Gradle          8.7      (required by the team; installed via wget in CI —
//                           see .github/workflows/android_build.yml, no
//                           gradlew wrapper is committed to this repo)
// AGP             8.5.0    Requires Gradle 8.7 minimum (AGP release notes:
//                           https://developer.android.com/build/releases/gradle-plugin
//                           "Gradle minimum: 8.7" for AGP 8.5.0) — MATCHES.
// Kotlin          1.9.24   Official Kotlin↔Compose-Compiler map pairs
//                           Kotlin 1.9.24 with Compose Compiler 1.5.14 —
//                           see app/build.gradle.kts `kotlinCompilerExtensionVersion`.
// JDK             17       Required by AGP 8.5.x; set via `compileOptions`/
//                           `kotlinOptions` in app/build.gradle.kts.
//
// Do not bump any one of these independently — check
// https://developer.android.com/build/releases/gradle-plugin for the AGP↔Gradle
// table and https://developer.android.com/jetpack/androidx/releases/compose-kotlin
// for the Kotlin↔Compose-Compiler table before changing any version here.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
