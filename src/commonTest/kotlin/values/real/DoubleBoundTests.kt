package values.real

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DoubleBoundTests {
    @Test
    fun plusTest() {
        val a = DoubleBound.Finite(1.0)
        val b = DoubleBound.Finite(2.0)
        val c = a + b
        assertEquals(DoubleBound.Finite(3.0), c)

        val d = DoubleBound.NegativeInfinity + DoubleBound.PositiveInfinity
        assertNull(d)

        val e = d + c
        assertNull(e)
    }
}