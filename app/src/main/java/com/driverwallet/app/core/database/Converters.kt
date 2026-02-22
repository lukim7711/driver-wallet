package com.driverwallet.app.core.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val localDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val zonedFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? =
        date?.format(localDateFormatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, localDateFormatter) }

    @TypeConverter
    fun fromZonedDateTime(zdt: ZonedDateTime?): String? =
        zdt?.format(zonedFormatter)

    @TypeConverter
    fun toZonedDateTime(value: String?): ZonedDateTime? =
        value?.let { ZonedDateTime.parse(it, zonedFormatter) }
}
