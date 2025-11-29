package jp.unaguna.massgit

data class MainArgs(
    val mainOptions: List<String>,
    val subCommand: String?,
    val subOptions: List<String>,
) {
    companion object {
        fun of(args: List<String>): MainArgs {
            val mainOptions = mutableListOf<String>()
            var subCommand: String? = null
            val subOptions = mutableListOf<String>()

            val argsBuffer = ArrayDeque(args)
            while (subCommand == null && argsBuffer.isNotEmpty()) {
                val arg = argsBuffer.removeFirst()
                if (arg.first() != '-') {
                    subCommand = arg
                } else {
                    mainOptions.add(arg)
                }
            }
            subOptions.addAll(argsBuffer)

            return MainArgs(
                mainOptions = mainOptions,
                subCommand = subCommand,
                subOptions = subOptions,
            )
        }
    }
}
