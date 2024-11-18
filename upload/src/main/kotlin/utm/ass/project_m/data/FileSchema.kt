package utm.ass.project_m.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class FileService(val connection: Connection) {

    companion object {
        private const val CREATE_TABLE_FILES =
            "CREATE TABLE FILES (ID SERIAL PRIMARY KEY, NAME VARCHAR(255), PATH VARCHAR(255), DESCRIPTION VARCHAR(255));"
        private const val SELECT_FILE_BY_ID = "SELECT name, path, description FROM files WHERE id = ?"
        private const val INSERT_FILE = "INSERT INTO files (name, path, description) VALUES (?, ?, ?)"
        private const val UPDATE_FILE = "UPDATE files SET name = ?, path = ?, description = ? WHERE id = ?"
        private const val DELETE_FILE = "DELETE FROM files WHERE id = ?"
    }

    init {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT to_regclass('public.files')")
        if (!resultSet.next() || resultSet.getString(1) == null) {
            statement.executeUpdate(CREATE_TABLE_FILES)
        }
    }

    suspend fun create(fileDto: FileDto): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_FILE, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, fileDto.name)
        statement.setString(2, fileDto.path)
        statement.setString(3, fileDto.description)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted fileDto")
        }
    }

    // Read a fileDto
    suspend fun read(id: Int): FileDto = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_FILE_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val name = resultSet.getString("name")
            val fileDto = resultSet.getString("path")
            val description = resultSet.getString("description")
            return@withContext FileDto(name, fileDto, description)
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a fileDto
    suspend fun update(id: Int, fileDto: FileDto) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_FILE)
        statement.setString(1, fileDto.name)
        statement.setString(2, fileDto.path)
        statement.setString(3, fileDto.description)
        statement.setInt(4, id)
        statement.executeUpdate()
    }

    // Delete a fileDto
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_FILE)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}

data class FileDto(
    val name: String,
    val path: String,
    val description: String,
)
