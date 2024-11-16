package utm.ass.project_m.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
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
