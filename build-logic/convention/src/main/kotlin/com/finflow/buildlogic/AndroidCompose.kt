package com.finflow.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    commonExtension.buildFeatures.compose = true

    dependencies {
        val bom = libs.lib("androidx-compose-bom")
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libs.bundle("compose"))
        add("implementation", libs.lib("androidx-compose-material-icons-extended"))
        add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))

        add("debugImplementation", libs.bundle("compose-debug"))
        add("androidTestImplementation", libs.lib("androidx-compose-ui-test-junit4"))
    }
}