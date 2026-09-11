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

// Ownership boundaries, innermost first — see docs/adr/0009-module-ownership-boundaries.md.
include(":core")          // pure JVM: model, common, domain
include(":core:testing")  // pure JVM: fixtures, fixed clock, model builders

include(":local_db")      // Room: entities, DAOs, local data sources
include(":network")       // Supabase: client, DTOs, remote data sources
include(":service")       // repository impls, sync engine, preferences
include(":ui")            // Compose: design system, MVI base, route keys

include(":feature:accounts")
include(":feature:analytics")
include(":feature:auth")
include(":feature:budgets")
include(":feature:categories")
include(":feature:dashboard")
include(":feature:goals")
include(":feature:settings")
include(":feature:transactions")