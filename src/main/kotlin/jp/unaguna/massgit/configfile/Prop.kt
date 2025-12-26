package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.Main
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.io.path.toPath
import kotlin.reflect.KClass

class Prop(
    defaultUrl: URL? = null,
    systemUrl: URL? = null,
    localUrl: URL? = null,
) {
    private val default: Properties = Properties()
    private val system: Properties = Properties(default)
    private val wrapper: Properties = Properties(system)

    private val loaderUrls: Array<URL> = listOfNotNull(
        SystemProp.getSystemDir()?.toUri()?.toURL(),
    )
        .toTypedArray()
    private val loader = URLClassLoader(loaderUrls, this.javaClass.classLoader)

    init {
        load(default, defaultUrl ?: loader.getResource("massgit-default.properties"))
        load(
            system,
            systemUrl ?: getSystemDir()?.resolve("massgit-system.properties")?.toUri()?.toURL(),
        )
        load(wrapper, localUrl ?: loader.getResource("massgit-local.properties"))
    }

    @Suppress("ReturnCount")
    private fun getSystemDir(): Path? {
        SystemProp.getSystemDir()?.let { return it }

        // If launched by executing an EXE file, obtain the path to that file;
        // if launched via the java command, obtain the path to the JAR file.
        val fromClassLocation = runCatching {
            val codeSource = Main::class.java.protectionDomain.codeSource?.location?.toURI()?.toPath()
            when {
                codeSource == null -> null
                codeSource.isDirectory() -> codeSource
                else -> codeSource.parent
            }
        }.onFailure {
            // TODO: ログ出力
        }.getOrNull()
        if (fromClassLocation != null) {
            return fromClassLocation
        }

        return null
    }

    private fun load(prop: Properties, url: URL?) {
        runCatching {
            url?.openStream()?.use { inputStream ->
                prop.load(inputStream)
            }
        }.onFailure {
            // TODO: ログ出力して続行
        }.getOrNull()
    }

    fun propertyNames(): Enumeration<*> {
        return wrapper.propertyNames()
    }

    fun getProperty(key: String): String? {
        return wrapper.getProperty(key)
    }

    fun getProperty(key: Key): String? {
        return wrapper.getProperty(key.propertyName)
    }

    fun getBoolean(key: Key): Boolean? {
        require(key.type == Boolean::class) { "type of property '${key.propertyName}' is not boolean" }
        return getProperty(key)?.let { java.lang.Boolean.parseBoolean(it) }
    }

    fun getSet(key: Key): Set<String>? {
        return wrapper.getProperty(key.propertyName)?.split(",")?.map { it.trim() }?.toSet()
    }

    sealed class Key(val propertyName: String, val type: KClass<*> = String::class) {
        object KnownSubcommands : Key("subcommands.known", Set::class)
        object ProhibitedSubcommandDefault : Key("subcommands.prohibited.default", Boolean::class)
        class ProhibitedSubcommands(
            cmd: String,
        ) : Key("subcommands.prohibited.$cmd", Boolean::class)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val prop = Prop()

            // Don't use `prop.wrapper.list(System.out)`;  long values are truncated.
            prop.propertyNames().iterator().forEach {
                println("!$it=${prop.wrapper.getProperty(it as String)}")
            }
        }
    }
}
