package feature.chronus.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.datetime.DayOfWeek
import library.ui.Screen
import library.ui.innerCardPadding
import library.ui.screenElementPadding
import model.chronus.EntryInfo
import model.chronus.Lesson
import model.chronus.ProgressStatus
import model.chronus.asString

@Composable
fun ScheduleContent(
	component: ScheduleComponent,
	modifier: Modifier = Modifier,
) {
	val state by component.retainable.observableState.subscribeAsState()
	var isDescriptionVisible by rememberSaveable { mutableStateOf(false) }

	Screen(
		title = "${state.schedule.name}: занятия",
		onBackClick = { component.onFinish() },
		actions = {
			OutlinedButton({ isDescriptionVisible = !isDescriptionVisible }) {
				Text(if (isDescriptionVisible) "Скрыть" else "Расширить")
			}
		},
		modifier = modifier,
	) {
		item {
			when (state.searchStatus) {
				ProgressStatus.LOADING -> {
					CircularProgressIndicator()
				}
				ProgressStatus.ERROR -> {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Text("Ошибка при поиске")
						OutlinedButton(onClick = component::getLessons) {
							Text("Загрузить заново")
						}
					}
				}
				ProgressStatus.SUCCESS -> {
					if (state.foundLessons.isEmpty()) {
						Text("Занятия не найдены")
					}
				}
				ProgressStatus.NOT_USED -> {}
			}
		}
		items(state.foundLessons) {
			val lesson = it.copy(
				groups = it.groups.sortAndRemoveProtocol(),
				subgroups = it.subgroups.sorted().toSet(),
				classrooms = it.classrooms.sortAndRemoveProtocol(),
				persons = it.persons.sortAndRemoveProtocol(),
			)
			OutlinedCard(modifier = Modifier.screenElementPadding()) {
				Text(
					text = "${lesson.startTime.date.dayOfWeek.asString()} ${lesson.startTime.date}   |   " +
						"${lesson.startTime.hour}:${twoDigitNum(lesson.startTime.minute)} (${lesson.durationInMinutes} минут)",
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier.innerCardPadding(),
				)
				Text(
					text = lesson.getTitle(),
					modifier = Modifier.innerCardPadding(),
				)
				AnimatedVisibility(isDescriptionVisible) {
					Text(
						text = lesson.getDescription(),
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.innerCardPadding(),
					)
				}
			}
		}
	}
}

private fun twoDigitNum(num: Int) = "${if (num > 9) "" else "0"}$num"

private fun DayOfWeek.asString(): String = when (this) {
	DayOfWeek.MONDAY -> "пн"
	DayOfWeek.TUESDAY -> "вт"
	DayOfWeek.WEDNESDAY -> "ср"
	DayOfWeek.THURSDAY -> "чт"
	DayOfWeek.FRIDAY -> "пт"
	DayOfWeek.SATURDAY -> "сб"
	DayOfWeek.SUNDAY -> "вс"
	else -> ""
}

private fun String.minimizeLessonType(): String = when (this) {
	"Лабораторная работа" -> "Лаб."
	"Лекция" -> "Лек."
	"Практика" -> "Пр."
	"Проектная деятельность" -> "Проект"
	"Экзамен" -> "Экз."
	"Консультация" -> "Конс."
	else -> this
}

private fun Lesson.getTitle() =
	(
		this.classrooms.takeIf { it.isNotEmpty() }
			?.let { "${it.joinToString(transform = { it.name }, separator = " / ") } | " } ?: ""
	) +
		(this.type.asString()?.let { "${it.minimizeLessonType()} | " } ?: "") +
		this.name +
		(this.subgroups.takeIf { it.isNotEmpty() }?.let { " (подгруппа ${it.joinToString(separator = " / ") })" } ?: "")

private fun Lesson.getDescription() = (
	when {
		(this.type.asString() != null && this.persons.size == 1) ->
			"${this.type.asString()}, ${this.persons.first().name}\n"
		this.type.asString() != null -> "${this.type.asString()}\n"
		this.persons.size == 1 -> "Преподаватель ${this.persons.first().calendarStyle()}"
		else -> ""
	} + when {
		(this.persons.size == 1 && this.persons.first().url != null && this.type.asString() != null) ->
			"\nСсылка на преподавателя: ${this.persons.first().url}\n"
		(this.persons.size > 1) ->
			"\nПреподаватели:\n" + this.persons.joinToString(separator = "") { it.calendarStyle() }
		else -> ""
	} + when {
		(this.classrooms.size == 1 && this.classrooms.first().url != null) ->
			"\nСсылка на аудиторию: ${this.classrooms.first().url}\n"
		else -> {
			val classroomsWithUrl = this.classrooms.filter { it.url != null }

			if (classroomsWithUrl.isNotEmpty()) {
				"\nАудитории${if (classroomsWithUrl.size == this.classrooms.size) "" else " с доступными ссылками"}:\n" +
					this.classrooms.joinToString(separator = "") { it.calendarStyle() }
			} else {
				""
			}
		}
	} + when (this.groups.size) {
		0 -> ""
		1 -> "\nГруппа ${this.groups.first().calendarStyle()}"
		else -> "\nГруппы:\n" + this.groups.joinToString(separator = "") { it.calendarStyle() }
	} + when {
		this.additionalInfo != null -> "\n${this.additionalInfo}"
		else -> ""
	}
).trim()

private fun Set<EntryInfo>.sortAndRemoveProtocol(): Set<EntryInfo> = this.map { (name, url) ->
	EntryInfo(
		name = name,
		url = when {
			url?.startsWith("https://") == true -> url.substringAfter("https://")
			url?.startsWith("http://") == true -> url.substringAfter("http://")
			else -> url
		},
	)
}.sortedBy { it.name }.toSet()

private fun EntryInfo.calendarStyle() = "${this.name}${this.url?.let { ": $it" } ?: ""}\n"
