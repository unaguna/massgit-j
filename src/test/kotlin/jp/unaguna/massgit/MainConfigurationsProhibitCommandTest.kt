package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Prop
import jp.unaguna.massgit.testcommon.io.createTempTextFile
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals

class MainConfigurationsProhibitCommandTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "false,cmd1,true",
            "false,cmd2,false",
            "false,cmd3,false",
            "true,cmd1,true",
            "true,cmd2,false",
            "true,cmd3,true",
        ]
    )
    fun `test if subcommand is prohibited`(
        defaultProhibit: Boolean,
        subcommand: String,
        expectedProhibit: Boolean,
        @TempDir tempDir: Path
    ) {
        val (options, _) = MassgitOptions.build(emptyList())
        val propPath = createTempTextFile(tempDir, "prop") {
            println("subcommands.known=cmd1,cmd2,cmd3")
            println("subcommands.prohibited.default=$defaultProhibit")
            println("subcommands.prohibited.cmd1=true")
            println("subcommands.prohibited.cmd2=false")
        }
        val emptyUrl = createTempFile(tempDir, "empty").toUri().toURL()
        val prop = Prop(propPath.toUri().toURL(), emptyUrl, emptyUrl)

        val conf = MainConfigurations(options, prop)

        assertEquals(expectedProhibit, conf.prohibitSubcommand(subcommand))
    }
}
