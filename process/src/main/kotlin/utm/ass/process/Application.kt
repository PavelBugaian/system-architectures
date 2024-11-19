package utm.ass.process

import io.ktor.server.application.*
import utm.ass.process.plugins.configureRouting
import utm.ass.process.plugins.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}
