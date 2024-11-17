package utm.ass.project_m.domain

import utm.ass.project_m.data.FileDto
import utm.ass.project_m.data.FileService

class UpdateFileRecord(private val fileService: FileService) {
    suspend fun execute(name: String, path: String, description: String) {
        val fileDto = FileDto(
            name = name,
            path = path,
            description = description
        )

        fileService.create(fileDto)
    }
}
