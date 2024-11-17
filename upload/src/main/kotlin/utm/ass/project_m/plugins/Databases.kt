package utm.ass.project_m.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import utm.ass.project_m.data.FileDto
import utm.ass.project_m.data.FileService

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres()
    val fileService = FileService(dbConnection)

    routing {
        // Create file
        post("/files") {
            val file = call.receive<FileDto>()
            val id = fileService.create(file)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read file
        get("/files/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val file = fileService.read(id)
                call.respond(HttpStatusCode.OK, file)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update file
        put("/files/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val file = call.receive<FileDto>()
            fileService.update(id, file)
            call.respond(HttpStatusCode.OK)
        }

        // Delete file
        delete("/files/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            fileService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Application.connectToPostgres(): Connection {
    Class.forName("org.postgresql.Driver")
    val url = environment.config.property("postgres.url").getString()
    val user = environment.config.property("postgres.user").getString()
    val password = environment.config.property("postgres.password").getString()

    return DriverManager.getConnection(url, user, password)
}
