package values.integer

import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.integer.LongMath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LongMathInfTest {

    private fun b(value: Long) = LongBound.Finite(value)

    @Test
    fun testAddInfinity() {
        assertEquals(LongBound.PositiveInfinity, LongMath.add(LongBound.PositiveInfinity, b(1)))
        assertEquals(LongBound.PositiveInfinity, LongMath.add(b(1), LongBound.PositiveInfinity))
        assertEquals(LongBound.NegativeInfinity, LongMath.add(LongBound.NegativeInfinity, b(1)))
        assertEquals(LongBound.NegativeInfinity, LongMath.add(b(1), LongBound.NegativeInfinity))

        assertEquals(LongBound.PositiveInfinity, LongMath.add(LongBound.PositiveInfinity, LongBound.PositiveInfinity))
        assertEquals(LongBound.NegativeInfinity, LongMath.add(LongBound.NegativeInfinity, LongBound.NegativeInfinity))

        assertNull(LongMath.add(LongBound.PositiveInfinity, LongBound.NegativeInfinity))
        assertNull(LongMath.add(LongBound.NegativeInfinity, LongBound.PositiveInfinity))
    }

    @Test
    fun testSubtractInfinity() {
        assertEquals(LongBound.PositiveInfinity, LongMath.subtract(LongBound.PositiveInfinity, b(1)))
        assertEquals(LongBound.NegativeInfinity, LongMath.subtract(LongBound.NegativeInfinity, b(1)))

        assertEquals(LongBound.PositiveInfinity, LongMath.subtract(LongBound.PositiveInfinity, LongBound.NegativeInfinity))
        assertEquals(LongBound.NegativeInfinity, LongMath.subtract(LongBound.NegativeInfinity, LongBound.PositiveInfinity))

        assertNull(LongMath.subtract(LongBound.PositiveInfinity, LongBound.PositiveInfinity))
        assertNull(LongMath.subtract(LongBound.NegativeInfinity, LongBound.NegativeInfinity))
    }

    @Test
    fun testMultiplyInfinity() {
        assertEquals(LongBound.PositiveInfinity, LongMath.multiply(LongBound.PositiveInfinity, b(2)))
        assertEquals(LongBound.NegativeInfinity, LongMath.multiply(LongBound.PositiveInfinity, b(-2)))

        assertEquals(LongBound.PositiveInfinity, LongMath.multiply(LongBound.NegativeInfinity, b(-2)))
        assertEquals(LongBound.NegativeInfinity, LongMath.multiply(LongBound.NegativeInfinity, b(2)))

        assertEquals(LongBound.PositiveInfinity, LongMath.multiply(LongBound.PositiveInfinity, LongBound.PositiveInfinity))
        assertEquals(LongBound.NegativeInfinity, LongMath.multiply(LongBound.PositiveInfinity, LongBound.NegativeInfinity))
        assertEquals(LongBound.PositiveInfinity, LongMath.multiply(LongBound.NegativeInfinity, LongBound.NegativeInfinity))

        assertNull(LongMath.multiply(LongBound.PositiveInfinity, b(0)))
        assertNull(LongMath.multiply(b(0), LongBound.PositiveInfinity))
        assertNull(LongMath.multiply(LongBound.NegativeInfinity, b(0)))
        assertNull(LongMath.multiply(b(0), LongBound.NegativeInfinity))
    }

    @Test
    fun testDivideInfinity() {
        assertEquals(LongBound.PositiveInfinity, LongMath.divide(LongBound.PositiveInfinity, b(2)))
        assertEquals(LongBound.NegativeInfinity, LongMath.divide(LongBound.PositiveInfinity, b(-2)))

        assertEquals(LongBound.PositiveInfinity, LongMath.divide(LongBound.NegativeInfinity, b(-2)))
        assertEquals(LongBound.NegativeInfinity, LongMath.divide(LongBound.NegativeInfinity, b(2)))

        assertEquals(b(0), LongMath.divide(b(5), LongBound.PositiveInfinity))
        assertEquals(b(0), LongMath.divide(b(-5), LongBound.NegativeInfinity))

        assertNull(LongMath.divide(LongBound.PositiveInfinity, LongBound.PositiveInfinity))
        assertNull(LongMath.divide(LongBound.PositiveInfinity, LongBound.NegativeInfinity))
        assertNull(LongMath.divide(LongBound.NegativeInfinity, LongBound.PositiveInfinity))
        assertNull(LongMath.divide(LongBound.NegativeInfinity, LongBound.NegativeInfinity))
    }
}