package com.epam.drill

import com.epam.drill.version.*
import org.eclipse.jgit.api.*
import org.gradle.testkit.runner.*
import org.junit.rules.*
import org.junit.runner.*
import java.io.*
import kotlin.test.*


class VersionRetrieverTest {
    private val builder = BuildScriptBuilder()
    private val firstTag = SimpleSemVer(0, 0, 1)
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
        git.add().addFilepattern(".").call()
        git.commit().setMessage("first commit").call()

        git.tag(firstTag.toTag())
    }

    @Test
    fun `release version equals of last tag with zero distance`() {
        git.randomCommit()
        git.tag("v0.1.0")
        Thread.sleep(1500)
        git.randomCommit()
        git.tag("0.1.3")
        Thread.sleep(1500)
        git.randomCommit()
        git.tag("0.1.2")
        git.randomCommit()
        git.tag("0.1.11")
        val output = GradleRunner.create()
            .withProjectDir(projectDir.root)
            .withArguments("build")
            .withGradleVersion("5.6.2")
            .withPluginClasspath()
            .withDebug(true)
            .build().output
        println(output)
        assertTrue(output.contains("version: '0.1.11'"))
    }

    @Test
    fun `max version equals of last tag`() {
        val listOf = listOf(
            "0.0.1",
            "0.2.1",
            "0.2.100",
            "0.2.10",
            "0.2.2",
            "0.3.0-123",
            "0.3.0-124"
        )
        val lastVersion = listOf.lastVersion()
        assertEquals("0.3.0-124", lastVersion.toString())
    }
}
