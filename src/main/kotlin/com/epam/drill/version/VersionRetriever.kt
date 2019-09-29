package com.epam.drill.version

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.internal.storage.file.FileSnapshot
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref

class VersionRetriever : Plugin<Project> {
    private val tagVersionRegex = Regex("refs/tags/v(\\d+)\\.(\\d+)\\.(\\d+)")

    override fun apply(target: Project) {
        val git = Git.wrap(FileRepository(target.rootDir.resolve(".git")))
        val allTags = git.tagList().call()
        val lastTag = allTags.sortedBy { it.getSnapShot().lastModifiedInstant() }.lastOrNull { tagVersionRegex.matches(it.name) }
        if (lastTag != null) {
            val (_, major, minor, patch) = tagVersionRegex.matchEntire(lastTag.name)!!.groupValues
            target.version = when (git.getTagDistance(lastTag)) {
                0 -> "$major.$minor.$patch"
                else -> "$major.${minor.toInt().inc()}.$patch-SNAPSHOT"
            }
        } else target.version = "0.1.0-SNAPSHOT"

    }

}

fun Git.getTagDistance(tag: Ref) = this.log()
        .addRange(tag.objectId, this.repository.findRef(Constants.HEAD).objectId).call().count()


fun Ref.getSnapShot(): FileSnapshot {
    val getSnapShot = this::class.java.methods.first { it.name == ::getSnapShot.name }
    getSnapShot.isAccessible = true
    return getSnapShot(this) as FileSnapshot
}