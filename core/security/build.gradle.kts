plugins {
    alias(libs.plugins.finflow.android.library)
    alias(libs.plugins.finflow.android.hilt)
}

android {
    namespace = "com.finflow.core.security"
}

dependencies {
    implementation(project(":core:common"))
}
