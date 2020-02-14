package com.epam.drill.gradle

import com.epam.drill.gradle.version.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*

class VersionRetriever : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (!hasProperty("version")) {
            val gitVersion: SimpleSemVer by GitVersionDelegateProvider(rootProject)
            version = gitVersion
            tasks {
                register("printReleaseVersion") {
                    group = "version"
                    doLast { println(gitVersion.copy(suffix = "")) }
                }
            }
        }

        tasks {
            register("printVersion") {
                group = "version"
                doLast { println(version) }
            }
        }
    }
}

