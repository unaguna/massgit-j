package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.SystemProp
import jp.unaguna.massgit.exception.MassgitException
import jp.unaguna.massgit.help.HelpDefinition
import jp.unaguna.massgit.logging.LoggingSetUp
import org.slf4j.LoggerFactory
import java.io.PrintStream
import java.nio.charset.Charset
import kotlin.system.exitProcess

class Main {
    private val logger = LoggerFactory.getLogger(Main::class.java)

    init {
        logger.info("Start massgit.")
    }

    @Suppress("MagicNumber", "ReturnCount", "ThrowsCount")
    fun run(
        mainArgs: MainArgs,
        confInj: MainConfigurations? = null,
        reposInj: List<Repo>? = null,
        processExecutor: ProcessExecutor? = null,
    ): Int {
        val conf = confInj ?: MainConfigurations(mainArgs.mainOptions)
        initializeStdoutStderr(conf)

        if (mainArgs.mainOptions.isHelp()) {
            val helpDef = HelpDefinition.load()

            // TODO: ウィンドウサイズを取得して、引数として使用する
            helpDef.print(System.out, "massgit")

            return 0
        }
        if (mainArgs.mainOptions.isVersion()) {
            showVersion()
            return 0
        }

        // subCommand is required.
        // If not specified, print usage.
        if (mainArgs.subCommand == null) {
            val helpDef = HelpDefinition.load()

            // TODO: ウィンドウサイズを取得して、引数として使用する
            helpDef.printSection(System.out, "usage", cmd = "massgit")
            return 127
        }

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

    /**
     * Initialize stdout and stderr
     *
     * In native image, standard output and standard error are initialized in UTF-8
     * regardless of runtime system properties,
     * so they are reinitialized using the system property's character encoding.
     */
    private fun initializeStdoutStderr(conf: MainConfigurations) {
        if (conf.enableStdoutEncodingReset) {
            val charsetName = System.getProperty("stdout.encoding")
            val out = PrintStream(
                System.out,
                false,
                Charset.forName(charsetName),
            )
            System.setOut(out)
            logger.debug("Encode of stdout has been reset to {}", charsetName)
        } else {
            logger.debug("Encode of stdout is {}", System.out.charset().name())
        }

        if (conf.enableStderrEncodingReset) {
            val charsetName = System.getProperty("stderr.encoding")
            val err = PrintStream(
                System.err,
                false,
                Charset.forName(charsetName),
            )
            System.setErr(err)
            logger.debug("Encode of stderr has been reset to {}", charsetName)
        } else {
            logger.debug("Encode of stderr is {}", System.err.charset().name())
        }
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
