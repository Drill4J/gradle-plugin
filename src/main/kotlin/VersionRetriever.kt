package com.epam.drill.gradle

import com.epam.drill.gradle.version.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*

private val GITHUB_REF = "GITHUB_REF"

private val TAG_REF_REGEX = "refs/tags/(.*)".toRegex()

class VersionRetriever : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        if (version == Project.DEFAULT_VERSION) {
            version = System.getenv(GITHUB_REF)?.let(TAG_REF_REGEX::matchEntire)?.run {
                val (_, tagVersion) = groupValues
                tagVersion
            } ?: run {
                val gitVersion by GitVersionDelegateProvider(rootProject)
                gitVersion
            }
        }

        target.tasks {
            register("printVersion") {
                group = "version"
                doLast { println(version) }
            }

            register("printReleaseVersion") {
                group = "version"
                doLast {
                    val releaseVersion = (version as? SimpleSemVer)?.copy(suffix = "") ?: version
                    println(releaseVersion)
                }
            }
        }
    }
}

