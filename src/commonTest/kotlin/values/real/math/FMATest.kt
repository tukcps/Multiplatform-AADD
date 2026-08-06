package values.real.math

import io.github.tukcps.aadd.values.real.rounding.FMA
import kotlin.test.Test
import kotlin.test.assertEquals

class FMATest {
    @Test
    fun testFma() {
        val x = FMA.compute(2.0, 3.0, 4.0)
        assertEquals(10.0, x)
    }
}