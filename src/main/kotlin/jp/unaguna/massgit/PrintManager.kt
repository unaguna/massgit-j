package jp.unaguna.massgit

import java.io.Closeable
import java.io.InputStream

interface PrintManager : Closeable {
    fun readAllLinesAndInstantOutput(stdout: InputStream)
    fun postOutput(stdout: InputStream)
}
