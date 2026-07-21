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
        // إضافة مستودع هواوي الأساسي لجلب الحزم
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

rootProject.name = "LandscapeDesignApp"
include(":app")
