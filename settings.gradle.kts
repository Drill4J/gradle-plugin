rootProject.name = "gradle-plugin"

pluginManagement {
    val licenseVersion: String by extra
    plugins {
        id("com.github.hierynomus.license") version licenseVersion
    }
}
