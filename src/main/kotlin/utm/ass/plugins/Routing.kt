package utm.ass.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File

fun Application.configureRouting() {
    routing {
        post("process") {
            val uploadsDirectory = File("uploads\\\\")

            // get file from body
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        val fileDescription = part.value
                    }

                    is PartData.FileItem -> {
                        val fileName = part.originalFileName as String
                        val fileBytes = part.provider().readRemaining().readByteArray()
                        if (!uploadsDirectory.exists()) {
                            uploadsDirectory.mkdir()
                        }
                        val newFile = File(uploadsDirectory, fileName).apply {
                            if (exists()) {
                                call.respondText("File already exists", status = HttpStatusCode.BadRequest)
                            }
                        }
                        newFile.writeBytes(fileBytes)

                        // upload file to upload service here
                        call.upload(newFile, uploadsDirectory)
                    }

                    else -> {}
                }
                part.dispose()
            }
        }

    }
}

suspend fun RoutingCall.upload(file: File, directory: File) {
    File(directory, file.name).apply {
        if (exists()) {
            val image = javax.imageio.ImageIO.read(this)
            val mirroredImage = mirrorImage(image)
            val newFile = File(directory, "mirrored_${file.name}")
            javax.imageio.ImageIO.write(mirroredImage, "png", newFile)
            respondFile(newFile)
        } else {
            respondText(
                "File not found",
                status = HttpStatusCode.NotFound
            )
        }
    }

}

fun mirrorImage(image: BufferedImage, horizontally: Boolean = true): BufferedImage {
    val mirrored = BufferedImage(image.width, image.height, image.type)
    val graphics = mirrored.createGraphics()
    val transform = if (horizontally) {
        AffineTransform(-1.0, 0.0, 0.0, 1.0, image.width.toDouble(), 0.0)
    } else {
        AffineTransform(1.0, 0.0, 0.0, -1.0, 0.0, image.height.toDouble())
    }
    graphics.drawImage(image, transform, null)
    graphics.dispose()
    return mirrored
}

fun negativeImage(image: BufferedImage): BufferedImage {
    val negative = BufferedImage(image.width, image.height, image.type)
    val graphics = negative.createGraphics()
    graphics.drawImage(image, 0, 0, null)
    graphics.dispose()
    for (x in 0 until negative.width) {
        for (y in 0 until negative.height) {
            val color = negative.getRGB(x, y)
            val alpha = color shr 24 and 0xff
            val red = 255 - color shr 16 and 0xff
            val green = 255 - color shr 8 and 0xff
            val blue = 255 - color and 0xff
            val newColor = alpha shl 24 or (red shl 16) or (green shl 8) or blue
            negative.setRGB(x, y, newColor)
        }
    }
    return negative
}

fun cropImage(image: BufferedImage, x: Int, y: Int, width: Int, height: Int): BufferedImage {
    return image.getSubimage(x, y, width, height)
}

fun sepiaImage(image: BufferedImage): BufferedImage {
    val sepia = BufferedImage(image.width, image.height, image.type)
    val graphics = sepia.createGraphics()
    graphics.drawImage(image, 0, 0, null)
    graphics.dispose()
    for (x in 0 until sepia.width) {
        for (y in 0 until sepia.height) {
            val color = sepia.getRGB(x, y)
            val alpha = color shr 24 and 0xff
            var red = color shr 16 and 0xff
            var green = color shr 8 and 0xff
            var blue = color and 0xff
            val newRed = (red * 0.393 + green * 0.769 + blue * 0.189).toInt()
            val newGreen = (red * 0.349 + green * 0.686 + blue * 0.168).toInt()
            val newBlue = (red * 0.272 + green * 0.534 + blue * 0.131).toInt()
            red = if (newRed > 255) 255 else newRed
            green = if (newGreen > 255) 255 else newGreen
            blue = if (newBlue > 255) 255 else newBlue
            val newColor = alpha shl 24 or (red shl 16) or (green shl 8) or blue
            sepia.setRGB(x, y, newColor)
        }
    }
    return sepia
}


