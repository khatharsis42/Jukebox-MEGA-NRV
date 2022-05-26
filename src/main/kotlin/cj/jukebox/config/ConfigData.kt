package cj.jukebox.config

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

const val nb0to255 = """(25[0-5]|2[0-4]\d|[01]?\d\d?)"""
val regIP = Regex("""$nb0to255\.$nb0to255\.$nb0to255\.$nb0to255""")
val regHex = Regex("""[\da-fA-F]+""")
@Serializable
data class ConfigData(
    @EncodeDefault val APP_NAME: String = "Jukebox",
    @EncodeDefault val DEBUG: Boolean = true,

    @EncodeDefault val LISTEN_ADDRESS: String = "0.0.0.0",
    @EncodeDefault val LISTEN_PORT: Int = 8080,

    @EncodeDefault val YT_KEYS: ArrayList<String> = arrayListOf(),
    @EncodeDefault val AMIXER_CHANNEL: String = "Master",

    @EncodeDefault val DATABASE_PATH: String = "src/main/resources/jukebox.sqlite3",
    @EncodeDefault val TMP_PATH: String = "src/backend/tmp/",

    @EncodeDefault val SECRET_ENCRYPT_KEY: String = "6b6287991d47e783e3a261cca1a0a1b9",
    @EncodeDefault val SECRET_SIGN_KEY: String = "9cb8d9913aebc815cb58562aa479",

    @EncodeDefault val SECRET_PASSWORD_ENCRYPT: String = "kXp2s5v8y/B?E(H+"
) {
    init {
        require(APP_NAME.isNotEmpty()) { "App name can't be empty" }
        require(LISTEN_ADDRESS == "localhost" || LISTEN_ADDRESS.matches(regIP)) { "Listen address doesn't look like an IP address" }
        require(LISTEN_PORT in (0..65535)) { "Invalid port" }
        require(DATABASE_PATH.endsWith(".sqlite3")) { "Requiring a sqlite3 reference for database file" }
        // TODO: check if file paths (database, tmp) are valid paths
        // TODO: check YT keys
        // TODO: check amixer
        require(SECRET_ENCRYPT_KEY.length == 32) { "Incorrect length of cookie encryption key (${SECRET_ENCRYPT_KEY.length} != 32)" }
        require(SECRET_ENCRYPT_KEY.matches(regHex)) { "Cookie encryption key doesn't look like a hex number" }
        require(SECRET_SIGN_KEY.length == 28) { "Incorrect length of cookie sign key (${SECRET_SIGN_KEY.length} != 28)" }
        require(SECRET_SIGN_KEY.matches(regHex)) { "Cookie sign key doesn't look like a hex number" }
        require(SECRET_PASSWORD_ENCRYPT.length == 16) { "Incorrect length of password encryption key (${SECRET_PASSWORD_ENCRYPT.length} != 16)" }

        if (DEBUG && YT_KEYS.isEmpty()) println("WARNING: no YT key registered")
    }
}