package jp.unaguna.massgit

interface OutputAnalyzer<Rs> {
    fun loadStdoutLine(line: String)
    fun loadStderrLine(line: String)
    fun getResult(): Rs

    fun toStdoutAdapter(): LinesUnitAdapter {
        return OutputAnalyzerStdoutAdapter(this)
    }

    fun toStderrAdapter(): LinesUnitAdapter {
        return OutputAnalyzerStderrAdapter(this)
    }

    /**
     * Adapter for using OutputAnalyzer within classes
     * that treat stdout and stderr as a single stream without distinguishing them
     */
    interface LinesUnitAdapter {
        fun loadLine(line: String)
    }

    private class OutputAnalyzerStdoutAdapter(private val outputAnalyzer: OutputAnalyzer<*>) : LinesUnitAdapter {
        override fun loadLine(line: String) {
            outputAnalyzer.loadStdoutLine(line)
        }
    }

    private class OutputAnalyzerStderrAdapter(private val outputAnalyzer: OutputAnalyzer<*>) : LinesUnitAdapter {
        override fun loadLine(line: String) {
            outputAnalyzer.loadStderrLine(line)
        }
    }

    object DoNothing : OutputAnalyzer<Unit> {
        override fun loadStdoutLine(line: String) {
            // do nothing
        }

        override fun loadStderrLine(line: String) {
            // do nothing
        }

        override fun getResult() {
            return
        }
    }
}
