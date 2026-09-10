plugins {
    alias(libs.plugins.finflow.android.library)
    alias(libs.plugins.finflow.kotlin.serialization)
}

android {
    namespace = "com.finflow.core.navigation"
}

dependencies {
    api(libs.androidx.navigation.compose)
}
