package jp.unaguna.massgit

interface OutputAnalyzerFactory<Rp, Rs> {
    fun create(repo: Rp): OutputAnalyzer<Rs>?
}

interface OutputAnalyzer<Rs> {
    fun loadStdoutLine(line: String)
    fun loadStderrLine(line: String)
    fun getResult(): Rs

    fun toStdoutAdapter(): OutputAnalyzerUnitAdapter {
        return OutputAnalyzerStdoutAdapter(this)
    }

    fun toStderrAdapter(): OutputAnalyzerUnitAdapter {
        return OutputAnalyzerStderrAdapter(this)
    }
}

/**
 * Adapter for using OutputAnalyzer within classes
 * that treat stdout and stderr as a single stream without distinguishing them
 */
interface OutputAnalyzerUnitAdapter {
    fun loadLine(line: String)
}

private class OutputAnalyzerStdoutAdapter(private val outputAnalyzer: OutputAnalyzer<*>) : OutputAnalyzerUnitAdapter {
    override fun loadLine(line: String) {
        outputAnalyzer.loadStdoutLine(line)
    }
}

private class OutputAnalyzerStderrAdapter(private val outputAnalyzer: OutputAnalyzer<*>) : OutputAnalyzerUnitAdapter {
    override fun loadLine(line: String) {
        outputAnalyzer.loadStderrLine(line)
    }
}
