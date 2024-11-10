package utm.ass.project_m.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    routing {
        swaggerUI(path = "openapi", swaggerFile = "upload/src/main/resources/openapi/documentation.yaml.kt")
    }
}
