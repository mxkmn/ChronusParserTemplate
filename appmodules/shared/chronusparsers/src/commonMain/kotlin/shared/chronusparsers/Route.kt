package shared.chronusparsers

import kotlinx.serialization.Serializable
import model.chronus.Contributor
import model.chronus.Place
import model.chronus.Schedule

@Serializable
internal sealed interface Route {
	@Serializable
	data object Places : Route // home screen

	@Serializable
	data class Search(val place: Place) : Route

	@Serializable
	data class Lessons(val schedule: Schedule) : Route

	@Serializable
	data class AboutContributor(val contributor: Contributor) : Route
}
