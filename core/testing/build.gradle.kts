plugins {
    alias(libs.plugins.finflow.android.library)
}

android {
    namespace = "com.finflow.core.testing"
}

dependencies {
    // Android library rather than JVM: the domain models it builds live in `:core`, which is
    // an Android module, and a JVM module cannot resolve an AAR onto its compile classpath.
    api(project(":core"))

    // `api`, not `implementation`: a module that depends on core:testing is a test source
    // set, and it needs these on its own compile classpath.
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.mockk)
}