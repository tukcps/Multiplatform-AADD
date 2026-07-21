package values.real

import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.RealRange
import kotlin.test.Test
import kotlin.test.assertTrue

class RealsTest {
    @Test
     fun testRealsAdd() {
        val a = RealRange(1.0 .. 2.0)
        val b = RealRange(2.0 .. 3.0)
        val c = a + b
        assertTrue(c == RealRange(3.0, 5.0))
    }

    @Test
     fun testIntsAdd() {
        val a = IntegerRange(1 ,2)
        val b = IntegerRange(2 , 3)
        val c = a + b
        assertTrue(c == IntegerRange(3, 5))
    }

}