import java.util.Properties

plugins {
    alias(libs.plugins.finflow.android.library.compose)
    alias(libs.plugins.finflow.android.hilt)
    alias(libs.plugins.finflow.android.room)
    alias(libs.plugins.finflow.kotlin.serialization)
}

// Supabase credentials come from local.properties (git-ignored) or the environment.
// Empty defaults keep a fresh clone buildable without any credentials.
//
// Only the anon/publishable key is ever read here. A service-role key must never reach the
// client — RLS is the security boundary (APP_SPEC.md §14), and no variant ships a secret (§19).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun secret(key: String): String =
    localProperties.getProperty(key) ?: System.getenv(key) ?: ""

/**
 * Per-environment credential with a fallback to the unsuffixed key, so a `local.properties`
 * holding only `SUPABASE_URL` still builds every flavor. Point a flavor at its own project by
 * adding `SUPABASE_URL_QA` / `SUPABASE_ANON_KEY_QA` and so on.
 */
fun envSecret(key: String, flavor: String): String =
    secret("${key}_${flavor.uppercase()}").ifBlank { secret(key) }

android {
    namespace = "com.finflow.core"

    buildFeatures {
        buildConfig = true
    }

    // The `environment` dimension and its three flavors come from
    // AndroidLibraryConventionPlugin; this only attaches credentials to each.
    productFlavors.configureEach {
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${envSecret("SUPABASE_URL", name)}\"",
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${envSecret("SUPABASE_ANON_KEY", name)}\"",
        )
    }
}

dependencies {
    // `api` where a consumer writes against the type directly: features build navigation
    // graphs and ViewModels, so these have to be on their compile classpath, not just ours.
    api(libs.androidx.navigation.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(project(":core:testing"))
}