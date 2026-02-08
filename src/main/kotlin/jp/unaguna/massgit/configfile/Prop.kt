package jp.unaguna.massgit.configfile

import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*
import kotlin.io.path.exists
import kotlin.reflect.KClass

class Prop(
    defaultUrl: URL? = null,
    defaultByExecutableTypeUrl: URL? = null,
    systemUrl: URL? = null,
    systemByExecutableTypeUrl: URL? = null,
    localUrl: URL? = null,
    localByExecutableTypeUrl: URL? = null,
) {
    private val logger = LoggerFactory.getLogger(Prop::class.java)
    private val default: Properties = Properties()
    private val system: Properties = Properties(default)
    private val wrapper: Properties = Properties(system)

    private val loader = this.javaClass.classLoader

    init {
        val executableType = SystemProp.executableType?.name?.lowercase()

        load(default, defaultUrl ?: loader.getResource("massgit-default.properties"))
        if (executableType != null) {
            load(
                default,
                defaultByExecutableTypeUrl
                    ?: loader.getResource("massgit-default-$executableType.properties")
            )
        }

        load(
            system,
            systemUrl ?: SystemProp.systemDir?.resolve("massgit-system.properties")
                ?.takeIf { it.exists() }?.toUri()?.toURL(),
        )
        if (executableType != null) {
            load(
                system,
                systemByExecutableTypeUrl ?: SystemProp.systemDir?.resolve("massgit-system-$executableType.properties")
                    ?.takeIf { it.exists() }?.toUri()?.toURL(),
            )
        }

        load(wrapper, localUrl ?: loader.getResource("massgit-local.properties"))
        if (executableType != null) {
            load(
                wrapper,
                localByExecutableTypeUrl ?: loader.getResource("massgit-local-$executableType.properties")
            )
        }

        if (logger.isDebugEnabled) {
            propertyNames().asIterator().forEach {
                it as String
                logger.debug("Massgit Prop: {}={}", it, getProperty(it))
            }
        }
    }

    private fun load(prop: Properties, url: URL?) {
        runCatching {
            url?.openStream()?.use { inputStream ->
                prop.load(inputStream)
            }
        }.onFailure { e ->
            logger.warn("Could not load properties file '$url'", e)
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

    fun getPropertiesAsSequence(prefix: KeyPrefix): Sequence<KeyValue<String>> {
        val prefixStr = prefix.propertyPrefix + "."
        return wrapper.propertyNames().asSequence()
            .map { it as String }
            .filter { it.startsWith(prefixStr) }
            .map { KeyValue(it, wrapper.getProperty(it)) }
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
        object ResetStdoutEncoding : Key("stdout.encoding.reset", Boolean::class)
        object ResetStderrEncoding : Key("stderr.encoding.reset", Boolean::class)
    }

    sealed class KeyPrefix(val propertyPrefix: String, val type: KClass<*> = String::class) {
        object GitConfig : KeyPrefix("git.config", String::class)
    }

    data class KeyValue<V>(
        val key: String,
        val value: V,
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val prop = Prop()

            // Don't use `prop.wrapper.list(System.out)`;  long values are truncated.
            prop.propertyNames().iterator().forEach {
                println("$it=${prop.wrapper.getProperty(it as String)}")
            }
        }
    }
}
