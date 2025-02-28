package shared.chronusparsers

import feature.chronus.contributor.ContributorComponent
import feature.chronus.places.PlacesComponent
import feature.chronus.schedule.ScheduleComponent
import feature.chronus.search.SearchComponent

sealed class Child { // all screens in app
	class Places(val component: PlacesComponent) : Child()

	class Search(val component: SearchComponent) : Child()

	class Schedule(val component: ScheduleComponent) : Child()

	class AboutContributor(val component: ContributorComponent) : Child()
}
