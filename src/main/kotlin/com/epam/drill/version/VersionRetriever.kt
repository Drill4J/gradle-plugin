package com.epam.drill.version

import org.eclipse.jgit.api.*
import org.eclipse.jgit.internal.storage.file.*
import org.eclipse.jgit.lib.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*

private val defaultVersion = SimpleSemVer(0, 1, 0, "0")

class VersionRetriever : Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
        val gitRepo = FileRepository(target.rootProject.rootDir.resolve(".git"))
        val git = Git.wrap(gitRepo)
        val version = try {
            val head = git.repository.resolve(Constants.HEAD)
            val commitName = head?.name
            git.describe().setTags(true).call()?.tagToSemVer(commitName) ?: defaultVersion
        } catch (ex: Exception) {
            logger.error("Git 'describe' failed, defaulting to v$defaultVersion")
            defaultVersion
        }
        target.version = version

        target.tasks {
            register("printVersion") {
                doLast { println(version) }
            }

            register("printReleaseVersion") {
                doLast { println(version.copy(suffix = "")) }
            }
        }
    }
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
            isNew -> next()
            else -> this
        }
    }
}
