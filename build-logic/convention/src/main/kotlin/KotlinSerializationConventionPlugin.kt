import com.finflow.buildlogic.lib
import com.finflow.buildlogic.libs
import com.finflow.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KotlinSerializationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-serialization"))

            dependencies {
                add("implementation", libs.lib("kotlinx-serialization-json"))
            }
        }
    }
}