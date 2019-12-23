package com.epam.drill.version

import groovy.json.JsonOutput
import org.eclipse.jgit.api.Git
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class BuildScriptBuilder {
    var kotlinVersion = "1.0.6"

    val scriptClassPath = ArrayList<Any>()
    val compileDependencies = ArrayList<String>()

    val applyPlugins = ArrayList<String>()

    fun applyDrillVersioningPlugin() {
        applyPlugins += "com.epam.drill.version.plugin"
    }

    fun build(body: Builder.() -> Unit = {}): String {
        val builder = Builder()

        builder.apply {
            block("buildscript") {
                line("ext.kotlin_version = ${JsonOutput.toJson(kotlinVersion)}")

                repositories()
                scriptClassPath.add("com.epam.drill:drill-gradle-plugin:0.4.0")
                block("dependencies") {
                    dependencies("classpath", scriptClassPath)
                }
            }

            for (plugin in applyPlugins) {
                line("apply plugin: ${JsonOutput.toJson(plugin)}")
            }

            repositories()

            block("dependencies") {
                dependencies("compile", compileDependencies)
            }

            body()
        }

        return builder.build()
    }

    private fun Builder.dependencies(type: String, list: List<Any>) {
        for (dep in list) {
            if (dep is String) {
                if (dep.startsWith(":")) {
                    line("$type project(${JsonOutput.toJson(dep)})")
                } else {
                    line("$type ${JsonOutput.toJson(dep)}")
                }
            } else if (dep is File) {
                line("$type(files(${JsonOutput.toJson(dep.absolutePath)}))")
            } else {
                throw IllegalArgumentException("Unsupported dependency type $dep")
            }
        }
    }

    private fun Builder.repositories() {
        block("repositories") {
            line("jcenter()")
            line("mavenCentral()")
            line("mavenLocal()")
        }
    }

    private fun Builder.mavenRepo(url: String) {
        block("maven") {
            line("url ${JsonOutput.toJson(url)}")
        }
    }

    inner class Builder {
        private val sb = StringBuilder(1024)
        var level: Int = 0

        fun line(text: String) {
            ensureLineStart()
            indent()
            appendDirect(text)
        }

        fun appendDirect(s: String) {
            sb.append(s)
        }

        fun ensureLineStart() {
            if (!sb.endsWith("\n")) {
                sb.appendln()
            }
        }

        fun build(): String {
            ensureLineStart()
            return sb.toString()
        }
    }

    private fun Builder.indent() {
        for (i in 1..level) {
            appendDirect("    ")
        }
    }
}

fun BuildScriptBuilder.Builder.block(name: String, block: () -> Unit) {
    line("$name {")
    level++
    block()
    level--
    line("}")
}

fun Git.randomCommit() {
    this.repository.directory.parentFile.resolve("${UUID.randomUUID()}.txt").createNewFile()
    this.add().addFilepattern(".").call()
    this.commit().setMessage(UUID.randomUUID().toString()).call()
}