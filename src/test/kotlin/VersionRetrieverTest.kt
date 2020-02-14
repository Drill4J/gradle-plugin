package com.epam.drill.gradle

import com.epam.drill.gradle.version.*
import org.eclipse.jgit.api.*
import org.gradle.testkit.runner.*
import org.junit.rules.*
import org.junit.runner.*
import java.io.*
import java.util.concurrent.*
import kotlin.test.*


class VersionRetrieverTest {
    private val projectVersion = System.getProperty("project.version")

    private val builder = BuildScriptBuilder(projectVersion)
    private val firstTag = SimpleSemVer(0, 1, 0)
    private lateinit var buildGradleFile: File
    private lateinit var srcDir: File
    private lateinit var git: Git

    @get:org.junit.Rule
    val testName = TestName()

    @get:org.junit.Rule
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

    @get:org.junit.Rule
    val projectDir = TemporaryFolder()

    @BeforeTest
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
        buildGradleFile.writeText(
            """
            $text 
            $versionPrinter
            """
        )
        git = Git.init().setGitDir(projectDir.root.resolve(".git")).call()
    }

    @Test
    fun `no repo - default version`() {
        val deleted = projectDir.root.resolve(".git").deleteRecursively()
        assertTrue(actual = deleted, message = "Error deleting .git dir")
        val output = GradleRunner.create()
            .withProjectDir(projectDir.root)
            .withArguments("build")
            .withGradleVersion("6.0.1")
            .withPluginClasspath()
            .withDebug(true)
            .build().output
        println(output)
        assertTrue(output.contains("version: '0.1.0-0'"))
    }

    @Test
    fun `empty repo - default version`() {
        val output = GradleRunner.create()
            .withProjectDir(projectDir.root)
            .withArguments("build")
            .withGradleVersion("6.0.1")
            .withPluginClasspath()
            .withDebug(true)
            .build().output
        println(output)
        assertTrue(output.contains("version: '0.1.0-0'"))
    }

    @Test
    fun `print current version for new commit`() {
        firstCommit()
        git.randomCommit()
        val output = runTaskQuietly("printVersion")
        println(output)
        val taskOutput = output.outputLine()
        assertEquals("0.2.0-0", taskOutput)
    }

    @Test
    fun `print release version for new commit`() {
        firstCommit()
        git.randomCommit()
        val output = runTaskQuietly("printReleaseVersion")
        println(output)
        val taskOutput = output.outputLine()
        assertEquals("0.2.0", taskOutput)
    }

    @Test
    fun `print release version for tagged commit`() {
        firstCommit()
        git.randomCommit()
        git.tag("0.2.0-0")
        val output = runTaskQuietly("printReleaseVersion")
        println(output)
        val taskOutput = output.outputLine()
        assertEquals("0.2.0", taskOutput)
    }

    @Test
    fun `ignore non-version tags - current version`() {
        firstCommit()
        git.randomCommit()
        git.tag("something0.3.0")
        git.tag("sometag")
        val version = runTaskQuietly("printVersion").outputLine()
        assertEquals("0.2.0-0", version)
    }


    @Test
    fun `ignore non-version tags - release version`() {
        firstCommit()
        git.randomCommit()
        git.tag("v0.2.0-0")
        git.tag("something0.3.0")
        git.tag("sometag")
        val version = runTaskQuietly("printReleaseVersion").outputLine()
        assertEquals("0.2.0", version)
    }

    @Test
    fun `print release version for tagged commit - v prefix`() {
        firstCommit()
        git.randomCommit()
        git.tag("v0.2.0-0")
        val output = runTaskQuietly("printReleaseVersion")
        println(output)
        val taskOutput = output.outputLine()
        assertEquals("0.2.0", taskOutput)
    }

    @Test
    fun `print version after release`() {
        firstCommit()
        git.randomCommit()
        git.tag("v0.2.0-0")
        git.tag("v0.2.0-1")
        git.tag("v0.2.0")
        git.randomCommit()
        val output = runTaskQuietly("printVersion")
        println(output)
        val taskOutput = output.outputLine()
        assertEquals("0.3.0-0", taskOutput)
    }


    @Test
    fun `print version after custom suffix - rc1`() {
        firstCommit()
        git.randomCommit()
        git.tag("v0.2.0-55")
        git.tag("v0.2.0-rc1")
        git.randomCommit()
        val output = runTaskQuietly("printVersion")
        println(output)
        val taskOutput = output.outputLine()
        assertEquals("0.2.0-56", taskOutput)
    }

    private fun runTaskQuietly(task: String): String {
        return GradleRunner.create()
            .withProjectDir(projectDir.root)
            .withPluginClasspath()
            .withArguments("-q", task)
            .build().output
    }

    private fun firstCommit() {
        git.add().addFilepattern(".").call()
        git.commit().setMessage("first commit").call()

        git.tag(firstTag.toString())
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

private fun String.outputLine() = lines().last(String::isNotBlank)
