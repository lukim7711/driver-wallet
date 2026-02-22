package com.driverwallet.app.core.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val JAKARTA_ZONE = ZoneId.of("Asia/Jakarta")
private val DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
private val DISPLAY_DAY = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale("id", "ID"))

fun nowJakarta(): ZonedDateTime = ZonedDateTime.now(JAKARTA_ZONE)

fun todayJakarta(): LocalDate = LocalDate.now(JAKARTA_ZONE)

fun LocalDate.startOfWeek(): LocalDate =
    with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun LocalDate.endOfWeek(): LocalDate =
    with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

fun LocalDate.toDisplayString(): String = format(DISPLAY_DATE)

fun LocalDate.toDayDisplayString(): String = format(DISPLAY_DAY)

fun LocalDate.toIsoString(): String = toString()
