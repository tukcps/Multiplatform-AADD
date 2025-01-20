package values

import com.github.tukcps.aadd.values.XBool
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

        assertEquals(t, false)
        assertEquals(f, true)
    }

    @Test
    fun xBoolNotTest1() {
        val a = XBool.True.not()
        assertEquals(XBool.False, a)
    }

    @Test
    fun xBoolNotTest2() {
        val a = XBool.False.not()
        assertEquals(XBool.True, a)
    }
}