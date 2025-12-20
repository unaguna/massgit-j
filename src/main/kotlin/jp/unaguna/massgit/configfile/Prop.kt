package jp.unaguna.massgit.configfile

import java.net.URL
import java.net.URLClassLoader
import java.util.*
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
        load(system, systemUrl ?: loader.getResource("massgit-system.properties"))
        load(wrapper, localUrl ?: loader.getResource("massgit-local.properties"))
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

            prop.wrapper.list(System.out)
        }
    }
}
