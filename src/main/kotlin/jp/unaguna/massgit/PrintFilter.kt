package jp.unaguna.massgit

interface PrintFilter {
    /**
     * Convert the line for output.
     *
     * @return the converted line. If it is determined that this input line should be ignored, return null.
     */
    fun mapLine(line: String): String?
}
