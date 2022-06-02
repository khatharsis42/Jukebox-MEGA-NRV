package cj.jukebox.utils

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.Logger
import org.slf4j.event.Level
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

enum class Signal { SIGUSR1, SIGUSR2 }

/**
 * Lance une commande à partir d'une liste de String.
 * Chaque item de la liste est lu tel quel.
 * @author Khâtharsis
 */
fun List<String>.runCommand(
    workingDir: File = File("."),
    toNbr: Long = 60,
    toUnit: TimeUnit = TimeUnit.SECONDS,
    logger: Logger = Loggers.DEBUG,
): Process {
    val process = ProcessBuilder(this)
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    runBlocking {
        launch {logger.readFromStream(process.inputStream, Level.INFO) }
        launch {logger.readFromStream(process.errorStream, Level.ERROR) }
    }
    process.waitFor(toNbr, toUnit)
    return process
}

/**
 * Reads a stream and prints it in real time to the logger.
 * @author Khâtharsis
 */
fun Logger.readFromStream(inputStream: InputStream, level: Level) {
    val reader = InputStreamReader(inputStream)
    var c = reader.read()
    var s = ""
    while (c >= 0) {
        if (Char(c) == '\n') {
            when (level) {
                Level.DEBUG -> debug(s)
                Level.ERROR -> error(s)
                Level.INFO -> info(s)
                Level.TRACE -> trace(s)
                Level.WARN -> warn(s)
            }
            s = ""
        } else s += Char(c)
        c = reader.read()
    }
    reader.close()
}

fun Process.sendSignal(signal: Signal) {
    listOf("kill", "-${signal.name}", "${pid().toInt()}").runCommand()
}