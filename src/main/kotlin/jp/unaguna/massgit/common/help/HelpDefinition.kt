package jp.unaguna.massgit.common.help

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.PrintStream
import java.net.URL

@Serializable
data class HelpDefinition(
    val usages: List<String>,
    val options: List<Option>,
) {
    fun print(out: PrintStream, cmd: String, optionWidth: Int = 10, descWidth: Int = 69) {
        require(optionWidth > 0) { "optionWidth must be greater than zero" }

        out.println("Usage:")
        usages.forEach { usage ->
            out.println(usage.format(cmd))
        }
        out.println()

        out.println("Options:")
        options.forEach { option ->
            val optionStr = option.toString()
            out.print(optionStr)
            if (optionStr.length > optionWidth) {
                out.println()
                out.print(" ".repeat(optionWidth + 2))
            } else {
                out.print(" ".repeat(optionWidth - optionStr.length + 2))
            }

            var lineLength = 0
            option.description.split(" ").forEach { word ->
                if (lineLength > 0 && lineLength + word.length > descWidth) {
                    out.println()
                    out.print(" ".repeat(optionWidth + 2))
                    lineLength = optionWidth + 2
                } else if (lineLength > 0) {
                    out.print(" ")
                    lineLength += 1
                }

                out.print(word)
                lineLength += word.length
            }
            out.println()
            out.println()
        }
    }

    companion object {
        fun load(url: URL): HelpDefinition {
            return Json.decodeFromString<HelpDefinition>(url.readText(Charsets.UTF_8))
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val helpDef = load(HelpDefinition::class.java.getResource("/" + args[0])!!)
            helpDef.print(System.out, "command")
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
            return when {
                type == null -> names.joinToString(separator = ", ")
                argOptional -> names.joinToString(separator = ", ") { "$it[=<$dest>]" }
                else -> names.joinToString(separator = ", ") { "$it=<$dest>" }
            }
        }
    }

    enum class ArgType {
        String,
    }
}
