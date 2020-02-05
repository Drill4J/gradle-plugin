package com.epam.drill.publish

import org.gradle.api.*
import org.gradle.api.publish.PublishingExtension

class Publish : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        plugins.apply("org.gradle.maven-publish")
        with(extensions.getByType(PublishingExtension::class.java)) {
            publications {
                repositories {
                    maven {
                        url = uri(
                            if (project.hasProperty("drill.content.repository"))
                                project.property("drill.content.repository").toString()
                            else "https://oss.jfrog.org/oss-release-local"
                        )
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
        }

    }
}
