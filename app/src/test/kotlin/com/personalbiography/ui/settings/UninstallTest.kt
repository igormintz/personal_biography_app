package com.personalbiography.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class UninstallTest {
    @Test
    fun `uninstall uri uses package scheme with given package name`() {
        assertEquals(
            "package:com.personalbiography",
            uninstallUriForPackage("com.personalbiography"),
        )
    }

    @Test
    fun `uninstall uri works for arbitrary package names`() {
        assertEquals(
            "package:com.example.foo",
            uninstallUriForPackage("com.example.foo"),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank package name is rejected`() {
        uninstallUriForPackage("   ")
    }
}
