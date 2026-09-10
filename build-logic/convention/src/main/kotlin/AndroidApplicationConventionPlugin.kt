import com.android.build.api.dsl.ApplicationExtension
import com.finflow.buildlogic.configureFlavors
import com.finflow.buildlogic.configureKotlinAndroid
import com.finflow.buildlogic.configureUnitTestDependencies
import com.finflow.buildlogic.intVersion
import com.finflow.buildlogic.libs
import com.finflow.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9 applies the Kotlin plugin itself (built-in Kotlin support), so
            // org.jetbrains.kotlin.android must NOT be applied here.
            pluginManager.apply(libs.pluginId("android-application"))

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureFlavors(this)

                defaultConfig {
                    targetSdk = libs.intVersion("targetSdk")
                    versionCode = 1
                    versionName = "1.0"
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildFeatures {
                    buildConfig = true
                }
            }

            configureUnitTestDependencies()
        }
    }
}