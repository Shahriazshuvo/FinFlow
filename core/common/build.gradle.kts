plugins {
    alias(libs.plugins.finflow.jvm.library)
    alias(libs.plugins.finflow.jvm.hilt)
}

dependencies {
    api(project(":core:model"))
}
