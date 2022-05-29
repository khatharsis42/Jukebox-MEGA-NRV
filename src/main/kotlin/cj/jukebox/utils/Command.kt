package cj.jukebox.utils

import java.io.File
import java.util.concurrent.TimeUnit

enum class SigName { SIGINT, SIGUSR1, SIGUSR2 }

fun String.runCommand(workingDir: File = File("."), toNbr: Long = 60, toUnit: TimeUnit = TimeUnit.SECONDS): Process? =
    runCatching {
        ProcessBuilder(Regex("\\s").split(this))
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start().also { it.waitFor(toNbr, toUnit) }
    }.onFailure { it.printStackTrace() }.getOrNull()

fun Process.sendSignal(sigName: SigName) {
    "kill -${sigName.name} ${pid().toInt()}".runCommand()
}