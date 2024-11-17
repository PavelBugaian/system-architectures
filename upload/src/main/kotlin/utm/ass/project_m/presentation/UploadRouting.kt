package utm.ass.project_m.presentation

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureUploadRouting() {
    routing {
        get("download/{fileName}") {
            val fileName = call.parameters["fileName"] ?: return@get call.respondText(
                "Provide file name",
                status = HttpStatusCode.BadRequest
            )
            val uploadsDirectory = File("uploads\\\\")

            File(uploadsDirectory, fileName).apply {
                if (exists()) {
                    call.respondFile(this)
                } else {
                    call.respondText(
                        "File not found",
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }
    }


}