package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.common.io.InputStreamSource
import jp.unaguna.massgit.common.io.UrlResource
import jp.unaguna.massgit.common.io.toResource
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

    init {
        val propResources = listOfNotNull(
            Pair(default, MassgitPropertiesDefaultResource(defaultUrl)),
            SystemProp.executableType?.let {
                Pair(
                    default,
                    MassgitPropertiesDefaultByExecutableTypeResource(it, defaultByExecutableTypeUrl)
                )
            },
            Pair(system, MassgitPropertiesSystemResource(systemUrl)),
            SystemProp.executableType?.let {
                Pair(
                    system,
                    MassgitPropertiesSystemByExecutableResource(
                        it,
                        systemByExecutableTypeUrl
                    )
                )
            },
            Pair(wrapper, MassgitPropertiesLocalResource(localUrl)),
            SystemProp.executableType?.let {
                Pair(
                    wrapper,
                    MassgitPropertiesLocalByExecutableTypeResource(
                        it,
                        localByExecutableTypeUrl
                    )
                )
            },
        )

        if (SystemProp.executableType == null) {
            logger.warn(
                "Massgit properties file by executable type will NOT be loaded; Executable type was not be detected"
            )
        }

        for ((prop, propResource) in propResources) {
            load(prop, propResource)
        }

        if (logger.isDebugEnabled) {
            propertyNames().asIterator().forEach {
                it as String
                logger.debug("Massgit Prop: {}={}", it, getProperty(it))
            }
        }
    }

    private fun load(prop: Properties, propResource: MassgitPropertiesResource) {
        val resource = propResource.resource
        if (resource == null) {
            logger.debug("canceled to load {}; file is not found", propResource.nameForLogging)
            return
        }

        runCatching {
            resource.getInputStream().use { inputStream ->
                prop.load(inputStream)
            }
        }.onSuccess {
            logger.debug("completed to load {}", propResource.nameForLogging)
        }.onFailure { e ->
            logger.warn("failed to load {}", propResource.nameForLogging, e)
        }
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

private interface MassgitPropertiesResource {
    val nameForLogging: String
    val resource: InputStreamSource?
}

private class MassgitPropertiesDefaultResource(inject: InputStreamSource? = null) : MassgitPropertiesResource {
    override val nameForLogging = "default properties"
    override val resource = inject ?: UrlResource.loadFromClasspathIfExist("massgit-default.properties")

    constructor(inject: URL?) : this(inject?.toResource())
}

private class MassgitPropertiesDefaultByExecutableTypeResource(
    executableType: SystemProp.ExecutableType,
    inject: InputStreamSource? = null,
) : MassgitPropertiesResource {
    override val nameForLogging = "default properties by executable type"
    override val resource = inject
        ?: UrlResource.loadFromClasspathIfExist("massgit-default-${executableType.nameForFilename}.properties")

    constructor(executableType: SystemProp.ExecutableType, inject: URL?) : this(executableType, inject?.toResource())
}

private class MassgitPropertiesSystemResource(inject: InputStreamSource? = null) : MassgitPropertiesResource {
    override val nameForLogging = "system properties"
    override val resource = inject ?: SystemProp.systemDir?.resolve("massgit-system.properties")?.takeIf {
        it.exists()
    }?.toResource()

    constructor(inject: URL?) : this(inject?.toResource())
}

private class MassgitPropertiesSystemByExecutableResource(
    executableType: SystemProp.ExecutableType,
    inject: InputStreamSource? = null,
) : MassgitPropertiesResource {
    override val nameForLogging = "system properties by executable type"
    override val resource = inject
        ?: SystemProp.systemDir?.resolve("massgit-system-${executableType.nameForFilename}.properties")?.takeIf {
            it.exists()
        }?.toResource()

    constructor(executableType: SystemProp.ExecutableType, inject: URL?) : this(executableType, inject?.toResource())
}

private class MassgitPropertiesLocalResource(inject: InputStreamSource? = null) : MassgitPropertiesResource {
    override val nameForLogging = "local properties"
    override val resource = inject ?: UrlResource.loadFromClasspathIfExist("massgit-local.properties")

    constructor(inject: URL?) : this(inject?.toResource())
}

private class MassgitPropertiesLocalByExecutableTypeResource(
    executableType: SystemProp.ExecutableType,
    inject: InputStreamSource? = null,
) : MassgitPropertiesResource {
    override val nameForLogging = "local properties by executable type"
    override val resource = inject
        ?: UrlResource.loadFromClasspathIfExist("massgit-local-${executableType.nameForFilename}.properties")

    constructor(executableType: SystemProp.ExecutableType, inject: URL?) : this(executableType, inject?.toResource())
}
