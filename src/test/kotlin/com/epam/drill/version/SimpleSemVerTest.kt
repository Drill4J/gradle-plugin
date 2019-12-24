package com.epam.drill.version

import kotlin.test.*

class SimpleSemVerTest {
    @Test
    fun `parse simple tag`() {
        val simpleTag = "0.1.0"
        assertEquals(SimpleSemVer(0, 1, 0), simpleTag.toSemVer())
    }

    @Test
    fun `parse v tag`() {
        val simpleTag = "v0.1.0"
        assertEquals(SimpleSemVer(0, 1, 0), simpleTag.toSemVer())
    }

    @Test
    fun `parse tag wiht suffix`() {
        val simpleTag = "0.1.0-123"
        assertEquals(SimpleSemVer(0, 1, 0, "123"), simpleTag.toSemVer())
    }

    @Test
    fun `next version with suffix`() {
        val next = "0.1.0-123".toSemVer()?.next()
        assertEquals(SimpleSemVer(0, 1, 0, "124"), next)
    }

    @Test
    fun `next version without suffix`() {
        val next = "0.1.0".toSemVer()?.next()
        assertEquals(SimpleSemVer(0, 2, 0, "0"), next)
    }
}