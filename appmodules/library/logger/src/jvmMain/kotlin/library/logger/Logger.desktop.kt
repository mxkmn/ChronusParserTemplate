package library.logger

import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.Log

actual fun installLogger() = Log.dispatcher.install(ConsoleLogger)
