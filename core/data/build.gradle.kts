plugins {
    alias(libs.plugins.finflow.android.library)
    alias(libs.plugins.finflow.android.hilt)
}

android {
    namespace = "com.finflow.core.data"
}

dependencies {
    testImplementation(project(":core:testing"))

    api(project(":core:domain"))

    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))

    implementation(libs.kotlinx.coroutines.android)
}
