package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.Main
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.toPath

interface SystemProp {
    val executableType: ExecutableType?
    val systemDir: Path?
    val logbackConfig: Path?

    enum class ExecutableType {
        EXE,
        JAVA,
        ;
        val nameForFilename = name.lowercase()
    }
}

object SystemPropImpl : SystemProp {
    override val executableType: SystemProp.ExecutableType? by lazy {
        System.getProperty("massgit.executable-type")?.let { SystemProp.ExecutableType.valueOf(it.uppercase()) }
    }

    override val systemDir: Path? by lazy {
        System.getProperty("massgit.system-dir")?.let { Path(it) }
    }

    override val logbackConfig: Path? by lazy {
        System.getProperty("logback.configurationFile")?.let { Path(it) }
    }

    fun initialize() {
        if (System.getProperty("massgit.executable-type") == null) {
            val codeSource = Main::class.java.protectionDomain.codeSource?.location?.toURI()?.toPath()
            println(codeSource)
            println(codeSource?.fileName)

            val executableType = when {
                codeSource == null -> null
                codeSource.isDirectory() -> SystemProp.ExecutableType.JAVA
                codeSource.fileName.toString().endsWith(".jar") -> SystemProp.ExecutableType.JAVA
                codeSource.fileName.toString().endsWith(".exe") -> SystemProp.ExecutableType.EXE
                else -> null
            }
            println(executableType)

            if (executableType != null) {
                System.setProperty("massgit.executable-type", executableType.name)
            }
        }

        if (System.getProperty("massgit.system-dir") == null) {
            val systemDir = runCatching {
                // If launched by executing an EXE file, obtain the path to that file;
                // if launched via the java command, obtain the path to the JAR file.
                val codeSource = Main::class.java.protectionDomain.codeSource?.location?.toURI()?.toPath()
                when {
                    codeSource == null -> null
                    codeSource.isDirectory() -> codeSource
                    else -> codeSource.parent
                }
            }.onFailure {
                // TODO: ログ出力
                // At the point this code executes, the logging configuration is not yet complete,
                // so getLogger() is unavailable.
            }.getOrNull()

            if (systemDir != null) {
                System.setProperty("massgit.system-dir", systemDir.toString())
            }
        }
    }
}
