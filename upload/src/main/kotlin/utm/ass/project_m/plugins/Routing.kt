package utm.ass.project_m.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.*
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import java.awt.geom.AffineTransform
import java.io.File
import java.awt.image.BufferedImage

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

        get("process/{fileName}/negative") {
            val fileName = call.parameters["fileName"] ?: return@get call.respondText(
                "Provide file name",
                status = HttpStatusCode.BadRequest
            )
            val uploadsDirectory = File("uploads\\\\")

            File(uploadsDirectory, fileName).apply {
                if (exists()) {
                    val image = javax.imageio.ImageIO.read(this)
                    val negativeImage = negativeImage(image)
                    val newFile = File(uploadsDirectory, "negative_$fileName")
                    javax.imageio.ImageIO.write(negativeImage, "png", newFile)
                    call.respondFile(newFile)
                } else {
                    call.respondText(
                        "File not found",
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }

        post("process/{fileName}/crop") {
            val fileName = call.parameters["fileName"] ?: return@post call.respondText(
                "Provide file name",
                status = HttpStatusCode.BadRequest
            )
            val uploadsDirectory = File("uploads\\\\")

            File(uploadsDirectory, fileName).apply {
                if (exists()) {
                    val image = javax.imageio.ImageIO.read(this)
                    val parts = call.receiveParameters()
                    val x = parts["x"]?.toInt() ?: 0
                    val y = parts["y"]?.toInt() ?: 0
                    val width = parts["width"]?.toInt() ?: image.width
                    val height = parts["height"]?.toInt() ?: image.height
                    val croppedImage = cropImage(image, x, y, width, height)
                    val newFile = File(uploadsDirectory, "cropped_$fileName")
                    javax.imageio.ImageIO.write(croppedImage, "png", newFile)
                    call.respondFile(newFile)
                } else {
                    call.respondText(
                        "File not found",
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }

        post("process/{fileName}/sepia") {
            val fileName = call.parameters["fileName"] ?: return@post call.respondText(
                "Provide file name",
                status = HttpStatusCode.BadRequest
            )
            val uploadsDirectory = File("uploads\\\\")

            File(uploadsDirectory, fileName).apply {
                if (exists()) {
                    val image = javax.imageio.ImageIO.read(this)
                    val sepiaImage = sepiaImage(image)
                    val newFile = File(uploadsDirectory, "sepia_$fileName")
                    javax.imageio.ImageIO.write(sepiaImage, "png", newFile)
                    call.respondFile(newFile)
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
