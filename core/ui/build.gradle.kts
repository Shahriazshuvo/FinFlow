plugins {
    alias(libs.plugins.finflow.android.library.compose)
    alias(libs.plugins.finflow.android.hilt)
}

android {
    namespace = "com.finflow.core.ui"
}

dependencies {
    api(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
}
