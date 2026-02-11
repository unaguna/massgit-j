package jp.unaguna.massgit.common.io

import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

class FileSystemResource(val path: Path) : InputStreamSource {
    override fun getInputStream(): InputStream {
        return path.inputStream()
    }
}

fun Path.toResource() = FileSystemResource(this)
