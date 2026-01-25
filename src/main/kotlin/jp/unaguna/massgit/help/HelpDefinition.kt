package jp.unaguna.massgit.help

import jp.unaguna.massgit.common.textio.IndentPrintStreamWrapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.io.PrintStream
import java.net.URL

@Suppress("LongParameterList", "TooManyFunctions")
@Serializable
data class HelpDefinition(
    val version: Int? = null,
    val name: String,
    val description: String? = null,
    val subdesc: String? = null,
    val hideInList: Boolean = false,
    val sections: List<Section> = emptyList(),
    val subcommands: List<HelpDefinition>? = null,
) {
    fun subcommandIsExist(subcommand: String): Boolean {
        return getSubcommandOrNull(subcommand) != null
    }

    private fun getSubcommandOrNull(subcommand: String): HelpDefinition? {
        return this.subcommands?.find { it.name == subcommand }
    }

    private fun getSubcommand(subcommand: String): HelpDefinition {
        return this.getSubcommandOrNull(subcommand)
            ?: error("Subcommand $subcommand not found")
    }

    fun print(
        out: PrintStream,
        cmd: String = name,
        root: HelpDefinition = this,
        windowWidth: Int = 120,
        optionWidth: Int = defaultOptionWidth(windowWidth),
        indentSize: Int = 4,
    ) {
        require(optionWidth > 0) { "optionWidth must be greater than zero" }
        require(windowWidth > 0) { "windowWidth must be greater than zero" }
        require(optionWidth <= windowWidth - 2) { "optionWidth must be less than or equal to windowWidth - 2" }

        val out = IndentPrintStreamWrapper(out, windowWidth = windowWidth)

        if (description != null) {
            out.println(description)
            out.println()
        }

        sections.forEach { section ->
            when (section.type) {
                SectionType.Options -> printOptions(out, section, optionWidth = optionWidth, indentSize = indentSize)
                SectionType.Subcommands -> printSubcommands(
                    out,
                    section,
                    optionWidth = optionWidth,
                    indentSize = indentSize,
                )
                SectionType.Regular -> printSection(
                    out,
                    section,
                    cmd = cmd,
                    optionWidth = optionWidth,
                    indentSize = indentSize,
                )
                SectionType.RootOptions -> root.printOptions(
                    out,
                    section.targetOptions,
                    optionWidth = optionWidth,
                    indentSize = indentSize,
                    header = section.header,
                )
            }
        }
    }

    fun printSection(
        out: PrintStream,
        sectionName: String,
        cmd: String = name,
        windowWidth: Int = 120,
        optionWidth: Int = defaultOptionWidth(windowWidth),
        indentSize: Int = 4,
    ) {
        val out = IndentPrintStreamWrapper(out, windowWidth = windowWidth)
        printSection(out, sectionName = sectionName, cmd = cmd, optionWidth = optionWidth, indentSize = indentSize)
    }

    fun printSection(
        out: IndentPrintStreamWrapper,
        sectionName: String,
        cmd: String = name,
        optionWidth: Int? = null,
        indentSize: Int = 4,
    ) {
        val section = sections.find { it.name == sectionName }!!
        printSection(out, section, cmd = cmd, optionWidth = optionWidth, indentSize = indentSize)
    }

    fun printSection(
        out: IndentPrintStreamWrapper,
        section: Section,
        cmd: String = name,
        optionWidth: Int? = null,
        indentSize: Int = 4,
    ) {
        val optionWidth = optionWidth
            ?: defaultOptionWidth(out.windowWidth)
        out.println(section.header)
        out.withIndent(indentSize) {
            section.items.forEach { item ->
                val left = item.left.format(cmd)
                out.print(left)

                if (item.right != null) {
                    if (left.length > optionWidth) {
                        out.println()
                        out.print(" ".repeat(optionWidth + 2))
                    } else {
                        out.print(" ".repeat(optionWidth - left.length + 2))
                    }

                    out.withIndent(optionWidth + 2) {
                        out.println(item.right)
                    }
                } else {
                    out.println()
                }
            }
        }
        out.println()
    }

    fun printOptions(
        out: IndentPrintStreamWrapper,
        sectionName: String,
        optionWidth: Int? = null,
        indentSize: Int = 4,
        header: String? = null,
    ) {
        val section = sections.find { it.name == sectionName }!!
        printOptions(out, section, optionWidth = optionWidth, indentSize = indentSize, header = header)
    }

    fun printOptions(
        out: IndentPrintStreamWrapper,
        section: Section,
        optionWidth: Int? = null,
        indentSize: Int = 4,
        header: String? = null,
    ) {
        require(section.type == SectionType.Options)

        val optionWidth = optionWidth
            ?: defaultOptionWidth(out.windowWidth)
        out.println(header ?: section.header)
        out.withIndent(indentSize) {
            section.options.forEach { option ->
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
    }

    fun printSubcommands(
        out: IndentPrintStreamWrapper,
        section: Section,
        optionWidth: Int? = null,
        indentSize: Int = 4,
    ) {
        val optionWidth = optionWidth
            ?: defaultOptionWidth(out.windowWidth)
        out.println(section.header)
        out.withIndent(indentSize) {
            subcommands?.asSequence()?.filter { !it.hideInList }?.forEach { subcommand ->
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
        out.println()
    }

    fun printSubcommand(
        out: PrintStream,
        cmd: String = name,
        subcommand: String,
        windowWidth: Int = 120,
        optionWidth: Int = defaultOptionWidth(windowWidth),
        indentSize: Int = 4,
    ) {
        getSubcommand(subcommand)
            .print(out, cmd, root = this, windowWidth = windowWidth, optionWidth = optionWidth, indentSize = indentSize)
    }

    companion object {
        private const val VERSION = 2

        fun load(url: URL? = null): HelpDefinition {
            val helpUrl = url
                ?: this::class.java.getResource("/massgit-help.json")
                ?: error("massgit-help.json could not be found")

            val helpJson = Json.parseToJsonElement(helpUrl.readText(Charsets.UTF_8)) as JsonObject
            val actualVersion = helpJson["version"]?.jsonPrimitive
            check(actualVersion == JsonPrimitive(VERSION)) {
                "Help files other than version 1 cannot be loaded; actual=$actualVersion"
            }

            return Json.decodeFromJsonElement<HelpDefinition>(helpJson)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val helpDef = load(HelpDefinition::class.java.getResource("/" + args[0])!!)
            println(helpDef)
            println()
            helpDef.print(System.out, "command", windowWidth = 80)
        }
    }

    @Suppress("MagicNumber")
    private fun defaultOptionWidth(windowWidth: Int): Int = windowWidth / 5

    @Serializable
    data class Section(
        val name: String,
        val header: String,
        val type: SectionType,
        val items: List<SectionItem> = emptyList(),
        val options: List<Option> = emptyList(),
        val targetOptions: String = "options",
    )

    @Serializable
    data class SectionItem(
        val left: String,
        val right: String? = null,
    )

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
        Integer,
    }

    enum class SectionType {
        Regular,
        Options,
        Subcommands,
        RootOptions,
    }
}
