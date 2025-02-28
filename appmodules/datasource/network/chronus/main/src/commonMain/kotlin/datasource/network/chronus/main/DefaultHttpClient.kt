package datasource.network.chronus.main

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun defaultHttpClient(json: Json): HttpClient = HttpClient {
	expectSuccess = true

	install(ContentNegotiation) { json(json) }

	install(HttpTimeout) {
		socketTimeoutMillis = 60_000
	}

	install(HttpRequestRetry) {
		maxRetries = 3
		retryIf { _, response -> !response.status.isSuccess() }
// 		retryOnExceptionIf { _, cause -> cause is NetworkErrorException }
		retryOnServerErrors()
		exponentialDelay()
	}
}
