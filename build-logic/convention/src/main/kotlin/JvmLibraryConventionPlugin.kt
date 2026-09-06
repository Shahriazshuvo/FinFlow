import com.finflow.buildlogic.configureKotlinJvm
import com.finflow.buildlogic.configureUnitTestDependencies
import com.finflow.buildlogic.lib
import com.finflow.buildlogic.libs
import com.finflow.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-jvm"))

            configureKotlinJvm()

            dependencies {
                add("implementation", libs.lib("kotlinx-coroutines-core"))
            }

            configureUnitTestDependencies()
        }
    }
}