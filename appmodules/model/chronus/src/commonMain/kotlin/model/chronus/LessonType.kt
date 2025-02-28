package model.chronus

sealed interface LessonType {
	data object Lecture : LessonType // лекция

	data object Practice : LessonType // практика

	data object LabWork : LessonType // лабораторная работа

	data object Project : LessonType // проектная деятельность

	data object Exam : LessonType // экзамен

	data object CourseCredit : LessonType // зачёт

	data object Consultation : LessonType // консультация

	data class Other(val name: String = "") : LessonType // любой другой тип
}

fun LessonType.Other.correctNameOrNull(): String? =
	this.name.replaceFirstChar { it.uppercase() }.takeIf { it.isNotBlank() }

fun LessonType.asString(): String? = when (this) {
	LessonType.Lecture -> "Лекция"
	LessonType.Practice -> "Практика"
	LessonType.LabWork -> "Лабораторная работа"
	LessonType.Project -> "Проектная деятельность"
	LessonType.Exam -> "Экзамен"
	LessonType.CourseCredit -> "Зачёт"
	LessonType.Consultation -> "Консультация"
	is LessonType.Other -> correctNameOrNull()
}

fun String?.asLessonType(): LessonType = when (this?.trim()?.lowercase()) {
	// обратите внимание: тип должен быть указан маленькими буквами

	"лекция",
	-> LessonType.Lecture
	"практика",
	"практическое занятие", // БГУ Иркутск
	"практические занятия", // БГУ Усть-Илимск
	"практические (семинарские) занятия", // ИРГУПС
	-> LessonType.Practice
	"лабораторная работа",
	"лабораторная", // ИГУ
	"лаб.-практич.занятия", // БГУ Усть-Илимск
	-> LessonType.LabWork
	"проект",
	"проектная деятельность",
	-> LessonType.Project
	"экзамен",
	"экзамены",
	-> LessonType.Exam
	"зачет",
	"зачёт",
	-> LessonType.CourseCredit
	"консультация",
	-> LessonType.Consultation
	else -> LessonType.Other(this?.trim() ?: "")

	// если ни один LessonType не подходит к некоторым типам занятия в вашем ВУЗе,
	// оставьте эти типы ниже, я обработаю их самостоятельно:
	// "ваш_неподходящий_тип_1", "ваш_неподходящий_тип_2"
}
