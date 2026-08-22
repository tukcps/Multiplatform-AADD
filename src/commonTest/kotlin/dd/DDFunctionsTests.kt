package dd

import io.github.tukcps.aadd.Integer
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.DD
import io.github.tukcps.aadd.dd.div
import io.github.tukcps.aadd.dd.intersect
import io.github.tukcps.aadd.dd.ite
import io.github.tukcps.aadd.dd.minus
import io.github.tukcps.aadd.dd.plus
import io.github.tukcps.aadd.dd.times
import io.github.tukcps.aadd.util.Assertions
import io.github.tukcps.aadd.values.real.ia.RealRange
import testutil.ddTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DDFunctionsTests {

    /**
     * Check that typical use case parses and does not lead into infinite self-recursion.
     * Typical use case: development of methods that work with erased type information.
     */
    @Test
    fun testPlus() = ddTest {
        val select = true
        val a = if (select) real(1.0) else integer(2)
        val b = if (select) real(2.0) else integer(3)
        val c = a + b
        assertTrue(c is Real)
        assertEquals(3.0, c.min.finiteValue)
    }

    @Test
    fun testPlusDouble() = ddTest {
        val select = true
        val a = if (select) real(1.0) else integer(2)
        val c = a + 2.0
        assertTrue(c is Real)
        assertEquals(3.0, c.min.finiteValue)
    }

    @Test
    fun testMinus() = ddTest {
        val select = true
        val a = if (select) real(1.0) else integer(2)
        val b = if (select) real(2.0) else integer(3)
        val c = a - b
        assertTrue(c is Real)
        assertEquals(-1.0, c.min.finiteValue)
    }

    @Test
    fun testTimesTest() = ddTest {
        val select = true
        val a = if (select) real(1.0) else integer(2)
        val b = if (select) real(2.0) else integer(3)
        val c = a * b
        assertTrue(c is Real)
        assertEquals(2.0, c.min.finiteValue)
    }

    @Test
    fun testDivTest() = ddTest {
        for (select in listOf(true, false)) {
            val a = if (select) real(1.0) else integer(2)
            val b = if (select) real(2.0) else integer(3)
            val c = a / b
            if (select) {
                assertTrue(c is Real)
                assertEquals(0.5, c.min.finiteValue, 0.0000001)
            } else {
                assertTrue(c is Integer)
                assertEquals(0L, c.min.finiteValue)
            }
        }
    }

    @Test
    fun iteTest() = ddTest {
        val select: DD<*> = real(1.0..2.0) greaterThanOrEquals real(0.5..1.5)
        val a: DD<*> = select.ite(real(1.0) as DD<*>, real(2.0) as DD<*>)
        Assertions.assertSafeInclusion(1.0..2.0, a as AADD, 1e-10)
    }

    @Test
    fun testInequality() = ddTest {
        val a = real(1.0) as DD<*>
        val b = real(2.0) as DD<*>
        val c = a == b
        assertEquals(false, c)
    }

    @Test
    fun testIntersectTest() = ddTest {
        val a = real(1.0..3.0) as DD<*>
        val b = real(2.0..3.0) as DD<*>
        val c = a intersect b
        Assertions.assertSafeInclusion(2.0..3.0, c.asAadd(), 1e-10)
    }

    @Test
    fun testIntersectTest2() = ddTest {
        val a = real(1.0..3.0)
        val b = RealRange(2.0, 3.0)
        val c = a intersect b
        Assertions.assertSafeInclusion(2.0..3.0, c.asAadd(), 1e-10)
    }
}