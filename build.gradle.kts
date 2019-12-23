plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("gradle-plugin"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.5.0.201909110433-r")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation(gradleTestKit())
}

tasks.test {
    dependsOn("publishToMavenLocal")
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
