package utm.ass.project_m

import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import utm.ass.project_m.data.FileService
import utm.ass.project_m.domain.GetFileRecordById
import utm.ass.project_m.domain.HandleMultipartData
import utm.ass.project_m.domain.UpdateFileRecord
import utm.ass.project_m.plugins.*
import java.sql.Connection
import java.sql.DriverManager

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        configureRouting()
        configureSerialization()
        configureDatabases()
        modules(initAppModules(this@module))
    }
}

fun connectToPostgres(config: AppConfig): Connection {
    Class.forName("org.postgresql.Driver")
    return with(config) {
        DriverManager.getConnection(url, user, password)
    }
}

fun provideAppConfig(application: Application): AppConfig {
    val config = application.environment.config

    return AppConfig(
        url = config.property("postgres.url").getString(),
        user = config.property("postgres.user").getString(),
        password = config.property("postgres.password").getString(),
    )
}

fun initAppModules(
    application: Application,
): Module = module {
    single { provideAppConfig(application) }
    single { connectToPostgres(get()) }
    single { FileService(get()) }
    single { HandleMultipartData() }
    single { UpdateFileRecord(get()) }
    single { GetFileRecordById(get()) }
}

data class AppConfig(
    val url: String,
    val user: String,
    val password: String,
)