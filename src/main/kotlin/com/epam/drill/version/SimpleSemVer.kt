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

    fun incMinor() = copy(minor = minor + 1)

    fun next() = copy(
        minor = if (suffix.isEmpty()) minor + 1 else minor,
        //TODO regex replace
        suffix = "${suffix.toIntOrNull()?.inc() ?: 0}"
    )

    override fun toString(): String {
        val optSuffix = when (suffix) {
            "" -> ""
            else -> "-$suffix"
        }
        return "$major.$minor.$patch$optSuffix"
    }
}


private val semVerRegex = Regex("v?(\\d+)\\.(\\d+)\\.(\\d+)(-[^\\s]+|)")

internal fun String.toSemVer(): SimpleSemVer? = semVerRegex.matchEntire(this)?.run {
    val (_, major, minor, patch, suffix) = groupValues
    SimpleSemVer(
        major = major.toInt(),
        minor = minor.toInt(),
        patch = patch.toInt(),
        suffix = suffix.substringAfter("-")
    )
}
