package com.epam.drill.gradle

import com.epam.drill.semver.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import java.io.*

private val GITHUB_REF = "GITHUB_REF"

private val TAG_REF_REGEX = "refs/tags/(.*)".toRegex()

class VersionRetriever : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        if (version == Project.DEFAULT_VERSION) {
            version = System.getenv(GITHUB_REF)?.let(TAG_REF_REGEX::matchEntire)?.run {
                val (_, tagVersion) = groupValues
                tagVersion.toSemVer().takeIf { it != UNSPECIFIED } ?: tagVersion
            } ?: git(
                "describe", "--tags", "--long",
                "--match", "[0-9]*.[0-9]*.[0-9]*",
                "--match", "v[0-9]*.[0-9]*.[0-9]*"
            )?.prereleaseFromGit() ?: "0.1.0-0".toSemVer()
        }

        target.tasks {
            register("printVersion") {
                group = "version"
                doLast { println(version) }
            }
        }
    }
}

internal fun Project.git(vararg args: String): String? {
    val output = ByteArrayOutputStream()
    val execResult = exec {
        workingDir = rootProject.rootDir
        executable = "git"
        args(*args)
        standardOutput = output
        isIgnoreExitValue = true
    }
    return output.takeIf { execResult.exitValue == 0 }?.run {
        toString(Charsets.UTF_8.name()).trim()
    }?.takeIf(String::isNotEmpty)
}
