package jp.unaguna.massgit

import java.nio.file.Path

interface ProcessExecutor {
    fun execute(
        command: List<String>,
        workingDir: Path? = null,
    ): Process

    companion object {
        fun default(): ProcessExecutor = DefaultProcessExecutor()
    }
}

private class DefaultProcessExecutor : ProcessExecutor {
    override fun execute(command: List<String>, workingDir: Path?): Process {
        val processBuilder = ProcessBuilder(command)
        if (workingDir != null) {
            processBuilder.directory(workingDir.toFile())
        }

        return processBuilder.start()
    }
}
