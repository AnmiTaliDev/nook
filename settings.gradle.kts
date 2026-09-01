pluginManagement {
    repositories {
        google()
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

rootProject.name = "Nook"

include(":app")
include(":core:model")
include(":core:common")
include(":core:navigation")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":feature:browse")
