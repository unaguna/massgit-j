package jp.unaguna.massgit.common.help

import jp.unaguna.massgit.common.textio.IndentPrintStreamWrapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.PrintStream
import java.net.URL

@Serializable
data class HelpDefinition(
    val name: String,
    val subdesc: String? = null,
    val usages: List<String>,
    val options: List<Option>,
    val subcommands: List<HelpDefinition>? = null,
) {
    fun print(
        out: PrintStream,
        cmd: String = name,
        windowWidth: Int = 120,
        @Suppress("MagicNumber")
        optionWidth: Int = (windowWidth / 5),
        indentSize: Int = 4,
    ) {
        require(optionWidth > 0) { "optionWidth must be greater than zero" }
        require(windowWidth > 0) { "windowWidth must be greater than zero" }
        require(optionWidth <= windowWidth - 2) { "optionWidth must be less than or equal to windowWidth - 2" }

        val out = IndentPrintStreamWrapper(out, windowWidth = windowWidth)

        out.println("Usage:")
        out.withIndent(indentSize) {
            usages.forEach { usage ->
                out.println(usage.format(cmd))
            }
        }
        out.println()

        out.println("Options:")
        out.withIndent(indentSize) {
            options.forEach { option ->
                val optionStr = option.toString()
                out.print(optionStr)
                if (optionStr.length > optionWidth) {
                    out.println()
                    out.print(" ".repeat(optionWidth + 2))
                } else {
                    out.print(" ".repeat(optionWidth - optionStr.length + 2))
                }

                out.withIndent(optionWidth + 2) {
                    out.println(option.description)
                }
            }
        }
        out.println()

        out.println("Subcommands:")
        out.withIndent(indentSize) {
            subcommands?.forEach { subcommand ->
                out.print(subcommand.name)
                if (subcommand.name.length > optionWidth) {
                    out.println()
                    out.print(" ".repeat(optionWidth + 2))
                } else {
                    out.print(" ".repeat(optionWidth - subcommand.name.length + 2))
                }

                out.withIndent(optionWidth + 2) {
                    subcommand.subdesc?.let { out.println(it) }
                }
            }
        }
    }

    companion object {
        fun load(url: URL): HelpDefinition {
            return Json.decodeFromString<HelpDefinition>(url.readText(Charsets.UTF_8))
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val helpDef = load(HelpDefinition::class.java.getResource("/" + args[0])!!)
            println(helpDef)
            println()
            helpDef.print(System.out, "command", windowWidth = 80)
        }
    }

    @Serializable
    data class Option(
        val names: List<String>,
        val type: ArgType? = null,
        val argOptional: Boolean = false,
        val dest: String? = null,
        val description: String = "",
    ) {
        override fun toString(): String {
            val destNonNull = dest ?: names.maxBy { it.length }.replace(Regex("^-+"), "")

            return when {
                type == null -> names.joinToString(separator = ", ")
                argOptional -> names.joinToString(separator = ", ") { "$it[=<$destNonNull>]" }
                else -> names.joinToString(separator = ", ") { "$it=<$destNonNull>" }
            }
        }
    }

    enum class ArgType {
        String,
    }
}
