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
        maven { url 'https://developer.huawei.com/repo/' } // مستودع هواوي الأساسي لـ AR Engine و HMS
    }
}

rootProject.name = "LandscapeDesignApp"
include(":app")
