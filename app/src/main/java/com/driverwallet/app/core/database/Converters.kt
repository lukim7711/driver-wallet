package com.driverwallet.app.core.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromZonedDateTime(dateTime: ZonedDateTime?): String? =
        dateTime?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

    @TypeConverter
    fun toZonedDateTime(value: String?): ZonedDateTime? =
        value?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) }
}
