plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

apply(from = "gradle/git-version.gradle.kts")

repositories {
    mavenLocal()
    apply(from = "gradle/maven-repo.gradle.kts")
    jcenter()
}

dependencies {
    implementation(platform(kotlin("bom", version = "1.3.61")))
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("gradle-plugin"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
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
