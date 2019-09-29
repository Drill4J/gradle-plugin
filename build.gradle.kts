plugins {
    kotlin("jvm") version "1.3.50"
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

    testImplementation(kotlin("test-junit"))
    testImplementation(gradleTestKit())
}

tasks.test {
    dependsOn("publishToMavenLocal")
}
