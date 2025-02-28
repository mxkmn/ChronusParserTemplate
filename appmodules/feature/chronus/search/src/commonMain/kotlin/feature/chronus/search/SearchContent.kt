package feature.chronus.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import library.ui.Screen
import library.ui.screenElementPadding
import model.chronus.ProgressStatus
import model.chronus.SearchType

private const val FIND_PROBLEMS_DESCRIPTION = "Будут проверены все найденные расписания на " +
	"наличие ошибок, а также на наличие необработанных LessonType.\n\n" +

	"Эта автоматическая проверка направлена на быстрое нахождение проблем при парсинге и на " +
	"необходимость расширения String?.asLessonType(). Вы всё ещё должны проверять соответствие " +
	"полученных данных с данными на сайте вручную (перейдите в найденное расписание).\n\n" +

	"Ни одного расписания не должно появиться в problemSchedules. Корректно обработанные " +
	"расписания, но без занятий (группа перестала существовать, препод уволился, каникулы, " +
	"и т.д.) должны выдавать пустой List.\n\n" +

	"strangeTypes - типы, не найденные в String?.asLessonType(). Расширьте эту функцию."

@Composable
fun SearchContent(
	component: SearchComponent,
	modifier: Modifier = Modifier,
) {
	val state by component.retainable.observableState.subscribeAsState()

	Screen(
		modifier = modifier,
		onBackClick = { component.onFinish() },
		title = "${state.place.cyrillicName}: поиск",
		actions = {
			AnimatedVisibility(state.foundSchedules.isNotEmpty()) {
				OutlinedButton(onClick = component::showProblemsDialog) {
					Text("Найти проблемы")
				}
			}
		},
	) {
		item {
			if (state.searchStatus != ProgressStatus.LOADING) {
				val isSearchPossible =
					state.input.trim().length >= (state.place.minSearchChars ?: 0) &&
						state.searchStatus != ProgressStatus.SUCCESS

				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Center,
					modifier = Modifier.screenElementPadding(),
				) {
					OutlinedTextField(
						value = state.input,
						onValueChange = component::setInput,
						label = {
							Text(
								when (state.place.searchType) {
									SearchType.BY_GROUP_PERSON_PLACE -> "Группа, фамилия или аудитория"
									SearchType.BY_GROUP_PERSON -> "Группа или фамилия"
									SearchType.BY_GROUP_PLACE -> "Группа или аудитория"
									SearchType.BY_GROUP -> "Учебная группа"
								},
							)
						},
						placeholder = {
							Text(state.place.minSearchChars?.let { "Минимум $it символ(-а)" } ?: "")
						},
						keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
						keyboardActions = KeyboardActions(onSearch = { if (isSearchPossible) component.search() }),
						modifier = Modifier.weight(1f).onPreviewKeyEvent {
							if (it.key == Key.Enter) {
								if (isSearchPossible) {
									component.search()
								}
								true
							} else {
								false
							}
						},
					)
					AnimatedVisibility(isSearchPossible) {
						OutlinedIconButton(onClick = component::search, Modifier.padding(start = 8.dp)) {
							Icon(Icons.Default.Search, null)
						}
					}
				}
			} else {
				CircularProgressIndicator()
			}
		}
		item {
			if (state.searchStatus == ProgressStatus.ERROR) {
				Text("Ошибка при поиске")
			} else if (state.searchStatus == ProgressStatus.SUCCESS && state.foundSchedules.isEmpty()) {
				Text("Ничего не найдено")
			}
		}
		items(state.foundSchedules) {
			OutlinedButton(
				onClick = { component.onScheduleChoose(it) },
				modifier = Modifier.padding(vertical = 8.dp),
			) {
				Column {
					Text(it.name)
					Text(it.type.toString(), style = MaterialTheme.typography.bodySmall)
					Text(it.url, style = MaterialTheme.typography.bodySmall)
				}
			}
		}
	}

	if (state.isFindProblemsDialogShown) {
		AlertDialog(
			properties = DialogProperties(usePlatformDefaultWidth = false),
			modifier = Modifier.screenElementPadding(),
			icon = { Icon(Icons.Default.Warning, null) },
			text = {
				Text(
					text = state.problems ?: FIND_PROBLEMS_DESCRIPTION,
					modifier = Modifier.verticalScroll(rememberScrollState()),
				)
			},
			confirmButton = {
				if (state.problems == null) {
					TextButton(
						content = { Text("Запустить проверку") },
						onClick = component::findProblems,
					)
				}
			},
			dismissButton = {
				TextButton(
					content = { Text("Скрыть") },
					onClick = component::hideProblemsDialogAndClearProblems,
					enabled = state.problems?.endsWith('\n') != false,
				)
			},
			onDismissRequest = {},
		)
	}
}
