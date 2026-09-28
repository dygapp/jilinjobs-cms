pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://maven.aliyun.com/repository/public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "jilinjobs-cms-backend"

include(":modules:cms-core")
include(":apps:cms-server")
include(":apps:content-migration")
