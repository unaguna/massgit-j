package jp.unaguna.massgit

import jp.unaguna.massgit.common.args.Option
import jp.unaguna.massgit.common.args.OptionDefProvider
import jp.unaguna.massgit.common.args.Options
import jp.unaguna.massgit.exception.MassgitException

data class MainArgs(
    val mainOptions: MassgitOptions,
    val subCommand: String?,
    val subOptions: List<String>,
) {
    enum class OptionDef(val names: List<String>, val argNum: Int) : jp.unaguna.massgit.common.args.OptionDef {
        VERSION(listOf("--version"), 0),
        REP_SUFFIX(listOf("--rep-suffix"), 1),
        ;
        override val representativeName = names[0]

        /**
         * Judge sufficiency of args
         *
         * @return true if the specified number is valid as the number of arguments or exceeds it, or false otherwise.
         */
        override fun sufficient(actualNum: Int): Boolean {
            return actualNum >= this.argNum
        }
    }

    class MassgitOptions(
        private val options: Options<OptionDef>,
    ) : Map<OptionDef, List<Option<OptionDef>>> by options {
        fun getRepSuffix() = options.getOneOrNull(OptionDef.REP_SUFFIX)?.getOneArg()
    }

    companion object {
        fun of(args: Array<String>): MainArgs {
            return of(args.toList())
        }

        fun of(args: List<String>): MainArgs {
            val (mainOptionsInner, remainingArgs) = Options.build(args, MassgitOptionDefProvider)

            val mainOptions = MassgitOptions(mainOptionsInner)
            val subCommand: String? = remainingArgs.getOrNull(0)
            val subOptions = mutableListOf<String>().apply {
                addAll(remainingArgs.subList(1, remainingArgs.size))
            }

            // TODO: validate mainOptions

            return MainArgs(
                mainOptions = mainOptions,
                subCommand = subCommand,
                subOptions = subOptions,
            )
        }

        private object MassgitOptionDefProvider : OptionDefProvider<OptionDef> {
            private val mainOptionDef: Map<String, OptionDef> = OptionDef.entries
                .flatMap { it.names.map { name -> Pair(name, it) } }
                .associate { it }

            override fun getOptionDef(name: String): OptionDef {
                return mainOptionDef.getOrElse(name) {
                    throw UnknownOptionException(name)
                }
            }
        }
    }
}

private class UnknownOptionException(unknownOption: String) : MassgitException("Unknown option: $unknownOption")
