package model.common

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

fun String.trimAndCapitalizeFirstChar(): String =
	this.trim().replaceFirstChar(Char::titlecase)

private val windows1251WebMapping = mapOf(
	'А' to "%C0",
	'Б' to "%C1",
	'В' to "%C2",
	'Г' to "%C3",
	'Д' to "%C4",
	'Е' to "%C5",
	'Ё' to "%A8",
	'Ж' to "%C6",
	'З' to "%C7",
	'И' to "%C8",
	'Й' to "%C9",
	'К' to "%CA",
	'Л' to "%CB",
	'М' to "%CC",
	'Н' to "%CD",
	'О' to "%CE",
	'П' to "%CF",
	'Р' to "%D0",
	'С' to "%D1",
	'Т' to "%D2",
	'У' to "%D3",
	'Ф' to "%D4",
	'Х' to "%D5",
	'Ц' to "%D6",
	'Ч' to "%D7",
	'Ш' to "%D8",
	'Щ' to "%D9",
	'Ъ' to "%DA",
	'Ы' to "%DB",
	'Ь' to "%DC",
	'Э' to "%DD",
	'Ю' to "%DE",
	'Я' to "%DF",
	'а' to "%E0",
	'б' to "%E1",
	'в' to "%E2",
	'г' to "%E3",
	'д' to "%E4",
	'е' to "%E5",
	'ё' to "%B8",
	'ж' to "%E6",
	'з' to "%E7",
	'и' to "%E8",
	'й' to "%E9",
	'к' to "%EA",
	'л' to "%EB",
	'м' to "%EC",
	'н' to "%ED",
	'о' to "%EE",
	'п' to "%EF",
	'р' to "%F0",
	'с' to "%F1",
	'т' to "%F2",
	'у' to "%F3",
	'ф' to "%F4",
	'х' to "%F5",
	'ц' to "%F6",
	'ч' to "%F7",
	'ш' to "%F8",
	'щ' to "%F9",
	'ъ' to "%FA",
	'ы' to "%FB",
	'ь' to "%FC",
	'э' to "%FD",
	'ю' to "%FE",
	'я' to "%FF",
)

fun String.encodeToWindows1251() = this.map { windows1251WebMapping[it] ?: it }.joinToString(separator = "")

fun String.asDayOfWeek(): DayOfWeek? = when (this.lowercase()) {
	"пн", "понедельник" -> DayOfWeek.MONDAY
	"вт", "вторник" -> DayOfWeek.TUESDAY
	"ср", "среда" -> DayOfWeek.WEDNESDAY
	"чт", "четверг" -> DayOfWeek.THURSDAY
	"пт", "пятница" -> DayOfWeek.FRIDAY
	"сб", "суббота" -> DayOfWeek.SATURDAY
	"вс", "воскресенье" -> DayOfWeek.SUNDAY
	else -> null // parsing problems
}

fun String.asMonth(): Month? = when (this) {
	"января" -> Month.JANUARY
	"февраля" -> Month.FEBRUARY
	"марта" -> Month.MARCH
	"апреля" -> Month.APRIL
	"мая" -> Month.MAY
	"июня" -> Month.JUNE
	"июля" -> Month.JULY
	"августа" -> Month.AUGUST
	"сентября" -> Month.SEPTEMBER
	"октября" -> Month.OCTOBER
	"ноября" -> Month.NOVEMBER
	"декабря" -> Month.DECEMBER
	else -> null
}

/** Supported patterns: "23:59:59" or "23:59" */
fun String.asLocalTime(): LocalTime? {
	val numbers = this.split(':').map { it.toIntOrNull() ?: return null }
	return when (numbers.size) {
		3 -> LocalTime(numbers[0], numbers[1], numbers[2])
		2 -> LocalTime(numbers[0], numbers[1])
		else -> null
	}
}

/** Supported pattern: "31 декабря 2024" */
fun String.asLocalDate(): LocalDate? {
	val date = this.split(' ')
	val day = date.getOrNull(0)?.toIntOrNull()
	val month = date.getOrNull(1)?.asMonth()
	val year = date.getOrNull(2)?.toIntOrNull()

	return if (day != null && month != null && year != null) LocalDate(year, month, day) else null
}
