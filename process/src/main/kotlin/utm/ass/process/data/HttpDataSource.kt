package utm.ass.process.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json

private const val URL = "http://file:8080"

class HttpDataSource(
    private val endpoint: String,
) {

    val httpClient = HttpClientBuilder.builder()
        .enableLogging()
        .setJsonConfig(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
        .build()

    suspend fun get(params: Map<String, Any>): HttpResponse {
        return httpClient.get("$URL/$endpoint") {
            params.map { (key, value) -> parameter(key, value) }
        }
    }
}