package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.AllSet
import jp.unaguna.massgit.common.syntaxtree.BooleanTree
import jp.unaguna.massgit.common.syntaxtree.ValueProvider
import jp.unaguna.massgit.configfile.Prop
import java.nio.file.Path
import kotlin.io.path.Path

class MainConfigurations(
    options: MassgitOptions,
    private val prop: Prop = Prop(),
) {
    val massProjectDir: Path
        get() = System.getProperty("jp.unaguna.massgit.projectDir")?.let { Path(it) }
            ?: Path("").toAbsolutePath()

    val reposFilePath: Path
        get() = massProjectDir.resolve(".massgit").resolve("repos.json")

    val knownSubcommands: Set<String> by lazy {
        prop.getSet(Prop.Key.KnownSubcommands) ?: AllSet()
    }

    val markerConditions: MarkerConditions? by lazy {
        options.getMarker()?.let { MarkerConditions(it) }
    }

    val repSuffix: String? = options.getRepSuffix()

    fun subcommandAcceptation(subcommand: String): SubcommandAcceptation {
        return when {
            prohibitSubcommand(subcommand) -> SubcommandAcceptation.PROHIBITED
            subcommandIsUnknown(subcommand) -> SubcommandAcceptation.UNKNOWN
            else -> SubcommandAcceptation.OK
        }
    }

    fun prohibitSubcommand(subcommand: String): Boolean {
        return prop.getBoolean(Prop.Key.ProhibitedSubcommands(subcommand))
    }

    fun subcommandIsUnknown(subcommand: String): Boolean {
        return !knownSubcommands.contains(subcommand)
    }

    enum class SubcommandAcceptation {
        OK,
        PROHIBITED,
        UNKNOWN,
    }
}

class MarkerConditions(private val expression: BooleanTree) {
    constructor(expression: String) : this(BooleanTree.decode(expression))

    fun satisfies(markers: List<String>): Boolean {
        val vars = ValueProvider.fromTrueSet(markers)
        return expression.evaluate(vars)
    }
}
