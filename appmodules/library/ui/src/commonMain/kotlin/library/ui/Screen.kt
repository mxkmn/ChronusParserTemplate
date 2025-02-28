package library.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Screen(
	title: String,
	onBackClick: (() -> Unit)?,
	modifier: Modifier = Modifier,
	actions: @Composable RowScope.() -> Unit = {},
	content: LazyListScope.() -> Unit,
) {
	Scaffold(
		modifier = modifier,
		contentWindowInsets = WindowInsets(0.dp), // disables padding for navbar (enabled by default in contentPadding)
		topBar = {
			@OptIn(ExperimentalMaterial3Api::class)
			TopAppBar(
				title = { Text(title) },
				navigationIcon = {
					if (onBackClick != null) {
						IconButton(onClick = onBackClick) {
							Icon(
								imageVector = Icons.AutoMirrored.Default.ArrowBack,
								contentDescription = "Назад",
							)
						}
					}
				},
				actions = actions,
			)
		},
	) { paddingValues ->
		LazyColumn(
			modifier = Modifier.fillMaxWidth().padding(paddingValues),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			item {
				HorizontalSpacer(8.dp)
			}
			content()
			item {
				HorizontalSpacer(8.dp + WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding())
			}
		}
	}
}
