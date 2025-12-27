package jp.unaguna.massgit.logging

import jp.unaguna.massgit.Main
import java.util.logging.LogManager

object LoggingSetUp {
    fun setUpLogging() {
        LogManager.getLogManager()
            .readConfiguration(Main::class.java.getResourceAsStream("/massgit-logging-default.properties"))
    }
}
