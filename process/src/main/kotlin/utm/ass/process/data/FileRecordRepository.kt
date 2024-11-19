package utm.ass.process.data

import io.ktor.client.call.body
import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.Serializable
import utm.ass.process.domain.FileRecordDto

class FileRecordRepository {
    val dataSource = HttpDataSource("file")

    suspend fun retrieveFileRecord(id: Int): FileRecordDto {
        val response = dataSource.get(mapOf("id" to id))
        return response.body<FileRecordResponse>(TypeInfo(FileRecordResponse::class)).toDto()
    }
}

private fun FileRecordResponse.toDto(): FileRecordDto = FileRecordDto(name, path, description)

@Serializable()
data class FileRecordResponse(
    val name: String,
    val path: String,
    val description: String
)
