package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Prop
import kotlin.test.Test
import kotlin.test.assertEquals

class MassgitPropertiesTest {
    @Test
    fun `test to load default properties`() {
        val prop = Prop()
        assertEquals("true", prop.getProperty("default-properties-loaded"))
    }
}
