package app.chronusparsers.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import library.logger.installLogger
import shared.chronusparsers.RootComponent
import shared.chronusparsers.RootContent

fun main() {
	installLogger()
	val lifecycle = LifecycleRegistry()

	val root = runOnUiThread {
		RootComponent(
			componentContext = DefaultComponentContext(lifecycle = lifecycle),
		)
	}

	application {
		val windowState = rememberWindowState(width = 450.dp, height = 800.dp)

		LifecycleController(lifecycle, windowState)

		Window(
			onCloseRequest = ::exitApplication,
			state = windowState,
			title = "Chronus parsers",
		) {
			RootContent(root)
		}
	}
}
