package jp.unaguna.massgit.testcommon.io

import java.io.OutputStream

class EmptyOutputStream : OutputStream() {
    override fun write(b: Int) {
        // do nothing
    }
}
