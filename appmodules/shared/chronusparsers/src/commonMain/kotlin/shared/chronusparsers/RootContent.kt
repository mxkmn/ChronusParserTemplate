package shared.chronusparsers

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import feature.chronus.contributor.ContributorContent
import feature.chronus.places.PlacesContent
import feature.chronus.schedule.ScheduleContent
import feature.chronus.search.SearchContent

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootContent(
	component: RootComponent,
	modifier: Modifier = Modifier,
) {
	MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
		Children(
			stack = component.stack,
			modifier = modifier.fillMaxSize(),
			animation = predictiveBackAnimation(
				fallbackAnimation = stackAnimation(fade() + scale()),
				onBack = component::onBack,
				backHandler = component.backHandler,
			),
		) { (_, instance) ->
			when (instance) {
				is Child.Places -> PlacesContent(component = instance.component)
				is Child.Search -> SearchContent(component = instance.component)
				is Child.Schedule -> ScheduleContent(component = instance.component)
				is Child.AboutContributor -> ContributorContent(component = instance.component)
			}
		}
	}
}
