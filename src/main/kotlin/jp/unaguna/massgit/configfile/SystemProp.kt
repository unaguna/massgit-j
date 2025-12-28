package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.Main
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.toPath

object SystemProp {
    val systemDir: Path? by lazy {
        System.getProperty("massgit.system-dir")?.let { Path(it) }
    }

    val logbackConfig: Path? by lazy {
        System.getProperty("logback.configurationFile")?.let { Path(it) }
    }

    fun initialize() {
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
            }.getOrNull()

            if (systemDir != null) {
                System.setProperty("massgit.system-dir", systemDir.toString())
            }
        }
    }
}
