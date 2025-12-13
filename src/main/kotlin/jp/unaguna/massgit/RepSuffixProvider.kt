package jp.unaguna.massgit

class RepSuffixProvider {
    fun decideRefSuffix(
        args: MainArgs,
    ): String {
        // If explicitly specified in the main arguments, it returns that.
        val specified = args.mainOptions.getOneOrNull(MainArgs.OptionDef.REP_SUFFIX)?.getOneArg()
        if (specified != null) return specified

        return when (args.subCommand) {
            "grep", "ls-files" -> "/"
            "diff" -> when {
                args.subOptions.contains("--name-only") -> "/"
                else -> ": "
            }
            else -> ": "
        }
    }
}
