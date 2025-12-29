package jp.unaguna.massgit

import org.slf4j.LoggerFactory
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

        logger.debug("Executing command: ${command.joinToString(" ")}")
        return processBuilder.start()
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(DefaultProcessExecutor::class.java) }
    }
}
