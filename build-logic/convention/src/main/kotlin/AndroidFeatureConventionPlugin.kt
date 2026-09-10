import com.finflow.buildlogic.lib
import com.finflow.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * Everything a `feature:*` module needs.
 *
 * Deliberately wires only the presentation-facing core modules — a feature can never see
 * `core:database`, `core:network` or `core:data`, so it cannot inject a repository
 * implementation, touch a DAO or import a DTO. Features talk to use cases in `core:domain`;
 * that is the whole surface (APP_SPEC.md §26).
 *
 * Data lives in `core:data` rather than in the feature that displays it because data is not
 * UI-scoped: accounts are read by three different screens. See
 * `docs/adr/0006-centralized-data-layer.md`.
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
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:common"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:navigation"))

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