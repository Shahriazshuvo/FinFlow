package com.finflow.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * The `libs` accessor generated for build scripts is not available inside convention
 * plugins, so the catalog has to be looked up through [VersionCatalogsExtension].
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).orElseThrow { IllegalStateException("Missing version alias '$alias'") }
        .requiredVersion

internal fun VersionCatalog.intVersion(alias: String): Int = version(alias).toInt()

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalStateException("Missing library alias '$alias'") }

internal fun VersionCatalog.bundle(alias: String) =
    findBundle(alias).orElseThrow { IllegalStateException("Missing bundle alias '$alias'") }

internal fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).orElseThrow { IllegalStateException("Missing plugin alias '$alias'") }
        .get().pluginId

internal fun DependencyHandler.implementation(dependency: Any) = add("implementation", dependency)

internal fun DependencyHandler.api(dependency: Any) = add("api", dependency)

internal fun DependencyHandler.ksp(dependency: Any) = add("ksp", dependency)

internal fun DependencyHandler.testImplementation(dependency: Any) =
    add("testImplementation", dependency)

internal fun DependencyHandler.androidTestImplementation(dependency: Any) =
    add("androidTestImplementation", dependency)

internal fun DependencyHandler.debugImplementation(dependency: Any) =
    add("debugImplementation", dependency)