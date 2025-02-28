package library.logger

import com.juul.khronicle.AppleSystemLogger
import com.juul.khronicle.Log

actual fun installLogger() = Log.dispatcher.install(AppleSystemLogger)
