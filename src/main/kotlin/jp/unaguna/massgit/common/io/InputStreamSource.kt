package jp.unaguna.massgit.common.io

import java.io.InputStream

interface InputStreamSource {
    fun getInputStream(): InputStream
}
