package com.epam.drill.version

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.internal.storage.file.FileSnapshot
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import java.io.File
import java.util.concurrent.TimeUnit


class VersionRetriever : Plugin<Project> {
    private val tagVersionRegex = Regex("refs/tags/(\\d+)\\.(\\d+)\\.(\\d+)")

    override fun apply(target: Project) {
        target.run {
            val git = Git.wrap(FileRepository(target.rootProject.rootDir.resolve(".git")))
            try {
                val allTags = git.tagList().call()
                allTags.forEach {
                    logger.info(it.name)
                }

                val lastTag = allTags.filter { tagVersionRegex.matches(it.name) }.maxBy { it.getSnapShot().lastModifiedInstant().toEpochMilli() }
                logger.info(lastTag?.name)
                if (lastTag != null) {
                    val (_, major, minor, patch) = tagVersionRegex.matchEntire(lastTag.name)!!.groupValues
                    target.version = "$major.$minor.$patch"
                } else target.version = "0.1.0"
            } catch (ex: Exception) {
                logger.error("xx", ex)
                target.version = "0.1.0"
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

fun Git.getTagDistance(tag: Ref) = this.log()
        .addRange(tag.objectId, this.repository.findRef(Constants.HEAD).objectId).call().count()


fun Ref.getSnapShot(): FileSnapshot {
    val getSnapShot = this::class.java.methods.first { it.name == ::getSnapShot.name }
    getSnapShot.isAccessible = true
    return getSnapShot(this) as FileSnapshot
}