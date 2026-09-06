import com.finflow.buildlogic.lib
import com.finflow.buildlogic.libs
import com.finflow.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Hilt for pure-Kotlin modules (`core:common`, `core:domain`). Uses `hilt-core`, which
 * carries the annotations and `SingletonComponent` without pulling in anything Android,
 * so the domain layer stays framework-free.
 */
class JvmHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("ksp"))

            dependencies {
                add("implementation", libs.lib("hilt-core"))
                add("ksp", libs.lib("hilt-compiler"))
                add("ksp", libs.lib("kotlin-metadata-jvm"))
            }
        }
    }
}