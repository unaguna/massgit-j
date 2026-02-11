package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Prop
import jp.unaguna.massgit.configfile.SystemProp
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writer
import kotlin.test.Test
import kotlin.test.assertEquals

class MassgitPropertiesTest {
    @Test
    fun `test to load default properties`() {
        val prop = Prop()
        assertEquals("true", prop.getProperty("default-properties-loaded"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["JAVA", "EXE"])
    fun `test to load default properties by executable type`(
        executableType: SystemProp.ExecutableType,
        @TempDir tmpDir: Path,
    ) {
        val systemProp = MockSystemProp(
            systemDir = tmpDir.resolve("system-dir").createDirectories(),
            executableType = executableType,
        )
        val prop = Prop(systemProp)

        val actualValueForJava = prop.getProperty("default-properties-for-java-loaded")
        val actualValueForExe = prop.getProperty("default-properties-for-exe-loaded")
        when (executableType) {
            SystemProp.ExecutableType.JAVA -> {
                assertEquals("true", actualValueForJava)
                assertEquals(null, actualValueForExe)
            }
            SystemProp.ExecutableType.EXE -> {
                assertEquals(null, actualValueForJava)
                assertEquals("true", actualValueForExe)
            }
        }
    }

    @Test
    fun `test to load system properties`(@TempDir tmpDir: Path) {
        val systemDir = tmpDir.resolve("system-dir").createDirectories()
        systemDir.resolve("massgit-system.properties").writer().use { writer ->
            writer.write("system-properties-loaded=true\n")
        }

        val systemProp = MockSystemProp(systemDir, SystemProp.ExecutableType.JAVA)
        val prop = Prop(systemProp)
        assertEquals("true", prop.getProperty("system-properties-loaded"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["JAVA", "EXE"])
    fun `test to load system properties by executable type`(
        executableType: SystemProp.ExecutableType,
        @TempDir tmpDir: Path,
    ) {
        val systemDir = tmpDir.resolve("system-dir").createDirectories()
        systemDir.resolve("massgit-system.properties").writer().use { writer ->
            writer.write("system-properties-loaded=true\n")
        }
        systemDir.resolve("massgit-system-java.properties").writer().use { writer ->
            writer.write("system-properties-for-java-loaded=true\n")
        }
        systemDir.resolve("massgit-system-exe.properties").writer().use { writer ->
            writer.write("system-properties-for-exe-loaded=true\n")
        }

        val systemProp = MockSystemProp(systemDir, executableType)
        val prop = Prop(systemProp)

        val actualValueForJava = prop.getProperty("system-properties-for-java-loaded")
        val actualValueForExe = prop.getProperty("system-properties-for-exe-loaded")
        when (executableType) {
            SystemProp.ExecutableType.JAVA -> {
                assertEquals("true", actualValueForJava)
                assertEquals(null, actualValueForExe)
            }
            SystemProp.ExecutableType.EXE -> {
                assertEquals(null, actualValueForJava)
                assertEquals("true", actualValueForExe)
            }
        }
    }

    // TODO: local プロパティを実装するときに試験を作る
}

private class MockSystemProp(
    override val systemDir: Path,
    override val executableType: SystemProp.ExecutableType,
) : SystemProp {
    override val logbackConfig: Path
        get() = throw NotImplementedError()
}
