package values.integer

import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.integer.LongBound.NegativeInfinity.eq
import io.github.tukcps.aadd.values.integer.LongMath
import io.github.tukcps.aadd.values.integer.LongMath.isNegative
import io.github.tukcps.aadd.values.integer.LongMath.isPositive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LongBoundMathTest {

    private fun b(value: Long) = LongBound.Finite(value)

    private val pInf = LongBound.PositiveInfinity
    private val nInf = LongBound.NegativeInfinity
    private val nan: LongBound? = null

    @Test
    fun testCompare() {
        // Inequality
        assertEquals(0, LongMath.compare(b(5), b(5)))
        assertTrue(LongMath.compare(b(4), b(5)) < 0)
        assertTrue(LongMath.compare(b(5), b(4)) > 0)

        assertTrue(LongMath.compare(nInf, b(0)) < 0)
        assertTrue(LongMath.compare(pInf, b(0)) > 0)
        assertTrue(LongMath.compare(pInf, nInf) > 0)

        assertTrue(pInf > nInf)
        assertTrue(LongBound.Finite(0L) eq 0L)
        assertEquals(pInf, pInf)
    }

    @Test
    fun testNegate() {
        assertEquals(b(-7), LongMath.negate(b(7)))
        assertEquals(b(7), LongMath.negate(b(-7)))
        assertEquals(b(0), LongMath.negate(b(0)))

        assertEquals(pInf, LongMath.negate(nInf))
        assertEquals(nInf, LongMath.negate(pInf))

        assertEquals(LongBound.PositiveInfinity, LongMath.negate(b(Long.MIN_VALUE)))
        assertEquals(LongBound.Finite(Long.MIN_VALUE+1), LongMath.negate(b(Long.MAX_VALUE)))
    }

    @Test
    fun testAbs() {
        assertEquals(b(5), LongMath.abs(b(5)))
        assertEquals(b(5), LongMath.abs(b(-5)))
        assertEquals(b(0), LongMath.abs(b(0)))

        assertEquals(pInf, LongMath.abs(pInf))
        assertEquals(pInf, LongMath.abs(nInf))
        // assertNull(LongMath.abs(nan))

        assertEquals(pInf, LongMath.abs(b(Long.MIN_VALUE)))
    }

    @Test
    fun testMin() {
        assertEquals(b(2), LongMath.min(b(2), b(3)))
        assertEquals(nInf, LongMath.min(nInf, b(3)))

        assertEquals(
            b(-5),
            LongMath.min(
                b(7),
                b(2),
                b(-5),
                b(11)
            )
        )

        assertEquals(
            nInf,
            LongMath.min(
                b(7),
                nInf,
                b(2),
                pInf
            )
        )

        assertEquals(b(-3), LongMath.min(b(-3)))

        // assertNull(LongMath.min(b(7), pInf, nan))
    }

    @Test
    fun testMax() {
        assertEquals(b(3), LongMath.max(b(2), b(3)))
        assertEquals(pInf, LongMath.max(pInf, b(3)))

        assertEquals(
            b(11),
            LongMath.max(
                b(7),
                b(2),
                b(-5),
                b(11)
            )
        )

        assertEquals(
            pInf,
            LongMath.max(
                b(7),
                nInf,
                b(2),
                pInf
            )
        )

        assertEquals(b(-3), LongMath.max(b(-3)))

        // assertNull(LongMath.max(b(7), pInf, nan))
    }

    @Test
    fun testIsPositiveNegative() {
        assertTrue(b(5).isPositive())
        assertFalse(b(-5).isPositive())

        assertTrue(b(-5).isNegative())
        assertFalse(b(5).isNegative())

        assertTrue(pInf.isPositive())
        assertTrue(nInf.isNegative())

        assertNull(nan?.isPositive())
        assertNull(nan?.isNegative())
    }

    @Test
    fun testMinMaxEmpty() {
        assertFailsWith<IllegalArgumentException> {
            LongMath.min()
        }

        assertFailsWith<IllegalArgumentException> {
            LongMath.max()
        }
    }

    @Test
    fun testEq() {
        val b1 = 1L eq LongBound.PositiveInfinity
        val b2 = LongBound.NegativeInfinity eq 1000L
        assertEquals(false, b2)
        assertEquals(false, b1)
    }
}