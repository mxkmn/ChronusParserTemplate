package feature.chronus.contributor

import com.arkivanov.decompose.ComponentContext
import library.navigation.Retainable
import model.chronus.Contributor

class ContributorComponent(
	componentContext: ComponentContext,
	contributor: Contributor,
	val onFinish: () -> Unit,
) : ComponentContext by componentContext {
	data class State(
		val isRuLangUsed: Boolean, // eng otherwise
		val contributor: Contributor,
	)

	val retainable = Retainable(
		instanceKeeper = instanceKeeper,
		defaultState = State(isRuLangUsed = true, contributor = contributor),
	)

	fun onLangChange() {
		retainable.updateState { it.copy(isRuLangUsed = !it.isRuLangUsed) }
	}
}
