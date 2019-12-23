@file:Suppress("UnstableApiUsage")

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

tasks {
    test {
        systemProperty("project.version", project.version)
        dependsOn(publishToMavenLocal)
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Drill4j/${project.name}")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GH_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GH_API_KEY")
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            from(components["kotlin"])
            artifact(sourcesJar.get())
        }
    }
}
