package jp.unaguna.massgit.printfilter

import jp.unaguna.massgit.PrintFilter

class LineHeadFilter(private val prefix: String) : PrintFilter {
    override fun mapLine(line: String): String {
        return prefix + line
    }
}
