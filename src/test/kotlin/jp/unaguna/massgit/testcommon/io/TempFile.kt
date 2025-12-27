package jp.unaguna.massgit.testcommon.io

import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.createTempFile
import kotlin.io.path.outputStream

fun createTempTextFile(
    directory: Path,
    prefix: String? = null,
    suffix: String? = null,
    vararg attributes: FileAttribute<*>,
    setup: PrintWriter.() -> Unit,
): Path {
    val path = createTempFile(directory, prefix, suffix, *attributes)

    PrintWriter(path.outputStream()).use { writer ->
        setup(writer)
        writer.flush()
    }

    return path
}
