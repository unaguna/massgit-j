package jp.unaguna.massgit

import jp.unaguna.massgit.exception.MassgitException

data class MainArgs(
    val mainOptions: Options,
    val subCommand: String?,
    val subOptions: List<String>,
) {
    enum class OptionDef(val names: List<String>, val argNum: Int) {
        VERSION(listOf("--version"), 0),
        REP_SUFFIX(listOf("--rep-suffix"), 1),
        ;

        /**
         * Judge sufficiency of args
         *
         * @return true if the specified number is valid as the number of arguments or exceeds it, or false otherwise.
         */
        fun sufficient(actualNum: Int): Boolean {
            return actualNum >= this.argNum
        }
    }

    interface Option {
        val def: OptionDef
        val args: List<String>
    }

    class OptionImpl(override val def: OptionDef) : Option {
        override val args = mutableListOf<String>()

        fun push(arg: String) {
            args.add(arg)
        }

        /**
         * Judge sufficiency of args
         *
         * @return true if the number of arguments is valid or exceeds, or false otherwise.
         */
        fun argsNumberIsSufficient(): Boolean {
            return def.sufficient(args.size)
        }
    }

    interface Options : Map<OptionDef, List<Option>> {
        /**
         * Returns the specified option argument instance.
         *
         * @return an option instance. If the option is not used, returns empty list.
         */
        fun of(key: OptionDef): List<Option>
    }

    private class OptionsImpl private constructor(
        private val optionMap: MutableMap<OptionDef, MutableList<Option>>,
    ) : Map<OptionDef, List<Option>> by optionMap, Options {
        constructor() : this(mutableMapOf())

        fun addOption(optionDef: OptionDef): OptionImpl {
            val options = optionMap.getOrPut(optionDef) { mutableListOf() }
            val option = OptionImpl(optionDef)
            options.add(option)
            return option
        }

        fun addOption(optionSpec: String): OptionImpl {
            val optionParts = optionSpec.split("=", limit = 2)
            val optionName = optionParts[0]
            val optionValue = optionParts.getOrNull(1)

            val optionDef = mainOptionDef.getOrElse(optionName) {
                throw UnknownOptionException(optionName)
            }

            val option = addOption(optionDef)
            if (optionValue != null) {
                option.push(optionValue)
            }
            return option
        }

        override fun of(key: OptionDef): List<Option> {
            return this[key] ?: emptyList()
        }
    }

    companion object {
        private val mainOptionDef: Map<String, OptionDef> = OptionDef.entries
            .map { it.names.map { name -> Pair(name, it) } }
            .flatten()
            .associate { it }

        fun of(args: Array<String>): MainArgs {
            return of(args.toList())
        }

        fun of(args: List<String>): MainArgs {
            val mainOptions = OptionsImpl()
            var subCommand: String? = null
            val subOptions = mutableListOf<String>()

            var processingOption: OptionImpl? = null
            val argsBuffer = ArrayDeque(args)
            while (subCommand == null && argsBuffer.isNotEmpty()) {
                // Determine whether optional arguments continue
                if (processingOption != null) {
                    // If it has already received a sufficient number of arguments,
                    // it stops accepting arguments for that option.
                    if (processingOption.argsNumberIsSufficient()) {
                        processingOption = null
                    }
                }

                val arg = argsBuffer.removeFirst()
                if (arg.first() != '-') {
                    if (processingOption != null) {
                        processingOption.push(arg)
                    } else {
                        subCommand = arg
                    }
                } else {
                    processingOption = mainOptions.addOption(arg)
                }
            }
            subOptions.addAll(argsBuffer)

            // TODO: validate mainOptions

            return MainArgs(
                mainOptions = mainOptions,
                subCommand = subCommand,
                subOptions = subOptions,
            )
        }
    }
}

private class UnknownOptionException(unknownOption: String) : MassgitException("Unknown option: $unknownOption")
