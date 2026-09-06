// AGP 9 has built-in Kotlin support and carries a runtime dependency on KGP 2.2.10. It will
// silently pin that version unless a newer Kotlin Gradle plugin is on the root buildscript
// classpath — which is what this block is for. Without it, `kotlin = "2.3.21"` in the version
// catalog would only affect the Compose compiler plugin, leaving the Compose compiler and the
// Kotlin compiler on mismatched versions.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:${libs.versions.ksp.get()}")
    }
}

// Declared here so every project shares one buildscript classpath. Configuration lives in the
// build-logic convention plugins.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
}