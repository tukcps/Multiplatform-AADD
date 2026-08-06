package values.bool

import io.github.tukcps.aadd.values.bool.XBool
import kotlin.test.Test
import kotlin.test.assertEquals

class XBoolTest {
    @Test
    fun xBoolComparisonTest() {
        val a = XBool.True
        var t = a.equals(true)
        var f = a.equals(false)
        assertEquals(true, t)
        assertEquals(false, f)

        val b = XBool.False
        t = b.equals(true)
        f = b.equals(false)

        assertEquals(false, t)
        assertEquals(true, f)
    }
}