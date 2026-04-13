rootProject.name = "BillionSeconds"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Convention plugins
includeBuild("build-logic")

// Application
include(":app")
project(":app").projectDir = file("composeApp")

// Core modules
include(":core:core-domain")
include(":core:core-data")
include(":core:core-ui")
include(":core:core-navigation")

// Feature modules
include(":feature:feature-onboarding")
include(":feature:feature-countdown")
include(":feature:feature-lifestats")
include(":feature:feature-milestones")
include(":feature:feature-family")
include(":feature:feature-timecapsule")
include(":feature:feature-event")
include(":feature:feature-profile")