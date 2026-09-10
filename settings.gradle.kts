pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FinFlow"

include(":app")

include(":core")
include(":core:testing")

include(":feature:accounts")
include(":feature:analytics")
include(":feature:auth")
include(":feature:budgets")
include(":feature:categories")
include(":feature:dashboard")
include(":feature:goals")
include(":feature:settings")
include(":feature:transactions")