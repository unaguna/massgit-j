package jp.unaguna.massgit.logging

import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.ConsoleHandler
import java.util.logging.Formatter
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

class LogFormatter : Formatter() {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    }

    private fun levelToStr(level: Level): String = when (level) {
        Level.SEVERE -> "SEVERE"
        Level.WARNING -> "WARNING"
        Level.INFO -> "INFO"
        Level.CONFIG -> "CONFIG"
        else -> "FINE"
    }

    @Suppress("MagicNumber")
    private val nameColumnWidth = AtomicInteger(16)

    fun applyToRoot() {
        applyToRoot(ConsoleHandler())
    }

    fun applyToRoot(handler: Handler) {
        handler.setFormatter(LogFormatter())
        val root: Logger = Logger.getLogger("")
        root.setUseParentHandlers(false)
        for (h in root.handlers) {
            if (h is ConsoleHandler) root.removeHandler(h)
        }
        root.addHandler(handler)
    }

    @Suppress("MagicNumber")
    override fun format(record: LogRecord): String {
        val sb = StringBuilder(200)

        val instant: Instant = Instant.ofEpochMilli(record.millis)
        val ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        sb.append(formatter.format(ldt))
        sb.append(" ")

        sb.append(levelToStr(record.level))
        sb.append(" ")

        var category: String
        if (record.getSourceClassName() != null) {
            category = record.getSourceClassName()
            if (record.getSourceMethodName() != null) {
                category += " " + record.getSourceMethodName()
            }
        } else {
            category = record.loggerName
        }
        val width = nameColumnWidth.toInt()
        category = adjustLength(category, width)
        sb.append("[")
        sb.append(category)
        sb.append("] ")

        if (category.length > width) {
            // grow in length.
            nameColumnWidth.compareAndSet(width, category.length)
        }

        sb.append(formatMessage(record))

        sb.append(System.lineSeparator())
        if (record.thrown != null) {
            runCatching {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                record.thrown.printStackTrace(pw)
                pw.close()
                sb.append(sw.toString())
            }
        }

        return sb.toString()
    }

    fun adjustLength(packageName: String, aimLength: Int): String {
        var overflowWidth = packageName.length - aimLength

        val fragments = packageName.split(".").toMutableList()
        for (i in 0..fragments.size - 2) {
            val fragment = fragments[i]

            if (fragment.length > 1 && overflowWidth > 0) {
                val cutting = when {
                    fragment.length - 1 - overflowWidth < 0 -> fragment.length - 1
                    else -> overflowWidth
                }
                fragments[i] = fragment.substring(0, fragment.length - cutting)
                overflowWidth -= cutting
            }
        }

        return buildString {
            fragments.joinTo(this, separator = ".")
            while (length < aimLength) {
                append(" ")
            }
        }
    }
}
