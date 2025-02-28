package app.chronusparsers.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import library.logger.installLogger
import shared.chronusparsers.RootComponent
import shared.chronusparsers.RootContent

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		installLogger()
		val root = RootComponent(componentContext = defaultComponentContext())

		setContent {
			RootContent(root)
		}
	}
}
