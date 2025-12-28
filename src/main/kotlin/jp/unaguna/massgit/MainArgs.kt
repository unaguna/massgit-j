package jp.unaguna.massgit

import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class MainArgs(
    val mainOptions: MassgitOptions,
    val subCommand: String?,
    val subOptions: List<String>,
) {
    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(MainArgs::class.java) }

        fun of(args: Array<String>): MainArgs {
            return of(args.toList())
        }

        fun of(args: List<String>): MainArgs {
            logger.info("received arguments: {}", args.joinToString(" "))

            val (mainOptions, remainingArgs) = MassgitOptions.build(args)

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
            ).also {
                logger.debug("Interpreted arguments: {}", it)
            }
        }
    }
}
