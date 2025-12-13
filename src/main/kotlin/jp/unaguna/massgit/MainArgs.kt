package jp.unaguna.massgit

import jp.unaguna.massgit.common.args.OptionDefProvider
import jp.unaguna.massgit.common.args.Options
import jp.unaguna.massgit.exception.MassgitException

data class MainArgs(
    val mainOptions: MassgitOptions,
    val subCommand: String?,
    val subOptions: List<String>,
) {
    companion object {
        fun of(args: Array<String>): MainArgs {
            return of(args.toList())
        }

        fun of(args: List<String>): MainArgs {
            val (mainOptionsInner, remainingArgs) = Options.build(args, MassgitOptionDefProvider)

            val mainOptions = MassgitOptions(mainOptionsInner)
            val subCommand: String? = remainingArgs.getOrNull(0)
            val subOptions = mutableListOf<String>().apply {
                if (remainingArgs.size > 1) {
                    addAll(remainingArgs.subList(1, remainingArgs.size))
                }
            }

            // TODO: validate mainOptions

            return MainArgs(
                mainOptions = mainOptions,
                subCommand = subCommand,
                subOptions = subOptions,
            )
        }

        private object MassgitOptionDefProvider : OptionDefProvider<MassgitOptionsDef> {
            private val mainOptionDef: Map<String, MassgitOptionsDef> = MassgitOptionsDef.entries
                .flatMap { it.names.map { name -> Pair(name, it) } }
                .associate { it }

            override fun getOptionDef(name: String): MassgitOptionsDef {
                return mainOptionDef.getOrElse(name) {
                    throw UnknownOptionException(name)
                }
            }
        }
    }
}

private class UnknownOptionException(unknownOption: String) : MassgitException("Unknown option: $unknownOption")
