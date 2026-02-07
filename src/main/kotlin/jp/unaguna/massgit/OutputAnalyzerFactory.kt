package jp.unaguna.massgit

interface OutputAnalyzerFactory<Rp, Rs> {
    fun create(repo: Rp): OutputAnalyzer<Rs>
}

class OutputAnalyzerDoNothingFactory<Rp> : OutputAnalyzerFactory<Rp, Unit> {
    override fun create(repo: Rp): OutputAnalyzer<Unit> = OutputAnalyzer.DoNothing
}
