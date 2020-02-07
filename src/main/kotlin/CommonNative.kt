@file:Suppress("unused")
package com.epam.drill.gradle

import org.gradle.api.attributes.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*

private const val commonNative = "commonNative"

private const val commonPosix = "commonPosix"

private val targetAttribute = Attribute.of(
    "com.epam.drill.common.native.target",
    String::class.java
)

fun KotlinMultiplatformExtension.commonNative(
    configure: KotlinNativeTargetWithTests.() -> Unit = { }
): KotlinNativeTargetWithTests = common(
    name = commonNative,
    configure = configure,
    matcher = { !isCommonNative },
    depCondition = { isCommonPosix || isMingw || targets.findByName(commonPosix) == null }
)

fun KotlinMultiplatformExtension.commonPosix(
    configure: KotlinNativeTargetWithTests.() -> Unit = { }
): KotlinNativeTargetWithTests = common(
    name = commonPosix,
    configure = configure,
    matcher = { !isCommonPosix && !isCommonNative && !isMingw }
)

val KotlinNativeTarget.mainCompilation: KotlinNativeCompilation get() = compilations["main"]

val KotlinNativeTarget.defaultSourceSet: KotlinSourceSet get() = mainCompilation.defaultSourceSet

private fun KotlinMultiplatformExtension.common(
    name: String,
    configure: KotlinNativeTargetWithTests.() -> Unit = { },
    matcher: KotlinNativeTarget.() -> Boolean,
    depCondition: KotlinNativeTarget.() -> Boolean = { true }
): KotlinNativeTargetWithTests = linuxX64(name) {
    attributes.attribute(targetAttribute, name)
    val commonSourceSet = defaultSourceSet
    targets.withType<KotlinNativeTarget>()
        .matching(matcher)
        .all {
            with(mainCompilation) {
                defaultSourceSet {
                    if (!target.publishable) {
                        kotlin.setSrcDirs(emptyList<Any>())
                    }
                    if (depCondition()) {
                        dependsOn(commonSourceSet)
                    }
                }
            }
        }
    configure()
}

private val KotlinNativeTarget.isCommonNative get() = name == commonNative

private val KotlinNativeTarget.isCommonPosix get() = name == commonPosix

private val KotlinNativeTarget.isMingw get() = konanTarget.family == Family.MINGW
