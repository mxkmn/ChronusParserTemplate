package library.logger

import com.juul.khronicle.Log
import com.juul.khronicle.LogLevel
import com.juul.khronicle.WriteMetadata

// inline is needed to provide the true file name in the tag (if it's null)

enum class LogType(val logLevel: LogLevel) {
	IntentLaunchError(LogLevel.Error),
	NetworkClientError(LogLevel.Warn),
	ParseError(LogLevel.Warn),
	IOError(LogLevel.Warn),
	IORetry(LogLevel.Warn),
	ScheduledEvent(LogLevel.Info),
	SyncEvent(LogLevel.Info),
	ConcurrentLock(LogLevel.Debug),
	CornerCase(LogLevel.Debug),
	NonProductionCodeDebug(LogLevel.Debug),
	SensorInfo(LogLevel.Verbose),
	ParseResult(LogLevel.Verbose),
	NetworkRequestUrl(LogLevel.Verbose),
}

expect fun installLogger()

inline fun log(
	type: LogType,
	noinline message: (WriteMetadata?) -> String,
	throwable: Throwable? = null,
	tag: String? = null,
) {
	Log.dynamic(level = type.logLevel, throwable = throwable, tag = tag, message = message)
}

inline fun log(type: LogType, message: String, throwable: Throwable? = null, tag: String? = null) =
	log(type = type, message = { message }, throwable = throwable, tag = tag)

inline fun log(type: LogType, throwable: Throwable, tag: String? = null) =
	log(type = type, message = { throwable.toString() } /*, throwable = throwable*/, tag = tag)
