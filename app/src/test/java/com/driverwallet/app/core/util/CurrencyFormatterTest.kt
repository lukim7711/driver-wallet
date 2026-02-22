package com.driverwallet.app.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrencyFormatterTest {

    @Nested
    @DisplayName("format()")
    inner class Format {

        @Test
        fun `zero returns 0`() {
            assertEquals("0", CurrencyFormatter.format(0))
        }

        @Test
        fun `below thousand has no separator`() {
            assertEquals("999", CurrencyFormatter.format(999))
        }

        @Test
        fun `thousand uses dot separator for id locale`() {
            assertEquals("1.000", CurrencyFormatter.format(1_000))
        }

        @Test
        fun `million formats correctly`() {
            assertEquals("1.000.000", CurrencyFormatter.format(1_000_000))
        }

        @Test
        fun `max amount formats correctly`() {
            assertEquals("999.999.999", CurrencyFormatter.format(999_999_999))
        }

        @Test
        fun `negative amount formats with minus`() {
            assertEquals("-1.000", CurrencyFormatter.format(-1_000))
        }
    }

    @Nested
    @DisplayName("formatWithPrefix()")
    inner class FormatWithPrefix {

        @Test
        fun `prepends Rp prefix`() {
            assertEquals("Rp 0", CurrencyFormatter.formatWithPrefix(0))
        }

        @Test
        fun `prefix with thousand`() {
            assertEquals("Rp 50.000", CurrencyFormatter.formatWithPrefix(50_000))
        }
    }

    @Nested
    @DisplayName("formatShort()")
    inner class FormatShort {

        @Test
        fun `below 1000 returns plain format`() {
            assertEquals("500", CurrencyFormatter.formatShort(500))
        }

        @Test
        fun `1000 returns 1rb`() {
            assertEquals("1rb", CurrencyFormatter.formatShort(1_000))
        }

        @Test
        fun `50_000 returns 50rb`() {
            assertEquals("50rb", CurrencyFormatter.formatShort(50_000))
        }

        @Test
        fun `1_000_000 returns 1jt`() {
            assertEquals("1jt", CurrencyFormatter.formatShort(1_000_000))
        }

        @Test
        fun `1_500_000 returns 1jt due to integer division`() {
            assertEquals("1jt", CurrencyFormatter.formatShort(1_500_000))
        }

        @Test
        fun `1_000_000_000 returns 1M`() {
            assertEquals("1M", CurrencyFormatter.formatShort(1_000_000_000))
        }

        @Test
        fun `zero returns 0`() {
            assertEquals("0", CurrencyFormatter.formatShort(0))
        }
    }
}
