package jp.unaguna.massgit.common.args

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class OptionsTest {
    @Test
    fun `test each values`() {
        val args = listOf("--opt1", "--arg1", "arg1-1", "--arg2", "arg2-1", "--arg1", "arg1-2", "--arg2", "arg2-2")
        val (actualOptions, _) = Options.build(args, SampleOptionDefProvider)

        val opt1List = actualOptions.of(SampleOptionDef.OPT1)
        assertEquals(1, opt1List.size)
        assertEquals(SampleOptionDef.OPT1, opt1List[0].def)
        assertContentEquals(emptyList(), opt1List[0].args)
        assertEquals(0, opt1List[0].order)
        val opt2List = actualOptions.of(SampleOptionDef.OPT2)
        assertEquals(0, opt2List.size)
        val arg1List = actualOptions.of(SampleOptionDef.ARG1)
        assertEquals(2, arg1List.size)
        assertEquals(SampleOptionDef.ARG1, arg1List[0].def)
        assertContentEquals(listOf("arg1-1"), arg1List[0].args)
        assertEquals(1, arg1List[0].order)
        assertEquals(SampleOptionDef.ARG1, arg1List[1].def)
        assertContentEquals(listOf("arg1-2"), arg1List[1].args)
        assertEquals(3, arg1List[1].order)
        val arg2List = actualOptions.of(SampleOptionDef.ARG2)
        assertEquals(2, arg2List.size)
        assertEquals(SampleOptionDef.ARG2, arg2List[0].def)
        assertContentEquals(listOf("arg2-1"), arg2List[0].args)
        assertEquals(2, arg2List[0].order)
        assertEquals(SampleOptionDef.ARG2, arg2List[1].def)
        assertContentEquals(listOf("arg2-2"), arg2List[1].args)
        assertEquals(4, arg2List[1].order)
    }
}

private enum class SampleOptionDef(override val representativeName: String, val argsNum: Int) : OptionDef {
    OPT1("--opt1", 0),
    OPT2("--opt2", 0),
    ARG1("--arg1", 1),
    ARG2("--arg2", 1),
    ;

    override fun sufficient(actualNum: Int): Boolean {
        return actualNum >= argsNum
    }
}

private object SampleOptionDefProvider : OptionDefProvider<SampleOptionDef> {
    private val optionDef: Map<String, SampleOptionDef> = SampleOptionDef.entries
        .associateBy { it.representativeName }

    override fun getOptionDef(name: String): SampleOptionDef {
        return optionDef.getOrElse(name) {
            error("Unknown option: $name")
        }
    }
}
