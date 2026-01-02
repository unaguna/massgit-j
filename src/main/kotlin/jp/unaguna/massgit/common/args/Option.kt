package jp.unaguna.massgit.common.args

interface Option<D : OptionDef> {
    /** the order in original arguments list */
    val order: Int
    val def: D
    val args: List<String>

    fun getOneArg(): String {
        return args.getOrElse(0) { throw IllegalArgumentException("the option has 0 or more two arguments") }
    }
}

class OptionImpl<D : OptionDef>(override val order: Int, override val def: D) : Option<D> {
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

    override fun toString(): String {
        return when (args.size) {
            0 -> "Option(${def.representativeName})"
            else -> "Option(${def.representativeName}, ${args.joinToString(" ")})"
        }
    }
}
