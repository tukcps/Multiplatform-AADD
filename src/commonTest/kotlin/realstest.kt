package examples

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.Range
import kotlin.test.Test
import kotlin.test.assertTrue

class RealsTest {
    @Test
    // TODO wieso failen die Tests ?
     fun testRealsAdd() {
        val a = Range(1.0 .. 2.0)
        val b = Range(2.0 .. 3.0)
        val c = a + b
        assertTrue(c == Range(3.0, 5.0))
    }

    @Test
     fun testIntsAdd() {
        val a = IntegerRange(1 ,2)
        val b = IntegerRange(2 , 3)
        val c = a + b
        assertTrue(c == IntegerRange(3, 5))
    }

}