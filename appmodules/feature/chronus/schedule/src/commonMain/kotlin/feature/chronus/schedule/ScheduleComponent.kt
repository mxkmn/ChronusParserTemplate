package feature.chronus.schedule

import com.arkivanov.decompose.ComponentContext
import datasource.network.chronus.main.ChronusNetwork
import kotlinx.coroutines.launch
import library.navigation.Retainable
import model.chronus.Lesson
import model.chronus.ProgressStatus
import model.chronus.Schedule

class ScheduleComponent(
	componentContext: ComponentContext,
	schedule: Schedule,
	val onFinish: () -> Unit,
) : ComponentContext by componentContext {
	data class State(
		val schedule: Schedule,
		val searchStatus: ProgressStatus = ProgressStatus.NOT_USED,
		val foundLessons: List<Lesson> = emptyList(),
	)

	val retainable = Retainable(
		instanceKeeper = instanceKeeper,
		defaultState = State(schedule = schedule),
	)

	init {
		getLessons()
	}

	fun getLessons() {
		retainable.scope.launch {
			retainable.updateState { it.copy(searchStatus = ProgressStatus.LOADING) }

			val searchResults = ChronusNetwork.getLessons(retainable.state.schedule)

			retainable.updateState { capturedState ->
				if (searchResults != null) {
					capturedState.copy(
						searchStatus = ProgressStatus.SUCCESS,
						foundLessons = searchResults.sortedBy { it.startTime },
					)
				} else {
					capturedState.copy(searchStatus = ProgressStatus.ERROR)
				}
			}
		}
	}
}
