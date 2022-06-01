package cj.jukebox.utils

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.TimeUnit
import org.apache.logging.log4j.Logger
import org.slf4j.event.Level
import java.io.InputStream
import java.io.InputStreamReader

enum class SigName { SIGINT, SIGUSR1, SIGUSR2 }

/**
 * Lance la String sous la forme d'une commande. Opère un split(" ").
 * @author Khâtharsis
 */
@Deprecated("This is unsafe, you should use it on a List<String> instead.")
fun String.runCommand(
    workingDir: File = File("."),
    toNbr: Long = 60,
    toUnit: TimeUnit = TimeUnit.SECONDS,
    logger: Logger = Loggers.DEBUG
) =
    Regex("\\s").split(this).runCommand(workingDir, toNbr, toUnit, logger)

/**
 * Lance une commande à partir d'une liste de String.
 * Chaque item de la liste est lu tel quel.
 * @author Khâtharsis
 */
fun List<String>.runCommand(
    workingDir: File = File("."),
    toNbr: Long = 60,
    toUnit: TimeUnit = TimeUnit.SECONDS,
    logger: Logger = Loggers.DEBUG
): Process? {
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
    var s  = ""
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

fun Process.sendSignal(sigName: SigName) {
    "kill -${sigName.name} ${pid().toInt()}".runCommand()
}