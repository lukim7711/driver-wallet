package com.driverwallet.app.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class DateTimeExtTest {

    @Nested
    @DisplayName("nowJakarta()")
    inner class NowJakarta {

        @Test
        fun `returns Asia Jakarta timezone`() {
            val now = nowJakarta()
            assertEquals(ZoneId.of("Asia/Jakarta"), now.zone)
        }
    }

    @Nested
    @DisplayName("todayJakarta()")
    inner class TodayJakarta {

        @Test
        fun `returns a LocalDate not null`() {
            val today = todayJakarta()
            assertTrue(today is LocalDate)
        }
    }

    @Nested
    @DisplayName("startOfWeek()")
    inner class StartOfWeek {

        @Test
        fun `monday returns itself`() {
            val monday = LocalDate.of(2026, 2, 16) // Monday
            assertEquals(monday, monday.startOfWeek())
        }

        @Test
        fun `wednesday returns previous monday`() {
            val wednesday = LocalDate.of(2026, 2, 18)
            val expected = LocalDate.of(2026, 2, 16)
            assertEquals(expected, wednesday.startOfWeek())
        }

        @Test
        fun `sunday returns previous monday`() {
            val sunday = LocalDate.of(2026, 2, 22)
            val expected = LocalDate.of(2026, 2, 16)
            assertEquals(expected, sunday.startOfWeek())
        }

        @Test
        fun `start of week is always Monday`() {
            val anyDate = LocalDate.of(2026, 3, 5)
            assertEquals(DayOfWeek.MONDAY, anyDate.startOfWeek().dayOfWeek)
        }
    }

    @Nested
    @DisplayName("endOfWeek()")
    inner class EndOfWeek {

        @Test
        fun `sunday returns itself`() {
            val sunday = LocalDate.of(2026, 2, 22)
            assertEquals(sunday, sunday.endOfWeek())
        }

        @Test
        fun `monday returns next sunday`() {
            val monday = LocalDate.of(2026, 2, 16)
            val expected = LocalDate.of(2026, 2, 22)
            assertEquals(expected, monday.endOfWeek())
        }

        @Test
        fun `end of week is always Sunday`() {
            val anyDate = LocalDate.of(2026, 3, 5)
            assertEquals(DayOfWeek.SUNDAY, anyDate.endOfWeek().dayOfWeek)
        }
    }

    @Nested
    @DisplayName("toDisplayString()")
    inner class ToDisplayString {

        @Test
        fun `formats as dd MMM yyyy in Indonesian`() {
            val date = LocalDate.of(2026, 2, 22)
            val result = date.toDisplayString()
            assertTrue(result.contains("22"))
            assertTrue(result.contains("2026"))
        }
    }

    @Nested
    @DisplayName("toIsoString()")
    inner class ToIsoString {

        @Test
        fun `returns ISO 8601 format`() {
            val date = LocalDate.of(2026, 2, 22)
            assertEquals("2026-02-22", date.toIsoString())
        }
    }
}
