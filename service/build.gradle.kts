plugins {
    alias(libs.plugins.finflow.android.library)
    alias(libs.plugins.finflow.android.hilt)
    alias(libs.plugins.finflow.kotlin.serialization)
}

android {
    namespace = "com.finflow.core.service"
}

dependencies {
    implementation(project(":core"))

    // The only module that sees both sides. Repositories orchestrate them; neither knows
    // the other exists.
    implementation(project(":local_db"))
    implementation(project(":network"))

    implementation(libs.androidx.datastore.preferences)

    // WorkManager lives here and nowhere else — the write path only ever sees SyncTrigger.
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(project(":core:testing"))
}