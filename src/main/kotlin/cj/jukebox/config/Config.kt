package cj.jukebox.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOError

class Config(file: String) {
    private val fileRepr: File = File(file)
    init {
        if (!fileRepr.exists()) {
            this.reset()
        } else if (!fileRepr.canRead() && !fileRepr.canRead()) {
            throw IOError(Throwable("Cannot manipulate config file ${file}."))
        }
    }
    var data: ConfigData = Json.decodeFromString(fileRepr.readText())

    private fun reset() {
        this.data = ConfigData()
        return this.save()
    }

    private fun save() {
        this.fileRepr.writeText(Json.encodeToString(this.data))
    }
}