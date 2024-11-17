package utm.ass.project_m.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import utm.ass.project_m.domain.HandleMultipartData
import utm.ass.project_m.domain.UpdateFileRecord
import java.io.File
import kotlin.getValue

fun Application.configureRouting() {
    val updateFileRecord by inject<UpdateFileRecord>()
    val handleMultipartData by inject<HandleMultipartData>()

    val uploadsDirectory = File("uploads\\\\")
    if (!uploadsDirectory.exists()) {
        uploadsDirectory.mkdir()
    }

    routing {
        post("upload") {
            val (name, description, fileData) = handleMultipartData.execute(call)
            val file = File(uploadsDirectory.path + name).apply {
                if (exists()) {
                    call.respondText("File already exists", status = HttpStatusCode.BadRequest)
                }
                writeBytes(fileData)
            }

            log.debug("File uploaded: $name, $description, ${file.path}")

            updateFileRecord.execute(name, description, file.path)

            call.respondFile(file)
        }

        get("file/{fileName}") {
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

data class UploadedFileData(
    val name: String,
    val description: String,
    val path: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadedFileData

        if (name != other.name) return false
        if (description != other.description) return false
        if (!path.contentEquals(other.path)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + path.contentHashCode()
        return result
    }
}