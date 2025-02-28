package feature.chronus.places

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import library.ui.Screen
import library.ui.innerCardPadding
import library.ui.screenElementPadding
import model.chronus.Place

@Composable
fun PlacesContent(
	component: PlacesComponent,
	modifier: Modifier = Modifier,
) {
	val uriHandler = LocalUriHandler.current

	Screen(
		title = "Учебные заведения",
		onBackClick = null,
		modifier = modifier,
	) {
		items(items = Place.entries) {
			OutlinedCard(Modifier.screenElementPadding()) {
				Text(
					modifier = Modifier.innerCardPadding(),
					style = MaterialTheme.typography.headlineMedium,
					text = "${it.cyrillicName}, ${it.city.cyrillicName}",
				)
				OutlinedButton(
					onClick = { component.toSearch(it) },
					modifier = Modifier.innerCardPadding(),
				) {
					Text("Поиск")
				}
				OutlinedButton(
					onClick = { component.toContributor(it.contributor) },
					modifier = Modifier.innerCardPadding(),
				) {
					Text("Внедрил ${it.contributor.nickName}")
				}
				it.apiCredits?.let { (apiName, url) ->
					OutlinedButton(
						onClick = { uriHandler.openUri(url) },
						modifier = Modifier.innerCardPadding(),
					) {
						Text("На основе $apiName")
					}
				}
			}
		}
	}
}
