package com.epam.drill.version

import org.eclipse.jgit.api.*
import org.eclipse.jgit.internal.storage.file.*
import org.gradle.api.*
import java.io.*
import java.util.concurrent.*

class VersionRetriever : Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
        val git = Git.wrap(FileRepository(target.rootProject.rootDir.resolve(".git")))
        try {
            val tags = git.tagList().call()
                .map { it.name.substringAfter("refs/tags/", "") }
                .filter(String::isNotBlank)
            if (logger.isDebugEnabled) {
                tags.forEach(logger::debug)
            }
            val lastVersion = tags
                .lastVersion()

            logger.info("Last version from tags $lastVersion")
            target.version = lastVersion ?: SimpleSemVer(0, 1, 0)
            logger.info("Project version ${target.version}")
        } catch (ex: Exception) {
            target.version = "0.1.0"
            logger.error(
                "Something went wrong. $ex \n" +
                    "Version will be changed to ${target.version}"
            )
        }
    }
}

fun List<String>.lastVersion(): SimpleSemVer? = mapNotNull(String::toSemVer).max()

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
