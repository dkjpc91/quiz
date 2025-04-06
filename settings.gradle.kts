import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven ( url  = "https://repository.liferay.com/nexus/content/repositories/public/")
    }
}

rootProject.name = "LearnSource"
include(":app")
 