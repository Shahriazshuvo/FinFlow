package com.finflow.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

/** The dimension every module shares. One dimension is enough; environments are exclusive. */
const val FLAVOR_DIMENSION: String = "environment"

/**
 * Build environments (`APP_SPEC.md` §30).
 *
 * `dev` and `qa` install alongside `prod` via an application id suffix, so a tester can hold all
 * three on one device. Only `prod` keeps the plain application id.
 */
enum class FinFlowFlavor(val applicationIdSuffix: String?) {
    dev(".dev"),
    qa(".qa"),
    prod(null),
}

/**
 * Declares the flavors on the application module, where the id suffix and the logging switch
 * live.
 *
 * Note: in AGP 9 [com.android.build.api.dsl.CommonExtension] is not generic and the product
 * flavor types differ per extension ([com.android.build.api.dsl.ApplicationProductFlavor] carries
 * `applicationIdSuffix`, the library one does not), so this is two typed overloads rather than one
 * generic function.
 */
internal fun Project.configureFlavors(extension: ApplicationExtension) {
    extension.flavorDimensions += FLAVOR_DIMENSION
    FinFlowFlavor.entries.forEach { flavor ->
        extension.productFlavors.create(flavor.name) {
            dimension = FLAVOR_DIMENSION
            flavor.applicationIdSuffix?.let { applicationIdSuffix = it }
            // Verbose logging is a non-production affordance, not a debug-build one: a release
            // build of `qa` still wants it, and a debuggable `prod` build must not have it.
            buildConfigField(
                "boolean",
                "DEBUG_LOGGING",
                (flavor != FinFlowFlavor.prod).toString(),
            )
        }
    }
}

/**
 * Declares the same dimension on every Android library module.
 *
 * This is not optional bookkeeping: if `:app` has a dimension its dependencies lack, variant
 * resolution fails with "unable to find a matching variant". Pure-JVM modules
 * (`core:model`, `core:common`, `core:domain`) are exempt — they have no Android variants.
 */
internal fun Project.configureFlavors(extension: LibraryExtension) {
    extension.flavorDimensions += FLAVOR_DIMENSION
    FinFlowFlavor.entries.forEach { flavor ->
        extension.productFlavors.create(flavor.name) {
            dimension = FLAVOR_DIMENSION
        }
    }
}