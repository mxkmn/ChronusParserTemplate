package feature.chronus.search

import com.arkivanov.decompose.ComponentContext
import datasource.network.chronus.main.ChronusNetwork
import kotlinx.coroutines.launch
import library.logger.LogType
import library.logger.log
import library.navigation.Retainable
import model.chronus.LessonType
import model.chronus.Place
import model.chronus.ProgressStatus
import model.chronus.Schedule
import model.common.asyncMap

class SearchComponent(
	componentContext: ComponentContext,
	place: Place,
	val onScheduleChoose: (Schedule) -> Unit,
	val onFinish: () -> Unit,
) : ComponentContext by componentContext {
	data class State(
		val place: Place,
		val input: String = "",
		val searchStatus: ProgressStatus = ProgressStatus.NOT_USED,
		val foundSchedules: List<Schedule> = emptyList(),
		val isFindProblemsDialogShown: Boolean = false,
		val problems: String? = null,
	)

	val retainable = Retainable(
		instanceKeeper = instanceKeeper,
		defaultState = State(place = place),
	)

	init {
		if (place.minSearchChars == null) {
			search() // starts search when screen is opened
		}
	}

	fun setInput(string: String) {
		retainable.updateState {
			it.copy(
				input = string,
				searchStatus = ProgressStatus.NOT_USED,
				foundSchedules = emptyList(),
			)
		}
	}

	fun search() {
		retainable.scope.launch {
			retainable.updateState { it.copy(searchStatus = ProgressStatus.LOADING) }

			val getAllEntries = retainable.state.place.minSearchChars == null
			val query = if (getAllEntries) "" else retainable.state.input
			val searchResults = ChronusNetwork.getSearchResults(retainable.state.place, query)
				?.let { results ->
					if (getAllEntries) { // если с сервера пришло всё и требуется фильтрация на клиенте
						results.filter { result -> result.name.contains(retainable.state.input, ignoreCase = true) }
					} else { // если фильтрация была на сервере
						results
					}
				}

			retainable.updateState { capturedState ->
				if (searchResults != null) {
					capturedState.copy(
						searchStatus = ProgressStatus.SUCCESS,
						foundSchedules = searchResults.sortedBy { it.name }.sortedBy { it.type },
					)
				} else {
					capturedState.copy(
						searchStatus = ProgressStatus.ERROR,
						foundSchedules = emptyList(),
					)
				}
			}
		}
	}

	fun showProblemsDialog() {
		retainable.updateState { it.copy(isFindProblemsDialogShown = true) }
	}

	fun hideProblemsDialogAndClearProblems() {
		retainable.updateState { it.copy(problems = null, isFindProblemsDialogShown = false) }
	}

	fun findProblems() {
		val schedules = retainable.state.foundSchedules

		retainable.scope.launch {
			val strangeTypes = mutableSetOf<String>()
			val problemSchedules = mutableListOf<String>()

			log(LogType.NonProductionCodeDebug, "findProblems started for ${schedules.size} items")
			retainable.updateState { it.copy(problems = "findProblems started for ${schedules.size} items") }

			val limit = 25 // to prevent GC problems
			val startStep = 1 // if problems found, set last step from console instead of 1
			val steps = schedules.size / limit + 1
			for (step in startStep - 1..<steps) {
				log(
					LogType.NonProductionCodeDebug,
					"findProblems ${step + 1}/$steps:" +
						"\n  problemSchedules = $problemSchedules\n  strangeTypes = $strangeTypes",
				)
				retainable.updateState {
					it.copy(
						problems = it.problems + "\n\nfindProblems ${step + 1}/$steps:" +
							"\n  problemSchedules = $problemSchedules\n  strangeTypes = $strangeTypes",
					)
				}

				val start = 0 + step * limit
				val end = minOf(start + limit, schedules.size)

				schedules.slice(start..<end).asyncMap { schedule ->
					val result = ChronusNetwork.getLessons(schedule)
					if (result == null) {
						problemSchedules += schedule.name
					} else {
						result.forEach { lesson ->
							(lesson.type as? LessonType.Other)?.name?.takeIf { it.isNotBlank() }
								?.let { strangeTypes += "\"${it.lowercase()}\"" }
						}
					}
				}
			}

			log(
				LogType.NonProductionCodeDebug,
				"findProblems end:\n    problemSchedules = $problemSchedules\n    strangeTypes = $strangeTypes",
			)
			retainable.updateState {
				it.copy(
					problems = it.problems + "\n\nfindProblems end:" +
						"\n  problemSchedules = $problemSchedules\n  strangeTypes = $strangeTypes\n",
				)
			}
		}
	}
}
