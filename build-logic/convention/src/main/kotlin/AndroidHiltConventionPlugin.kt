import com.finflow.buildlogic.lib
import com.finflow.buildlogic.libs
import com.finflow.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.pluginId("ksp"))
                apply(libs.pluginId("hilt"))
            }

            dependencies {
                add("implementation", libs.lib("hilt-android"))
                add("ksp", libs.lib("hilt-compiler"))
                // Hilt pins an older kotlin-bom internally; align its metadata reader
                // with the compiler that actually produced our classes.
                add("ksp", libs.lib("kotlin-metadata-jvm"))
                add("testImplementation", libs.lib("hilt-android-testing"))
                add("kspTest", libs.lib("hilt-compiler"))
            }
        }
    }
}