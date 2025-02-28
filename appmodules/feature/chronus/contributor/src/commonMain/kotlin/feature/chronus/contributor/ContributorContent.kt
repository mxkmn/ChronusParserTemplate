package feature.chronus.contributor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import library.logger.LogType
import library.logger.log
import library.ui.Screen
import library.ui.innerCardPadding
import library.ui.screenElementPadding
import model.chronus.ContributorAboutNameAndCards

@Composable
fun ContributorContent(
	component: ContributorComponent,
	modifier: Modifier = Modifier,
) {
	val model by component.retainable.observableState.subscribeAsState()

	Screen(
		modifier = modifier,
		onBackClick = { component.onFinish() },
		title = "Внедрение",
		actions = {
			OutlinedButton(onClick = component::onLangChange) {
				Text("Сменить язык")
			}
		},
	) {
		item {
			FullInfo(
				imageModel = model.contributor.contributorAbout.photoUrl,
				nickName = model.contributor.nickName.takeIf { it.isNotBlank() },
				contributions = model.contributor.contributions.takeIf { it > 1 },
				nameAndCards = model.contributor.contributorAbout.let { if (model.isRuLangUsed) it.ru else it.en },
			)
		}
	}
}

@Composable
private fun FullInfo(
	imageModel: Any?,
	nickName: String?,
	contributions: Int?,
	nameAndCards: ContributorAboutNameAndCards?,
) {
	val uriHandler = LocalUriHandler.current

	Column {
		Box(
			contentAlignment = Alignment.BottomStart,
			modifier = Modifier.screenElementPadding()
				.aspectRatio(1f) // square
				.clip(RoundedCornerShape(12.dp))
				.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
		) {
			AsyncImage(
				model = imageModel,
				contentDescription = null,
				placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
				error = ColorPainter(MaterialTheme.colorScheme.error),
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxWidth(),
				onError = { log(LogType.IOError, it.result.throwable) },
			)
			Column {
				ImageText(nickName, true)
				ImageText(nameAndCards?.name)
				ImageText(contributions?.let { "Внедрил $it учебных заведения" })
			}
		}
		nameAndCards?.cards?.forEach { card ->
			OutlinedCard(modifier = Modifier.screenElementPadding()) {
				Text(
					modifier = Modifier.innerCardPadding(),
					style = MaterialTheme.typography.headlineMedium,
					text = card.title,
				)
				Text(
					modifier = Modifier.innerCardPadding(),
					text = card.text,
				)
				card.buttons.forEach {
					OutlinedButton(
						modifier = Modifier.innerCardPadding(),
						onClick = { uriHandler.openUri(it.url) },
					) {
						Text(it.text)
					}
				}
			}
		}
	}
}

@Composable
private fun ImageText(text: String?, isLarge: Boolean = false) {
	if (text != null) {
		Text(
			text = text,
			style = if (isLarge) MaterialTheme.typography.displaySmall else LocalTextStyle.current,
			letterSpacing = if (isLarge) 2.sp else TextUnit.Unspecified,
			modifier = Modifier
				.background(MaterialTheme.colorScheme.surface)
				.padding(horizontal = 4.dp + (if (isLarge) 0.sp else 2.sp).toDp()),
			color = MaterialTheme.colorScheme.onSurface,
		)
	}
}

@Composable
private fun TextUnit.toDp(): Dp {
	val localDensity = LocalDensity.current

	return toDp(localDensity)
}

@Composable
private fun TextUnit.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }
