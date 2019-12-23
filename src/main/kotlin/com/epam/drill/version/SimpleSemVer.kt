package com.epam.drill.version

import kotlin.math.*

data class SimpleSemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val suffix: String = ""
) : Comparable<SimpleSemVer> {
    override fun compareTo(other: SimpleSemVer) = when {
        major != other.major -> (major - other.major)
        minor != other.minor -> minor - other.minor
        patch != other.patch -> patch - other.patch
        else -> suffix.compareTo(other.suffix)
    }.sign

    fun toTag(): String {
        return "$major.$minor.$patch"
    }

    override fun toString(): String {
        return listOf("$major.$minor.$patch", suffix).joinToString("-")
    }
}

private val semVerRegex = Regex("(\\d+)\\.(\\d+)\\.(\\d+)(-[^\\s]+|)")

internal fun String.toSemVer(): SimpleSemVer? = semVerRegex.matchEntire(this)?.run {
    val (_, major, minor, patch, suffix) = groupValues
    SimpleSemVer(
        major = major.toInt(),
        minor = minor.toInt(),
        patch = patch.toInt(),
        suffix = suffix.substringAfter("-")
    )
}
