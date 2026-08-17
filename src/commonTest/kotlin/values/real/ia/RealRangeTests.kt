package values.real.ia
import io.github.tukcps.aadd.values.real.DoubleBound
import kotlin.test.*
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.exp
import io.github.tukcps.aadd.values.real.ia.ln
import io.github.tukcps.aadd.values.real.ia.minus
import io.github.tukcps.aadd.values.real.ia.plus
import io.github.tukcps.aadd.values.real.plus
import kotlin.math.*
import kotlin.ranges.rangeTo

class RealRangeTests {

    @Test
    fun rangeEqualsTestRange() {
        val realRangeA = RealRange(3.0..3.0)
        val realRangeB = RealRange(3.0..3.0)
        assertEquals(realRangeA,realRangeB)
    }

    @Test
    fun rangeEqualsTestScalar() {
        val realRangeA = RealRange(2.0..2.0)
        val realRangeB = RealRange(2.0..2.0)
        println(realRangeA == realRangeB)
    }

    val a = RealRange(-1.0, 2.0)
    val b = RealRange(2.0, 3.0)

    /** A real is the range +-MAX_VALUE. It is identified by isReal() */
    @Test fun realTest() {
        val real = RealRange(RealRange.Reals)
        assertEquals(DoubleBound.NegativeInfinity, real.min)
        assertEquals(DoubleBound.PositiveInfinity, real.max)
        assertTrue(real.isReals())
    }

    @Test
    fun testPlus() {
        val c = a+b
        assertEquals(c.min, a.min+b.min)
        assertEquals(c.max, a.max+b.max)
    }

    @Test
    fun testMinus() {
        val c = a-b
        assertEquals(c.max.finiteValue,
            max(a.min.finiteValue - b.max.finiteValue, b.min.finiteValue - a.max.finiteValue)
        )
        assertEquals(c.min.finiteValue,
            min(a.min.finiteValue - b.max.finiteValue, b.min.finiteValue - a.max.finiteValue)
        )
    }

    @Test
    fun testExp() {
        val c = exp(RealRange(1.0, 2.0))
        assertEquals(exp(1.0) .. exp(2.0), c, 0.0000000001)
    }

    @Test
    fun testLn() {
        val c = ln(RealRange(1.0, 2.0))
        assertEquals(ln(1.0) .. ln(2.0), c, 0.0000000001)
    }
}