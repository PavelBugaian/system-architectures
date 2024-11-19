package utm.ass.process.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utm.ass.process.domain.FileRecordDto
import utm.ass.process.domain.GetImageRecord
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun Application.configureRouting() {

    val getImageRecord = GetImageRecord()
    val imageProcessor = ImageProcessor()

    routing {
        post("process") {
            val fileEntry = call.receive<FileEntry>()

            val fileRecordDto: FileRecordDto = getImageRecord.execute(fileEntry.id)

            log.debug(fileEntry.toString())

            val command = when (fileEntry.type) {
                ProcessType.MIRROR -> MirrorCommand()
                ProcessType.NEGATIVE -> NegativeCommand()
                ProcessType.CROP -> CropCommand(
                    fileEntry.x ?: return@post call.respondText(
                        "Not enough params", status = HttpStatusCode.BadRequest
                    ),
                    fileEntry.y ?: return@post call.respondText(
                        "Not enough params", status = HttpStatusCode.BadRequest
                    ),
                    fileEntry.width ?: return@post call.respondText(
                        "Not enough params", status = HttpStatusCode.BadRequest
                    ),
                    fileEntry.height ?: return@post call.respondText(
                        "Not enough params", status = HttpStatusCode.BadRequest
                    ),
                )

                ProcessType.SEPIA -> SepiaCommand()
            }

            File(fileRecordDto.path).apply {
                if (!exists()) {
                    return@post call.respondText("File not found", status = HttpStatusCode.NotFound)
                }
                imageProcessor.addCommand(command)
                val image = imageProcessor.process(ImageIO.read(this))
                val newFile = File("processed_$name").apply { ImageIO.write(image, "png", this) }
                call.respondFile(newFile)
            }
        }
    }
}

@Serializable
data class FileEntry(
    val type: ProcessType,
    val id: Int,
    val x: Int?,
    val y: Int?,
    val width: Int?,
    val height: Int?,
    val horizontally: Boolean?,
)

@Serializable
enum class ProcessType {
    @SerialName("mirror")
    MIRROR,
    @SerialName("negative")
    NEGATIVE,
    @SerialName("crop")
    CROP,
    @SerialName("sepia")
    SEPIA;
}

interface ImageCommand {
    fun execute(image: BufferedImage): BufferedImage
}

class SepiaCommand : ImageCommand {
    override fun execute(image: BufferedImage): BufferedImage {
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
}

class CropCommand(private val x: Int, private val y: Int, private val width: Int, private val height: Int) :
    ImageCommand {
    override fun execute(image: BufferedImage): BufferedImage {
        return image.getSubimage(x, y, width, height)
    }
}

class NegativeCommand : ImageCommand {
    override fun execute(image: BufferedImage): BufferedImage {
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
}

class MirrorCommand(private val horizontally: Boolean = true) : ImageCommand {
    override fun execute(image: BufferedImage): BufferedImage {
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
}

class ImageProcessor() {
    private val commands = mutableListOf<ImageCommand>()

    fun addCommand(command: ImageCommand) {
        commands.add(command)
    }

    fun process(image: BufferedImage): BufferedImage {
        var currentImage = image
        commands.forEach { command ->
            currentImage = command.execute(currentImage)
        }
        commands.clear()
        return currentImage
    }
}