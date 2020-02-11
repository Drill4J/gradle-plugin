@file:Suppress("unused")

package com.epam.drill.gradle

import org.gradle.api.*
import org.gradle.api.internal.plugins.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*
import java.util.concurrent.atomic.*


private const val COMMON = "common"

private const val POSIX = "posix"

class CrossCompilation : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kotlinExt = extensions.getByName("kotlin") as KotlinProjectExtension
            val kotlinExtDsl = DslObject(kotlinExt)
            @Suppress("UNCHECKED_CAST") val targets =
                kotlinExtDsl.extensions.getByName("targets") as NamedDomainObjectCollection<KotlinTarget>
            val linuxX64Targets = targets.nativeTargets { konanTarget == KonanTarget.LINUX_X64 }
            linuxX64Targets.firstOnly {
                val common = compilations.create(COMMON)
                val posix = compilations.create(POSIX)
                common.setCommonSources()
                posix.setCommonSources()
                posix.associateWith(common)
                mainCompilation.associateWith(posix)
                targets.nativeTargets { konanTarget != KonanTarget.LINUX_X64 }.all {
                    mainCompilation.defaultSourceSet {
                        if (konanTarget.family != Family.MINGW) {
                            dependsOn(common.defaultSourceSet)
                            dependsOn(posix.defaultSourceSet)
                        } else {
                            dependsOn(common.defaultSourceSet)
                        }
                    }
                }
            }
            kotlinExtDsl.extensions.create("crossCompilation", CrossCompilationExtension::class.java, targets)
            project.afterEvaluate {
                if (linuxX64Targets.isEmpty())
                    throw GradleException(
                        "You must specify a ${KonanTarget.LINUX_X64} target to configure cross compilation."
                    )
            }
        }
    }
}

open class CrossCompilationExtension(
    private val targets: NamedDomainObjectCollection<KotlinTarget>
) {
    fun common(block: KotlinNativeCompilation.() -> Unit) {
        targets.configure(COMMON, block)
    }

    fun posix(block: KotlinNativeCompilation.() -> Unit) {
        targets.configure(POSIX, block)
    }
}

fun NamedDomainObjectCollection<KotlinTarget>.nativeTargets(
    matcher: KotlinNativeTarget.() -> Boolean = { true }
) = withType(KotlinNativeTarget::class).matching(matcher)

val NamedDomainObjectCollection<KotlinTarget>.linuxX64Targets
    get() = withType(KotlinNativeTarget::class).matching { it.konanTarget == KonanTarget.LINUX_X64 }

val KotlinNativeTarget.mainCompilation: KotlinNativeCompilation
    get() = compilations[KotlinCompilation.MAIN_COMPILATION_NAME]


fun <T> NamedDomainObjectCollection<T>.firstOnly(block: T.() -> Unit) {
    val configured = AtomicBoolean()
    all {
        if (configured.compareAndSet(false, true)) {
            block()
        } else throw GradleException(
            "You can't specify more then one ${KonanTarget.LINUX_X64} target for cross compilation."
        )
    }
}

private fun NamedDomainObjectCollection<KotlinTarget>.configure(
    compilationName: String,
    block: KotlinNativeCompilation.() -> Unit
) = linuxX64Targets.all {
    compilations[compilationName].apply(block)
}

private fun KotlinNativeCompilation.setCommonSources() {
    defaultSourceSet {
        val srcRoot = "src/${compilationName}Native"
        kotlin.setSrcDirs(listOf("$srcRoot/kotlin"))
        resources.setSrcDirs(listOf("$srcRoot/resources"))
    }
}
