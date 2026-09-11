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
 * Wires exactly two modules: `:core` for use cases and domain contracts, `:ui` for the design
 * system, the MVI base classes and the route keys. **`:service`, `:local_db` and `:network` are
 * deliberately absent**, so a feature cannot inject a repository implementation, touch a DAO or
 * import a DTO — the classpath makes it a compile error, and check 3 of
 * `scripts/check-context.sh` catches it earlier with a better message.
 *
 * Repository *implementations* are bound by Hilt at the `:app` composition root, which is why a
 * feature never needs `:service` on its own classpath.
 * Background: `docs/adr/0009-module-ownership-boundaries.md`.
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
                add("implementation", project(":ui"))

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