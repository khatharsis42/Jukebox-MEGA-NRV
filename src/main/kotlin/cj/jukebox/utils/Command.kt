package cj.jukebox.utils

import java.io.File
import java.util.concurrent.TimeUnit

enum class SigName { SIGINT, SIGUSR1, SIGUSR2 }

/**
 * Lance la String sous la forme d'une commande. Opère un split(" ").
 * @author Khâtharsis
 */
@Deprecated("This is unsafe, you should use it on a List<String> instead.")
fun String.runCommand(workingDir: File = File("."), toNbr: Long = 60, toUnit: TimeUnit = TimeUnit.SECONDS) = Regex("\\s").split(this).runCommand(workingDir, toNbr, toUnit)

/**
 * Lance une commande à partir d'une liste de String.
 * Chaque item de la liste est lu tel quel.
 * @author Khâtharsis
 */
fun List<String>.runCommand(workingDir: File = File("."), toNbr: Long = 60, toUnit: TimeUnit = TimeUnit.SECONDS): Process? =
    runCatching {
        ProcessBuilder(this)
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start().also { it.waitFor(toNbr, toUnit) }
    }.onFailure { it.printStackTrace() }.getOrNull()
fun Process.sendSignal(sigName: SigName) {
    "kill -${sigName.name} ${pid().toInt()}".runCommand()
}