package com.epam.drill.gradle.version

import groovy.json.*
import org.eclipse.jgit.api.*
import java.io.*
import java.util.*

class BuildScriptBuilder(val pluginVersion: String) {

    val scriptClassPath = mutableListOf<Any>()
    val compileDependencies = mutableListOf<String>()

    val applyPlugins = mutableListOf<String>()

    fun applyDrillVersioningPlugin() {
        applyPlugins += "com.epam.drill.version.plugin"
    }

    fun build(body: Builder.() -> Unit = {}): String = Builder().apply {
        
        

        block("buildscript") {
            repositories()
            scriptClassPath.add("com.epam.drill:gradle-plugin:$pluginVersion")
            block("dependencies") {
                dependencies("classpath", scriptClassPath)
            }
        }

        for (plugin in applyPlugins) {
            line("apply plugin: ${JsonOutput.toJson(plugin)}")
        }

        repositories()

        block("dependencies") {
            dependencies("implementation", compileDependencies)
        }

        body()
    }.build()

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
            line("mavenLocal()")
            line("jcenter()")
            line("mavenCentral()")
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
