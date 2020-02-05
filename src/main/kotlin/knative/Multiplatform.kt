package com.epam.drill.knative

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager


class MultiplatformConfig : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {

            plugins.apply("kotlin-multiplatform")
            kmp {
                if (isDevMode)
                    currentTarget {
                        compilations["main"].apply {
                            defaultSourceSet {
                                kotlin.srcDir("./src/nativeCommonMain/kotlin")
                                if (HostManager.hostIsLinux || HostManager.hostIsMac) {
                                    kotlin.srcDir("./src/nativePosixCommonMain/kotlin")
                                }

                            }
                        }
                    }
                else {
                    setOf(linuxX64(), macosX64(), mingwX64())
                }
                jvm()
                with(sourceSets) {
                    if (!isDevMode) {
                        val commonNativeMain = maybeCreate("nativeCommonMain")
                        val nativePosixCommonMain = maybeCreate("nativePosixCommonMain")
                        targets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().forEach {
                            val family = it.konanTarget.family
                            if (family == Family.OSX || family == Family.LINUX) {
                                it.compilations.forEach { knCompilation ->
                                    if (knCompilation.name == "main")
                                        knCompilation.defaultSourceSet {
                                            dependsOn(nativePosixCommonMain)
                                        }
                                }
                            }
                            it.compilations.forEach { knCompilation ->
                                if (knCompilation.name == "main")
                                    knCompilation.defaultSourceSet {
                                        dependsOn(commonNativeMain)
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

}