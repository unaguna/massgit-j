package jp.unaguna.massgit.testcommon.io

import java.io.InputStream

class EmptyInputStream : InputStream() {
    override fun read(): Int {
        return -1
    }
}
