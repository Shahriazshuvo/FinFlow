plugins {
    alias(libs.plugins.finflow.android.library)
    alias(libs.plugins.finflow.android.hilt)
    alias(libs.plugins.finflow.android.room)
}

android {
    // Matches the package root, so no import in this module had to change when it moved
    // out of `:core`.
    namespace = "com.finflow.core.database"
}

dependencies {
    implementation(project(":core"))

    implementation(libs.kotlinx.coroutines.android)
}