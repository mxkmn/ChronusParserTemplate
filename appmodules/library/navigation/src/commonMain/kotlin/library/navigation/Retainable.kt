package library.navigation

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * Предоставление доступа к State и CoroutineScope, которые должны сохраняться
 * при изменениях кофигурации и удаляться при уничтожении экрана. Предоставляет
 * основные возможности Jetpack ViewModel:
 *
 * @param onDestroy aka onCleared - вызывается при уничтожении экрана
 * @property scope aka viewModelScope - уничтожает работу зависимых Job при уничтожении экрана
 */
class Retainable<State : Any>(
	instanceKeeper: InstanceKeeper,
	defaultState: State,
	onDestroy: (Value<State>, CoroutineScope) -> Unit = { _, _ -> }, // aka onCleared from Jetpack ViewModel
) {
	private val stateAndScope = instanceKeeper.getOrCreate { StateAndScope(defaultState, onDestroy) }

	val scope = stateAndScope.scope
	val observableState: Value<State> = stateAndScope.state
	val state: State get() = stateAndScope.state.value

	fun updateState(function: (State) -> State) = stateAndScope.state.update(function)
}

private class StateAndScope<T : Any>(
	defaultState: T,
	private val onCleared: (Value<T>, CoroutineScope) -> Unit,
) : InstanceKeeper.Instance {
	val state = MutableValue(defaultState)
	val scope = CoroutineScope(Job() + Dispatchers.Main.immediate)

	override fun onDestroy() {
		onCleared(state, scope)

		scope.cancel()
	}
}
