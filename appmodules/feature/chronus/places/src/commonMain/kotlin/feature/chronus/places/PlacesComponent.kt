package feature.chronus.places

import com.arkivanov.decompose.ComponentContext
import model.chronus.Contributor
import model.chronus.Place

class PlacesComponent(
	componentContext: ComponentContext,
	val toSearch: (Place) -> Unit,
	val toContributor: (Contributor) -> Unit,
) : ComponentContext by componentContext
