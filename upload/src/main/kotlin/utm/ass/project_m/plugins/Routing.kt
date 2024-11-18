package utm.ass.project_m.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import utm.ass.project_m.domain.GetFileRecordById
import utm.ass.project_m.domain.HandleMultipartData
import utm.ass.project_m.domain.UpdateFileRecord
import java.io.File
import kotlin.getValue

fun Application.configureRouting() {
    val updateFileRecord by inject<UpdateFileRecord>()
    val getFileRecordById by inject<GetFileRecordById>()
    val handleMultipartData by inject<HandleMultipartData>()

    val uploadsDirectory = File("/usr/src/app/uploads")
    if (!uploadsDirectory.exists()) {
        uploadsDirectory.mkdir()
    }

    routing {
        post("upload") {
            val (name, description, fileData) = handleMultipartData.execute(call)
            val file = File(uploadsDirectory, name).apply {
                if (exists()) {
                    call.respondText("File already exists", status = HttpStatusCode.BadRequest)
                }
                writeBytes(fileData)
            }

            log.debug("File uploaded: $name, $description, ${file.path}")
            val recordId = updateFileRecord.execute(name, file.path, description)
            call.respond(PostUploadResponse(id = recordId), TypeInfo(PostUploadResponse::class))
        }

        get("file/byName/{fileName}") {
            val fileName = call.parameters["fileName"] ?: return@get call.respondText(
                "Provide file name",
                status = HttpStatusCode.BadRequest
            )
            val uploadsDirectory = File("/usr/src/app/uploads")

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

        get("file") {
            val requestId = call.queryParameters["id"] ?: return@get call.respondText(
                "Provide file id",
                status = HttpStatusCode.BadRequest
            )
            val fileRecord = getFileRecordById.execute(requestId.toInt())
            call.respond(fileRecord, TypeInfo(UploadedFileData::class))
        }
    }
}

@Serializable()
data class PostUploadResponse(
    val id: Int,
)

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