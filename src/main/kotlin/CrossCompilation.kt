@file:Suppress("unused")

package com.epam.drill.gradle

import org.gradle.api.*
import org.gradle.api.internal.plugins.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*


private const val COMMON = "common"

private const val POSIX = "posix"

class CrossCompilation : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kotlinExt = extensions.getByName("kotlin") as KotlinProjectExtension
            val kotlinExtDsl = DslObject(kotlinExt)
            @Suppress("UNCHECKED_CAST") val targets =
                kotlinExtDsl.extensions.getByName("targets") as NamedDomainObjectCollection<KotlinTarget>
            targets.whenObjectAdded {
                if (this is KotlinNativeTarget && konanTarget.family == Family.LINUX
                ) {
                    val common = compilations.create(COMMON)
                    val posix = compilations.create(POSIX) {
                        defaultSourceSet.dependsOn(common.defaultSourceSet)
                    }
                    posix.defaultSourceSet.dependsOn(common.defaultSourceSet)
                    targets.withType(KotlinNativeTarget::class) {
                        val main by compilations
                        main.defaultSourceSet {
                            if (konanTarget.family != Family.MINGW) {
                                dependsOn(common.defaultSourceSet)
                                dependsOn(posix.defaultSourceSet)
                            } else {
                                dependsOn(common.defaultSourceSet)
                            }
                        }
                    }
                }
            }
            kotlinExtDsl.extensions.create("crossCompilation", CrossCompilationExtension::class.java, targets)
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

private fun NamedDomainObjectCollection<KotlinTarget>.configure(
    compilationName: String,
    block: KotlinNativeCompilation.() -> Unit
) = withType(KotlinNativeTarget::class)
    .matching { it.konanTarget == KonanTarget.LINUX_X64 }.all {
        compilations.findByName(compilationName)?.apply(block)
    }
