package com.epam.drill.version

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import java.io.*

private val defaultVersion = SimpleSemVer(0, 1, 0, "0")

class VersionRetriever : Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
        val version = try {
            val commitName = target.git("rev-parse", "HEAD")
            commitName?.let {
                val rawTag = target.git(
                    "describe", "--tags",
                    "--match", "[0-9]*", "--match", "v[0-9]*"
                )
                rawTag?.tagToSemVer(it)
            } ?: defaultVersion
        } catch (ex: Exception) {
            logger.error("Git 'describe' failed, defaulting to v$defaultVersion")
            defaultVersion
        }
        target.version = version

        target.tasks {
            register("printVersion") {
                group = "version"
                doLast { println(version) }
            }

            register("printReleaseVersion") {
                group = "version"
                doLast { println(version.copy(suffix = "")) }
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
    }
    return output.takeIf { execResult.exitValue == 0 }?.run {
        toString(Charsets.UTF_8.name()).trim()
    }?.takeIf(String::isNotEmpty)
}

internal fun String.tagToSemVer(commitName: String?): SimpleSemVer? {
    val shortCommitName = commitName?.substring(0, 7)
    val isNew = shortCommitName != null && endsWith(shortCommitName)
    val rawVersion = when {
        //example: 0.1.0-1-gb35375e -> 0.1.0 
        isNew -> substringBeforeLast("-").substringBeforeLast("-")
        else -> this
    }
    return rawVersion.toSemVer()?.run {
        when {
            isNew -> bump()
            else -> this
        }
    }
}
