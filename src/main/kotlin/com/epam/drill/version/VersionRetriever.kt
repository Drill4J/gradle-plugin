package com.epam.drill.version

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import java.io.*

private val defaultVersion = SimpleSemVer(0, 1, 0, "0")

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

internal class GitVersionDelegateProvider(
    private val project: Project
) {
    operator fun provideDelegate(
        thisRef: Any?,
        property: kotlin.reflect.KProperty<*>
    ): InitialValueExtraPropertyDelegate<SimpleSemVer> {
        val extra = project.extra
        if (!extra.has(property.name)) {
            extra.set(property.name, project.resolveGitVersion())
        }
        return InitialValueExtraPropertyDelegate.of(extra)
    }
}

internal fun Project.resolveGitVersion(): SimpleSemVer {
    return try {
        val commitName = git("rev-parse", "HEAD")
        commitName?.let {
            val rawTag = git(
                "describe", "--tags",
                "--match", "[0-9]*", "--match", "v[0-9]*"
            )
            rawTag?.tagToSemVer(it)
        } ?: defaultVersion
    } catch (ex: Exception) {
        logger.error("Git 'describe' failed, defaulting to v$defaultVersion")
        defaultVersion
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
