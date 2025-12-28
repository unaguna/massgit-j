package jp.unaguna.massgit

import jp.unaguna.massgit.common.args.Option
import jp.unaguna.massgit.common.args.OptionDefProvider
import jp.unaguna.massgit.common.args.Options
import jp.unaguna.massgit.exception.MassgitException

class MassgitOptions(
    private val options: Options<MassgitOptionsDef>,
) : Map<MassgitOptionsDef, List<Option<MassgitOptionsDef>>> by options {
    fun isVersion() = contains(MassgitOptionsDef.VERSION)
    fun getMarker() = options.getOneOrNull(MassgitOptionsDef.MARKER)?.getOneArg()
    fun getRepSuffix() = options.getOneOrNull(MassgitOptionsDef.REP_SUFFIX)?.getOneArg()

    override fun toString(): String {
        return options.toString()
    }

    companion object {
        fun build(args: List<String>): Pair<MassgitOptions, List<String>> {
            val (mainOptionsInner, remainingArgs) = Options.build(args, MassgitOptionDefProvider)
            return Pair(MassgitOptions(mainOptionsInner), remainingArgs)
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
