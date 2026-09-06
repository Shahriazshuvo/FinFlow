import androidx.room.gradle.RoomExtension
import com.finflow.buildlogic.lib
import com.finflow.buildlogic.libs
import com.finflow.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.pluginId("ksp"))
                apply(libs.pluginId("room"))
            }

            extensions.configure<RoomExtension> {
                // Schemas are checked in so migrations can be diffed in review.
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                add("implementation", libs.lib("room-runtime"))
                add("implementation", libs.lib("room-ktx"))
                add("ksp", libs.lib("room-compiler"))
                add("testImplementation", libs.lib("room-testing"))
            }
        }
    }
}