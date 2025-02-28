package datasource.network.chronus.irkutsk.iguimit

import kotlinx.serialization.Serializable

/*
У нас два эндпоинта, и json'ы на них немного отличаются...

Это есть только на fillSchedule: { "groupName": "02223-ДБ", "typeSubjectId": 3 }
Это есть только на searchPairSTC: { "group": "02271-ДБ", "weekday": "Понедельник", "time": "10.10-11.40" }

Классический подход - создание отдельных Dto для каждого эндпоинта (это предпочтительный вариант если отличается всё,
но в нашем случае это бесполезный оверхед, поскольку почти все названия идентичны). Поэтому работаем с общими
переменными, не используем отличающиеся (timeId а не time, weekdayId а не weekday) по возможности, а если возможности
нет (group/groupName) - делаем дубли с параметром null по умолчанию - по null сможем распознать, что данные находятся
в соседней переменной.
*/

@Serializable
internal data class LessonDto(
	// weekday (не путать с weekdayId) нельзя использовать, так как он есть у searchPairSTC, но нет у fillSchedule
	val weekdayId: Int,
	// time (не путать с timeId) нельзя использовать, так как он есть у searchPairSTC, но нет у fillSchedule
	val	timeId: Int,
	val week: String, // "верхняя" == нечетная, "нижняя" == четная
	val	typeSubjectName: String,
	val	subjectId: Int,
	val	subjectName: String,
	val	group: String? = null, // для searchPairSTC
	val	groupName: String? = null, // для fillSchedule
	val	groupId: Int,
	val className: String?,
	val classroomId: Int,
	val	teacherName: String,
	val	teacherId: Int,
	val beginDatePairs: String,
	val endDatePairs: String,
)
