package cj.jukebox.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOError

/**
 * Classe contenant toutes les informations nécessaires à la configuration du jukebox.
 * @param[file] emplacement du fichier de configuration (format .json).
 * @author Ukabi
 */
class Config(file: String) {
    private val fileRepr: File = File(file)
    init {
        if (!fileRepr.exists()) {
            this.reset()
        } else if (!fileRepr.canRead() && !fileRepr.canWrite()) {
            throw IOError(Throwable("Cannot manipulate config file ${file}."))
        }
    }
    var data: ConfigData = Json.decodeFromString(fileRepr.readText())

    /**
     * Rétablit la configuration à celle par défaut, puis la sauvegarde.
     */
    private fun reset() {
        data = ConfigData()
        return save()
    }

    /**
     * Exporte la configuration actuelle.
     */
    private fun save() {
        fileRepr.writeText(Json.encodeToString(data))
    }
}