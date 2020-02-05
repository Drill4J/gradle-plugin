package com.epam.drill.knative

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val Project.kotlinMultiplatfrom get() = extensions.getByType(KotlinMultiplatformExtension::class.java)
fun Project.kmp(callback: KotlinMultiplatformExtension.() -> Unit) = kotlinMultiplatfrom.apply(callback)
