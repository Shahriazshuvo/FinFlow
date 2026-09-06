import com.android.build.api.dsl.LibraryExtension
import com.finflow.buildlogic.configureKotlinAndroid
import com.finflow.buildlogic.configureUnitTestDependencies
import com.finflow.buildlogic.libs
import com.finflow.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9 applies the Kotlin plugin itself (built-in Kotlin support), so
            // org.jetbrains.kotlin.android must NOT be applied here.
            pluginManager.apply(libs.pluginId("android-library"))

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
            }

            configureUnitTestDependencies()
        }
    }
}
