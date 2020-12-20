plugins {
    `kotlin-dsl-base`
    `java-gradle-plugin`
    `maven-publish`
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
