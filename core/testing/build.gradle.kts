plugins {
    alias(libs.plugins.finflow.jvm.library)
}

dependencies {
    api(project(":core"))

    // `api`, not `implementation`: a module that depends on core:testing is a test source
    // set, and it needs these on its own compile classpath.
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.mockk)
}