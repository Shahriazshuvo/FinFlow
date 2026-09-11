plugins {
    alias(libs.plugins.finflow.android.library.compose)
    alias(libs.plugins.finflow.android.hilt)
    alias(libs.plugins.finflow.kotlin.serialization)
}

android {
    namespace = "com.finflow.core.ui"
}

dependencies {
    implementation(project(":core"))

    // `api` where a feature writes against the type directly: it builds navigation graphs
    // and ViewModels, so these belong on its compile classpath, not just ours.
    api(libs.androidx.navigation.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
}