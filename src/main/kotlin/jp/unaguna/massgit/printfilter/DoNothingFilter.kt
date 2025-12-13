package jp.unaguna.massgit.printfilter

import jp.unaguna.massgit.PrintFilter

object DoNothingFilter : PrintFilter {
    override fun mapLine(line: String): String = line
}
