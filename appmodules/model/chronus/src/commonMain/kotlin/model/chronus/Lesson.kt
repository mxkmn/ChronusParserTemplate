package model.chronus

import kotlinx.datetime.LocalDateTime

data class Lesson(
	val name: String, // название предмета
	val type: LessonType = LessonType.Other(), // тип пары
	val startTime: LocalDateTime, // время и дата начала
	val durationInMinutes: Int,
	val groups: Set<EntryInfo> = emptySet(),
	val subgroups: Set<Int> = emptySet(), // подгруппа, отсутствует == общая
	val persons: Set<EntryInfo> = emptySet(),
	val classrooms: Set<EntryInfo> = emptySet(),
	val additionalInfo: String? = null, // дополнительная информация, которую выдаёт только ваш сервис расписания
)

/** У некоторых ВУЗов общие пары для нескольких групп, преподавателей, аудиторий или подгрупп прилетают в виде
 * отдельных записей - эта функция объединяет их для правильного представления.
 */
fun MutableList<Lesson>.addOrExtend(lesson: Lesson) {
	find {
		it.name == lesson.name && it.type == lesson.type && it.startTime == lesson.startTime &&
			it.durationInMinutes == lesson.durationInMinutes && it.additionalInfo == lesson.additionalInfo &&
			(it.persons == lesson.persons || it.classrooms == lesson.classrooms)
	}?.let {
		remove(it)
		add(
			it.copy(
				groups = it.groups + lesson.groups,
				subgroups = it.subgroups + lesson.subgroups,
				persons = it.persons + lesson.persons,
				classrooms = it.classrooms + lesson.classrooms,
			),
		)
	} ?: run {
		add(lesson)
	}
}
