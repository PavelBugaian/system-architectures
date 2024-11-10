package utm.ass.project_m.plugins

import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File

fun Application.configureRouting() {
    routing {
        get("/{id}") {
            call.respondText(call.parameters["id"] ?: "", contentType = ContentType.Text.Plain)
        }

        post("/upload") {
            val file = File("uploads/ktor_logo.png")
            call.receiveChannel().copyAndClose(file.writeChannel())
            call.respondText("A file is uploaded")
        }
    }
}
