package shared.chronusparsers

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import feature.chronus.contributor.ContributorComponent
import feature.chronus.places.PlacesComponent
import feature.chronus.schedule.ScheduleComponent
import feature.chronus.search.SearchComponent

class RootComponent(
	componentContext: ComponentContext,
) : BackHandlerOwner, ComponentContext by componentContext {
	private val navigation = StackNavigation<Route>()

	val stack: Value<ChildStack<*, Child>> = childStack(
		source = navigation,
		serializer = Route.serializer(),
		initialConfiguration = Route.Places,
		handleBackButton = true,
		childFactory = ::child,
	)

	fun onBack() = navigation.pop()

	private fun child(currentRoute: Route, childComponentContext: ComponentContext): Child = when (currentRoute) {
		is Route.Places -> Child.Places(
			PlacesComponent(
				componentContext = childComponentContext,
				toSearch = { place -> navigation.pushNew(Route.Search(place)) },
				toContributor = { contributor -> navigation.pushNew(Route.AboutContributor(contributor)) },
			),
		)

		is Route.Search -> Child.Search(
			SearchComponent(
				componentContext = childComponentContext,
				place = currentRoute.place,
				onScheduleChoose = { navigation.pushNew(Route.Lessons(it)) },
				onFinish = navigation::pop,
			),
		)

		is Route.Lessons -> Child.Schedule(
			ScheduleComponent(
				componentContext = childComponentContext,
				currentRoute.schedule,
				onFinish = navigation::pop,
			),
		)

		is Route.AboutContributor -> Child.AboutContributor(
			ContributorComponent(
				componentContext = childComponentContext,
				contributor = currentRoute.contributor,
				onFinish = navigation::pop,
			),
		)
	}
}
