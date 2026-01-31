package ru.crmplatforma.solo.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Type Converters для Room.
 *
 * Room не умеет хранить сложные типы напрямую,
 * поэтому конвертируем их в строки.
 */
class Converters {

    // OffsetDateTime ↔ String (ISO-8601)
    @TypeConverter
    fun fromOffsetDateTime(value: OffsetDateTime?): String? {
        return value?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let { OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
    }

    // LocalDate ↔ String (ISO-8601)
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
    }
}
