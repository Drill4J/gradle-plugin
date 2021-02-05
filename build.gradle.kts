import java.net.*

plugins {
    `kotlin-dsl-base`
    `java-gradle-plugin`
    `maven-publish`
    id("com.github.hierynomus.license")
}

val scriptUrl: String by extra

val kotlinVersion: String by extra

apply(from = "$scriptUrl/git-version.gradle.kts")

repositories {
    mavenLocal()
    apply(from = "$scriptUrl/maven-repo.gradle.kts")
    jcenter()
}

dependencies {
    compileOnly(kotlin("gradle-plugin", kotlinVersion))
}

gradlePlugin {
    plugins {
        create("cross-compilation") {
            id = "$group.cross-compilation"
            implementationClass = "com.epam.drill.gradle.CrossCompilation"
        }
    }
}

val licenseFormatSettings by tasks.registering(com.hierynomus.gradle.license.tasks.LicenseFormat::class) {
    source = fileTree(project.projectDir).also {
        include("**/*.kt", "**/*.java", "**/*.groovy")
        exclude("**/.idea")
    }.asFileTree
}

license {
    headerURI = URI("https://raw.githubusercontent.com/Drill4J/drill4j/develop/COPYRIGHT")
}

tasks["licenseFormat"].dependsOn(licenseFormatSettings)
