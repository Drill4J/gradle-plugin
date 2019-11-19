package com.epam.drill.version

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.concurrent.TimeUnit


class VersionRetriever : Plugin<Project> {
    private val tagVersionRegex = Regex("refs/tags/(\\d+)\\.(\\d+)\\.(\\d+)")

    override fun apply(target: Project) {
        target.run {
            val git = Git.wrap(FileRepository(target.rootProject.rootDir.resolve(".git")))
            try {
                val sortedSemVerList = git.tagList().call()
                        .filter { tagVersionRegex.matches(it.name) }
                        .map {
                            val (_, major, minor, patch) = tagVersionRegex.matchEntire(it.name)!!.groupValues
                            SemVer(major.toInt(), minor.toInt(), patch.toInt())
                        }.sortedDescending()

                sortedSemVerList.forEach { logger.info(it.toString()) }

                val lastTag = sortedSemVerList.firstOrNull()

                logger.info(lastTag.toString())
                if (lastTag != null) {
                    target.version = lastTag
                } else target.version = "0.1.0"
            } catch (ex: Exception) {

                target.version = "0.1.0"
                logger.error("Something went wrong. $ex \n" +
                        "Version will be changed to ${target.version}")
            }
        }
    }

}

fun Git.tag(name: String) {
    "git tag $name".runCommand(this.repository.directory.parentFile)
}

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
}


data class SemVer(val major: Int, val minor: Int, val patch: Int) : Comparable<SemVer> {
    override fun compareTo(other: SemVer) = when {
        major != other.major -> major - other.major
        minor != other.minor -> minor - other.minor
        else -> patch - other.patch
    }


    fun toTag(): String {
        return "$major.$minor.$patch"
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }

}