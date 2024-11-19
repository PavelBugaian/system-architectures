package utm.ass.process.data
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.gson.gson
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientBuilder private constructor() {
    private var enableLogging: Boolean = false
    private var jsonConfig: Json? = null

    companion object {
        fun builder(): HttpClientBuilder = HttpClientBuilder()
    }

    fun enableLogging(): HttpClientBuilder {
        this.enableLogging = true
        return this
    }

    fun setJsonConfig(json: Json): HttpClientBuilder {
        this.jsonConfig = json
        return this
    }

    fun build(): HttpClient {
        return HttpClient(CIO) {
            if (enableLogging) {
                install(Logging) {
                    level = LogLevel.ALL
                }
            }

            jsonConfig?.let {
                install(ContentNegotiation) {
                    json(it)
                    gson()
                }
            }
        }
    }
}