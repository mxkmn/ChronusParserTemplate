package datasource.network.chronus.main

import com.fleeksoft.charset.Charsets
import com.fleeksoft.charset.decodeToString
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes

/**
 * Use it if you get broken Russian characters with standard `.body()` (it happens when server
 * doesn't send `<meta charset="windows-1251">` in HTML code, but uses this encoding).
 */
suspend fun HttpResponse.bodyWithBadRussianEncoding(): String =
	this.bodyAsBytes().decodeToString(Charsets.forName("Windows-1251"))
