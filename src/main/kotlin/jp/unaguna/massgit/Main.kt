package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.SystemProp
import jp.unaguna.massgit.exception.MassgitException
import jp.unaguna.massgit.help.HelpDefinition
import jp.unaguna.massgit.logging.LoggingSetUp
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class Main {
    private val logger = LoggerFactory.getLogger(Main::class.java)

    init {
        logger.info("Start massgit.")
    }

    @Suppress("ReturnCount", "ThrowsCount")
    fun run(
        mainArgs: MainArgs,
        confInj: MainConfigurations? = null,
        reposInj: List<Repo>? = null,
        processExecutor: ProcessExecutor? = null,
    ): Int {
        if (mainArgs.mainOptions.isHelp() || mainArgs.subOptions.contains("--help")) {
            val helpDef = loadHelpDef()

            // TODO: jvm 実行時は cmd を java -jar massgit.jar に変更する。
            // TODO: ウィンドウサイズを取得して、引数として使用する
            when (mainArgs.subCommand) {
                null -> helpDef.print(System.out, "massgit")
                // TODO: massgit 専用サブコマンドの場合に限る
                else -> helpDef.printSubcommand(System.out, "massgit", mainArgs.subCommand.name)
            }

            return 0
        }
        if (mainArgs.mainOptions.isVersion()) {
            showVersion()
            return 0
        }
        // TODO: サブコマンドが無い場合、usage を表示して終了
        requireNotNull(mainArgs.subCommand)

        val conf = confInj ?: MainConfigurations(mainArgs.mainOptions)

        // check whether the subcommand is accepted
        when (conf.subcommandAcceptation(mainArgs.subCommand)) {
            MainConfigurations.SubcommandAcceptation.PROHIBITED -> {
                throw ProhibitedSubcommandException(mainArgs.subCommand)
            }
            MainConfigurations.SubcommandAcceptation.UNKNOWN -> {
                throw UnknownSubcommandException(mainArgs.subCommand)
            }
            MainConfigurations.SubcommandAcceptation.OK -> Unit
        }

        val subcommandExecutor = mainArgs.subCommand.executor(
            mainArgs,
            conf,
            processExecutor,
            reposInj,
        )

        return subcommandExecutor.execute(conf, mainArgs)
    }

    private fun showVersion() {
        val version = VersionProperties.getVersion()
        println("massgit on java $version")
    }

    private fun loadHelpDef(): HelpDefinition {
        val helpUrl = this::class.java.getResource("/massgit-help.json")
            ?: error("massgit-help.json could not be found")
        return HelpDefinition.load(helpUrl)
    }

    companion object {
        @Suppress("MagicNumber", "MemberNameEqualsClassName")
        @JvmStatic
        fun main(args: Array<String>) {
            SystemProp.initialize()
            LoggingSetUp.setUpLogging()

            val mainInstance = Main()

            val exitCode = runCatching {
                mainInstance.run(mainArgs = MainArgs.of(args))
            }.onFailure { e ->
                val message = if (e is MassgitException) {
                    e.consoleMessage
                } else if (e.message != null) {
                    "some error occurred: ${e.message}"
                } else {
                    "some error occurred"
                }

                System.err.println("error: $message")
                mainInstance.logger.warn(message, e)
            }.getOrDefault(127)

            exitProcess(exitCode)
        }
    }
}

private class ProhibitedSubcommandException(subcommand: Subcommand) :
    MassgitException("subcommand '${subcommand.name}' is prohibited")

private class UnknownSubcommandException(subcommand: Subcommand) :
    MassgitException("unknown subcommand '${subcommand.name}'")
