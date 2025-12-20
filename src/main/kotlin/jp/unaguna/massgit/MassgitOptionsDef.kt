package jp.unaguna.massgit

enum class MassgitOptionsDef(val names: List<String>, val argNum: Int) : jp.unaguna.massgit.common.args.OptionDef {
    VERSION(listOf("--version"), 0),
    MARKER(listOf("--marker", "-m"), 1),
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
