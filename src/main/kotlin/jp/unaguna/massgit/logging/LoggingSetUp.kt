package jp.unaguna.massgit.logging

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import jp.unaguna.massgit.configfile.SystemProp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.exists

object LoggingSetUp {
    fun setUpLogging() {
        // CAUTION: Do not call getLogger() until logging configuration is complete

        when (val iLoggerFactory = LoggerFactory.getILoggerFactory()) {
            is LoggerContext -> setUpLogbackLogging(iLoggerFactory)
            else -> {
                getLogger().info(
                    "Since logging library other than logback is being used, " +
                        "changes to the log configuration is canceled. LoggerFactory=$iLoggerFactory"
                )
            }
        }
    }

    private fun setUpLogbackLogging(context: LoggerContext) {
        // CAUTION: Do not call getLogger() until logging configuration is complete

        // When explicitly specifying settings using the -D option, those settings take precedence.
        // Since it should already be loaded via logback functionality, no action is required.
        if (SystemProp.logbackConfig != null) {
            LoggerFactory.getLogger(LoggingSetUp::class.java)
                .info("Logback config may be loaded by '-Dlogback.configurationFile=${SystemProp.logbackConfig}'.")
            return
        }

        val configPath = SystemProp.systemDir?.resolve("logback.xml")
        when {
            configPath == null -> {
                getLogger().info(
                    "The massgit system directory cannot be used. " +
                        "The logback configuration will use the massgit default."
                )
            }
            configPath.exists() -> {
                context.reset()

                val configurator = JoranConfigurator()
                configurator.setContext(context)
                configurator.doConfigure(configPath.toFile())

                getLogger().info("Logback config has been set to $configPath")
            }
            else -> {
                getLogger().info(
                    "The config file '$configPath' is not found. " +
                        "The logback configuration will use the massgit default."
                )
            }
        }
    }

    // CAUTION: Do not call getLogger() until logging configuration is complete
    private fun getLogger(): Logger {
        return LoggerFactory.getLogger(LoggingSetUp::class.java)
    }
}
