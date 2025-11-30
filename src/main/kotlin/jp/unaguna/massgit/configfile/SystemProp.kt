package jp.unaguna.massgit.configfile

import java.nio.file.Path
import kotlin.io.path.Path

object SystemProp {
    fun getSystemDir(): Path? = System.getProperty("massgit.system-dir")?.let { Path(it) }
}
