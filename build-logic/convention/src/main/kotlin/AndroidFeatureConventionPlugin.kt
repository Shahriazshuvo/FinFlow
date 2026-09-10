import com.finflow.buildlogic.lib
import com.finflow.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * Everything a `feature:*` module needs.
 *
 * A feature holds presentation and navigation only: it talks to use cases in
 * `com.finflow.core.domain`, and that is the whole surface (APP_SPEC.md §3). Data lives in
 * `com.finflow.core.data` rather than in the feature that displays it because data is not
 * UI-scoped — accounts are read by three different screens. See
 * `docs/adr/0006-centralized-data-layer.md`.
 *
 * **That rule is no longer a compile error.** `:core` is one module, so a feature's classpath
 * carries the DAOs, DTOs and repository implementations along with the use cases; this plugin
 * has nothing to withhold. The boundary is enforced instead by check 3 of
 * `scripts/check-context.sh`, which fails on an import of `com.finflow.core.data`, `.database`,
 * `.network`, `.datastore`, `.security` or `.sync` from anywhere under `feature/`. Run it
 * before you finish. Background: `docs/adr/0008-single-core-module.md`.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("finflow.android.library.compose")
                apply("finflow.android.hilt")
                apply("finflow.kotlin.serialization")
            }

            dependencies {
                add("implementation", project(":core"))

                add("implementation", libs.lib("androidx-core-ktx"))
                add("implementation", libs.lib("androidx-lifecycle-runtime-ktx"))
                add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
                add("implementation", libs.lib("androidx-navigation-compose"))
                add("implementation", libs.lib("androidx-hilt-navigation-compose"))
                add("implementation", libs.lib("kotlinx-coroutines-android"))

                add("testImplementation", project(":core:testing"))

                add("androidTestImplementation", libs.lib("androidx-junit"))
                add("androidTestImplementation", libs.lib("androidx-espresso-core"))
            }
        }
    }
}