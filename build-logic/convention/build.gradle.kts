plugins {
    `kotlin-dsl`
}

group = "com.finflow.buildlogic"

// Gradle 9.1 embeds Kotlin 2.2, so this source set compiles at that language
// level even though the app modules compile with Kotlin 2.3.x.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.javaToolchain.get().toInt())
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "finflow.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "finflow.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "finflow.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "finflow.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "finflow.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "finflow.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("jvmLibrary") {
            id = "finflow.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("jvmHilt") {
            id = "finflow.jvm.hilt"
            implementationClass = "JvmHiltConventionPlugin"
        }
        register("kotlinSerialization") {
            id = "finflow.kotlin.serialization"
            implementationClass = "KotlinSerializationConventionPlugin"
        }
    }
}