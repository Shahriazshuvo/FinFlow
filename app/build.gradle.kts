plugins {
    alias(libs.plugins.finflow.android.application)
    alias(libs.plugins.finflow.android.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.finflow.kotlin.serialization)
}

android {
    namespace = "com.finflow"

    defaultConfig {
        applicationId = "com.finflow"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))

    // Every feature module must be listed here, including ones with no screen yet — a module
    // absent from this list contributes no navigation graph, so its route is unreachable.
    // (The `@IntoSet Syncable` bindings all live in `core:data`'s `SyncableModule`; they have
    // not been feature-owned since `docs/adr/0006-centralized-data-layer.md`.)
    implementation(project(":feature:accounts"))
    implementation(project(":feature:analytics"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:budgets"))
    implementation(project(":feature:categories"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:goals"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:transactions"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
