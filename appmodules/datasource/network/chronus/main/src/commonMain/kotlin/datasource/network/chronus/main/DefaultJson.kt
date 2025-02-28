package datasource.network.chronus.main

import kotlinx.serialization.json.Json

internal fun defaultJson() = Json { ignoreUnknownKeys = true }
