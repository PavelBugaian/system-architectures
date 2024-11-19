package utm.ass.process.domain

import kotlinx.serialization.Serializable
import utm.ass.process.data.FileRecordRepository

class GetImageRecord {

    private val repository = FileRecordRepository()

    suspend fun execute(fileId: Int): FileRecordDto {
        return repository.retrieveFileRecord(fileId)
    }
}

@Serializable()
data class FileRecordDto(
    val name: String,
    val path: String,
    val description: String,
)