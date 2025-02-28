package library.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun HorizontalSpacer(height: Dp, modifier: Modifier = Modifier) {
	Spacer(modifier.height(height))
}
