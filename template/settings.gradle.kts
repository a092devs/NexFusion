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
        maven(url = "https://api.xposed.info")
        // The NexFusion patcher API.
        maven(url = providers.gradleProperty("nexfusionMaven").get())
    }
}

rootProject.name = "my-nexfusion-patches"
include(":bundle")
