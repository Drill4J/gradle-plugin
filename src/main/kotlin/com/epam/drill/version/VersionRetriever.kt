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
    var tagDistance: Int = 0
    private val tagVersionRegex = Regex("refs/tags/v(\\d+)\\.(\\d+)\\.(\\d+)")

    override fun apply(target: Project) {
        target.run {
            val git = Git.wrap(FileRepository(target.rootProject.rootDir.resolve(".git")))
            try {

                val allTags = git.tagList().call()
                val lastTag = allTags.sortedBy { it.getSnapShot().lastModifiedInstant().toEpochMilli() }.lastOrNull { tagVersionRegex.matches(it.name) }
                if (lastTag != null) {
                    val (_, major, minor, patch) = tagVersionRegex.matchEntire(lastTag.name)!!.groupValues
                    tagDistance = git.getTagDistance(lastTag)
                    target.version = when (tagDistance) {
                        0 -> "$major.$minor.$patch"
                        else -> "$major.$minor.${patch.toInt().inc()}"
                    }
                } else target.version = "0.3.20"
            } catch (ex: Exception) {
                println("set the default tag. Exception occured")
                target.version = "0.3.20"
            }
            tasks {
                register("incrementVersion") {
                    val githubUserName = if (project.hasProperty("githubUserName"))
                        project.property("githubUserName").toString()
                    else System.getenv("githubUserName") ?: null
                    if (githubUserName == null) {
                        println("please provide GH credentials")
                        return@register
                    }
                    if (tagDistance > 0) {
                        println(tagDistance)
                        println(target.version)
                        git.tag("v${target.version}")

                        val githubUserPassword = if (project.hasProperty("githubUserPassword"))
                            project.property("githubUserPassword").toString()
                        else System.getenv("githubUserPassword")
                        git.push().setPushTags().setCredentialsProvider(UsernamePasswordCredentialsProvider(githubUserName, githubUserPassword)).call()
                    }
                }
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