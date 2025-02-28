package library.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.screenElementPadding(
	horizontal: Dp = 16.dp,
	vertical: Dp = 8.dp,
) = this.width(640.dp).padding(horizontal = horizontal, vertical = vertical)
