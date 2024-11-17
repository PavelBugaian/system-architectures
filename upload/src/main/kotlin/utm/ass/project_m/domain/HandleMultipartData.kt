package utm.ass.project_m.domain

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import utm.ass.project_m.plugins.UploadedFileData

class HandleMultipartData {
    suspend fun execute(call: RoutingCall): UploadedFileData {
        val multipartData = call.receiveMultipart()

        var description: String? = null
        var name: String? = null
        var data: ByteArray? = null

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when(part.name) {
                        "description" -> description = part.value
                    }
                }

                is PartData.FileItem -> {
                    name = part.originalFileName as String
                    data = (data ?: byteArrayOf()) + part.provider().readRemaining().readByteArray()
                }
                else -> {}
            }
            part.dispose()
        }

        if (description == null || name == null || data == null) {
            println("description: $description, name $name, data $data")
            call.respondText("Missing data", status = HttpStatusCode.BadRequest)
            throw Exception("Missing data")
        }

        return UploadedFileData(name, description, data)
    }
}
