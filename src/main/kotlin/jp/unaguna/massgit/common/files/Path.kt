package jp.unaguna.massgit.common.files

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

fun Path.isDirectoryContainsEntry(): Boolean {
    if (!this.isDirectory()) {
        return false
    }

    return Files.newDirectoryStream(this).use { it.firstOrNull() } != null
}

fun Path.toSlashedString(): String {
    return when (File.separatorChar) {
        '/' -> this.toString()
        else -> this.toString().replace(File.separatorChar, '/')
    }
}
