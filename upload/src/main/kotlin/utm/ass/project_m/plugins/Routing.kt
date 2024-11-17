package utm.ass.project_m.plugins

import io.ktor.server.application.*
import utm.ass.project_m.presentation.configureUploadRouting

fun Application.configureRouting() {
    configureUploadRouting()
}
