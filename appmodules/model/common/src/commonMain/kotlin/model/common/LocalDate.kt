package model.common

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

fun LocalDate.Companion.current(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
	Clock.System.todayIn(timeZone)

fun LocalDate.Companion.current(timeZoneId: String) = Clock.System.todayIn(TimeZone.of(timeZoneId))

fun LocalDate.weekStartDay() = this.minus(this.dayOfWeek.ordinal, DateTimeUnit.DAY)

fun LocalDate.asLocalDateTime(hour: Int = 0, minute: Int = 0, second: Int = 0, nanosecond: Int = 0) =
	LocalDateTime(year, month, dayOfMonth, hour, minute, second, nanosecond)

fun LocalDate.asLocalDateTime(localTime: LocalTime) =
	asLocalDateTime(localTime.hour, localTime.minute, localTime.second, localTime.nanosecond)

fun LocalDate.weekOfYear(): Int {
	val currentYearStart = LocalDate(year = this.year, month = Month.JANUARY, dayOfMonth = 1)
	val dayOffset = currentYearStart.dayOfWeek.ordinal
	return (this.dayOfYear + dayOffset - 1 + 7) / 7
}

fun LocalDate.weekOfStudyYear(): Int {
	val thisWeekStartDate = this.minus(this.dayOfWeek.ordinal, DateTimeUnit.DAY)

	val startStudyYear = if (this.month.ordinal < Month.SEPTEMBER.ordinal) this.year - 1 else this.year

	val startStudyDate = LocalDate(startStudyYear, Month.SEPTEMBER, 1).let {
		val is1SepAtWeekend = it.dayOfWeek == DayOfWeek.SATURDAY || it.dayOfWeek == DayOfWeek.SUNDAY
		val startWeek = if (is1SepAtWeekend) it.plus(1, DateTimeUnit.WEEK) else it
		startWeek.minus(startWeek.dayOfWeek.ordinal, DateTimeUnit.DAY)
	}

	return (thisWeekStartDate.toEpochDays() - startStudyDate.toEpochDays()) / 7 + 1
}

/** Input pattern: "31.12.2024" */
fun LocalDate.Companion.parseRus(russianDate: String): LocalDate {
	val isoDate = russianDate.split('.').reversed().joinToString("-")
	return parse(isoDate)
}
