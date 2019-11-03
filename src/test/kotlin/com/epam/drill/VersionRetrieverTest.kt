package com.epam.drill

import com.epam.drill.version.tag
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import kotlin.test.assertTrue


class VersionRetrieverTest {
    private val builder = BuildScriptBuilder()
    private val firstTag = SemVer(0, 0, 1)
    private lateinit var buildGradleFile: File
    private lateinit var srcDir: File
    private lateinit var git: Git

    @get:Rule
    val testName = TestName()

    @get:Rule
    val failedRule = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description?) {
            val dst = File("build/tests/${testName.methodName.replace("[", "-").replace("]", "")}").apply { mkdirs() }
            projectDir.root.copyRecursively(dst, true) { file, copyError ->
                System.err.println("Failed to copy $file due to ${copyError.message}")
                OnErrorAction.SKIP
            }
            println("Copied project to ${dst.absolutePath}")
        }

    }

    @get:Rule
    val projectDir = TemporaryFolder()

    @Before
    fun setUp() {
        projectDir.delete()
        projectDir.create()
        projectDir.root.resolve("build/kotlin-build/caches").mkdirs()

        buildGradleFile = projectDir.root.resolve("build.gradle")
        srcDir = projectDir.root.resolve("src/main/kotlin")

        buildGradleFile.parentFile.mkdirs()
        builder.applyDrillVersioningPlugin()

        val text = builder.build()

        val versionPrinter = "println(\"version: '\${project.version}'\")"
        buildGradleFile.writeText("""
            $text 
            $versionPrinter
            """)
        git = Git.init().setGitDir(projectDir.root.resolve(".git")).call()
        git.add().addFilepattern(".").call()
        git.commit().setMessage("first commit").call()

        git.tag(firstTag.toTag())
    }

    @Test
    fun `release version equals of last tag with zero distance`() {

//        git.randomCommit()
//        git.tag("v0.1.0")
//        Thread.sleep(1500)
//        git.randomCommit()
//        git.tag("v0.1.3")
//        Thread.sleep(1500)
//        git.randomCommit()
//        git.tag("v0.1.2")
//        git.randomCommit()
        val output = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments("incrementVersion")
                .withGradleVersion("5.6.1")
                .withPluginClasspath()
                .withDebug(true)
                .build().output
        println(output)
        output.contains("version: '0.1.2'")

    }

    @Test
    fun `increment minor verison and added snapshot prefix when commit distance != 0`() {
        git.randomCommit()
        assertTrue {
            GradleRunner.create()
                    .withProjectDir(projectDir.root)
                    .withArguments("build")
                    .withGradleVersion("5.6.1")
                    .withPluginClasspath()
                    .withDebug(true)
                    .build().output.contains("version: '${firstTag.copy(minor = firstTag.minor.inc()).toVersion()}-SNAPSHOT'")
        }
    }


}

data class SemVer(val major: Int, val minor: Int, val patch: Int) {
    fun toTag(): String {
        return "v$major.$minor.$patch"
    }

    fun toVersion(): String {
        return "$major.$minor.$patch"
    }

}