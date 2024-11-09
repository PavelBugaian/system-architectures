package utm.ass.project_m

import io.ktor.server.application.*
import utm.ass.project_m.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureFrameworks()
    configureRouting()
}
