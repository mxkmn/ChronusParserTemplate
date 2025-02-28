package datasource.network.chronus.yourcity.youruniversity

import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import library.logger.LogType
import library.logger.log
import model.chronus.Schedule

suspend fun getSearchResults(client: HttpClient, json: Json, query: String): List<Schedule>? {
	val response: String = try {
		error("Получение данных не реализовано")
	} catch (e: Exception) {
		log(LogType.NetworkClientError, e)
		return null
	}

	return try {
		withContext(Dispatchers.Default) { // Dispatchers.Default нужен для ускорение парсинга
			error("Парсинг не реализован")
		}
	} catch (e: Exception) {
		log(LogType.ParseError, e)
		return null
	}
}
