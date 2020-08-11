plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

val scriptUrl: String by extra

apply(from = "$scriptUrl/git-version.gradle.kts")

repositories {
    mavenLocal()
    apply(from = "$scriptUrl/maven-repo.gradle.kts")
    jcenter()
}

val kotlinVersion: String by extra
val semverVersion: String by extra

dependencies {
    implementation(gradleApi())
    implementation(kotlin("native-utils", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation("com.epam.drill:semver:$semverVersion")

    testImplementation(kotlin("test", kotlinVersion))
    testImplementation(kotlin("test-junit", kotlinVersion))
    testImplementation("org.eclipse.jgit:org.eclipse.jgit:5.5.0.201909110433-r")
    testImplementation(gradleTestKit())
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        systemProperty("project.version", project.version)
        dependsOn(publishToMavenLocal)
    }
}

gradlePlugin {
    plugins {
        create("version") {
            id = "$group.version"
            implementationClass = "com.epam.drill.gradle.VersionRetriever"

        }
        create("version.plugin") {
            id = "$group.version.plugin"
            implementationClass = "com.epam.drill.gradle.VersionRetriever"

        }
        create("cross-compilation") {
            id = "$group.cross-compilation"
            implementationClass = "com.epam.drill.gradle.CrossCompilation"
        }
    }
}
