package com.finflow.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Shared Android + Kotlin configuration for every Android module.
 *
 * Note: in AGP 9 [CommonExtension] is no longer generic, and the `xxx(action)` DSL
 * helpers live on [com.android.build.api.dsl.ApplicationExtension] /
 * [com.android.build.api.dsl.LibraryExtension] rather than on [CommonExtension] — so the
 * shared properties are configured through `apply { }` on the exposed values.
 */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    val javaVersion = JavaVersion.toVersion(libs.version("javaToolchain"))

    commonExtension.compileSdk = libs.intVersion("compileSdk")

    commonExtension.defaultConfig.apply {
        minSdk = libs.intVersion("minSdk")
    }

    commonExtension.compileOptions.apply {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        // java.time is only available natively from API 26; FinFlow targets minSdk 24
        // and uses java.time throughout, so desugaring is mandatory.
        isCoreLibraryDesugaringEnabled = true
    }

    configureKotlinJvmTarget()

    dependencies {
        add("coreLibraryDesugaring", libs.lib("android-desugar-jdk-libs"))
    }
}

/** Shared configuration for pure-Kotlin (non-Android) modules. */
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(libs.intVersion("javaToolchain")))
    }
    configureKotlinJvmTarget()
}

private fun Project.configureKotlinJvmTarget() {
    val target = JvmTarget.fromTarget(libs.version("javaToolchain"))
    tasks.withType(KotlinCompile::class.java).configureEach {
        compilerOptions {
            jvmTarget.set(target)
        }
    }
}

/** Unit-test dependencies every module gets for free. */
internal fun Project.configureUnitTestDependencies() {
    dependencies {
        add("testImplementation", libs.bundle("unit-test"))
    }
}