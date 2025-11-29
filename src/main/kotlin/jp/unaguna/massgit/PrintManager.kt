package jp.unaguna.massgit

import java.io.InputStream

interface PrintManager {
    fun readAllLinesAndInstantOutput(stdout: InputStream)
    fun postOutput(stdout: InputStream)
}
