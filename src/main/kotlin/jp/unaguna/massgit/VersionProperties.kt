package jp.unaguna.massgit

import java.util.Properties

object VersionProperties {
    private val props by lazy {
        Properties().apply {
            VersionProperties.javaClass.classLoader.getResourceAsStream("massgit-version.properties")
                ?.use { load(it) }
        }
    }

    fun getVersion(): String {
        return props.getProperty("version")
            ?: error("cannot load the version number")
    }
}
