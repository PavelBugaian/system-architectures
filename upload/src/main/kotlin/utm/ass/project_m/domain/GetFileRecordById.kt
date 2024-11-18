package utm.ass.project_m.domain

import utm.ass.project_m.data.FileDto
import utm.ass.project_m.data.FileService

class GetFileRecordById(private val fileService: FileService) {
    suspend fun execute(id: Int): FileDto {
        return fileService.read(id)
    }
}