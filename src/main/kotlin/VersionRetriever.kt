package com.epam.drill.gradle

import com.epam.drill.gradle.version.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*

class VersionRetriever : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val gitVersion: SimpleSemVer by GitVersionDelegateProvider(rootProject)
        version = gitVersion

        target.tasks {
            register("printVersion") {
                group = "version"
                doLast { println(gitVersion) }
            }

            register("printReleaseVersion") {
                group = "version"
                doLast { println(gitVersion.copy(suffix = "")) }
            }
        }
    }
}

