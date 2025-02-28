package library.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.innerCardPadding(
	horizontal: Dp = 16.dp,
	vertical: Dp = 8.dp,
) = this.padding(horizontal = horizontal, vertical = vertical)
