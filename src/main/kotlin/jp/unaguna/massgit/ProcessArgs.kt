package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo

interface ProcessArgs {
    fun render(repo: Repo): List<String>
}

private class ProcessArgsImpl(private val buffer: List<ProcessArgsPart>) : ProcessArgs {
    override fun render(repo: Repo): List<String> {
        return buffer
            .map { it.render(repo) }
            .flatten()
    }
}

fun buildProcessArgs(action: ProcessArgsBuilder.() -> Unit): ProcessArgs {
    return ProcessArgsBuilder().apply(action).build()
}

class ProcessArgsBuilder {
    private val buffer = mutableListOf<ProcessArgsPart>()

    fun append(arg: String) {
        buffer.add(ProcessArgsPartConst(arg))
    }

    fun append(arg: List<String>) {
        buffer.addAll(arg.map { ProcessArgsPartConst(it) })
    }

    fun append(arg: (Repo) -> List<String>) {
        buffer.add(ProcessArgsPartImpl(arg))
    }

    fun build(): ProcessArgs {
        return ProcessArgsImpl(buffer)
    }
}

interface ProcessArgsPart {
    fun render(repo: Repo): List<String>
}

class ProcessArgsPartConst(value: String) : ProcessArgsPart {
    private val values = listOf(value)
    override fun render(repo: Repo): List<String> = values
}

class ProcessArgsPartImpl(private val renderAction: (Repo) -> List<String>) : ProcessArgsPart {
    override fun render(repo: Repo): List<String> {
        return renderAction(repo)
    }
}
