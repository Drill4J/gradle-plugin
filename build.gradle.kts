plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    mavenCentral()
}

val kotlinVersion = "1.3.60"
dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))

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

publishing {
    repositories {
        maven {
            url = uri("http://oss.jfrog.org/oss-release-local")
            credentials {
                username =
                    if (project.hasProperty("bintrayUser"))
                        project.property("bintrayUser").toString()
                    else System.getenv("BINTRAY_USER")
                password =
                    if (project.hasProperty("bintrayApiKey"))
                        project.property("bintrayApiKey").toString()
                    else System.getenv("BINTRAY_API_KEY")
            }
        }
    }
}
