package values.integer

import io.github.tukcps.aadd.values.integer.Bound
import io.github.tukcps.aadd.values.integer.bound
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoundTest {

    @Test
    fun compareTo() {
        assertTrue(Bound.NegativeInfinity < (-5).bound())
        assertTrue((-5).bound() < 0.bound())
        assertTrue(0.bound() < 5.bound())
        assertTrue(5.bound() < Bound.PositiveInfinity)
        assertTrue(Bound.PositiveInfinity < Bound.NaN)

        assertEquals(0, Bound.NegativeInfinity.compareTo(Bound.NegativeInfinity))
        assertEquals(0, Bound.PositiveInfinity.compareTo(Bound.PositiveInfinity))
        assertEquals(0, Bound.NaN.compareTo(Bound.NaN))
        assertEquals(0, 5.bound().compareTo(5.bound()))

        assertTrue(Bound.NegativeInfinity < Bound.PositiveInfinity)
        assertTrue(Bound.NegativeInfinity < Bound.NaN)
        assertTrue(Bound.PositiveInfinity < Bound.NaN)
    }

    @Test
    fun isFinite() {
        assertTrue(0.bound().isFinite)
        assertTrue(42.bound().isFinite)
        assertTrue((-42).bound().isFinite)

        assertFalse(Bound.PositiveInfinity.isFinite)
        assertFalse(Bound.NegativeInfinity.isFinite)
        assertFalse(Bound.NaN.isFinite)
    }

    @Test
    fun isInfinite() {
        assertFalse(0.bound().isInfinite)
        assertFalse(1.bound().isInfinite)

        assertTrue(Bound.PositiveInfinity.isInfinite)
        assertTrue(Bound.NegativeInfinity.isInfinite)
        assertFalse(Bound.NaN.isInfinite)
    }

    @Test
    fun isNaN() {
        assertFalse(0.bound().isNaN)
        assertFalse(Bound.PositiveInfinity.isNaN)
        assertFalse(Bound.NegativeInfinity.isNaN)

        assertTrue(Bound.NaN.isNaN)
    }

    @Test
    fun isZero() {
        assertTrue(0.bound().isZero)

        assertFalse(1.bound().isZero)
        assertFalse((-1).bound().isZero)
        assertFalse(Bound.PositiveInfinity.isZero)
        assertFalse(Bound.NegativeInfinity.isZero)
        assertFalse(Bound.NaN.isZero)
    }

    @Test
    fun sign() {
        assertEquals(-1, (-42).bound().sign)
        assertEquals(0, 0.bound().sign)
        assertEquals(1, 42.bound().sign)

        assertEquals(-1, Bound.NegativeInfinity.sign)
        assertEquals(1, Bound.PositiveInfinity.sign)
        assertEquals(0, Bound.NaN.sign)
    }

    @Test
    fun boundExtension() {
        assertEquals(Bound.Finite(0), 0.bound())
        assertEquals(Bound.Finite(42), 42.bound())
        assertEquals(Bound.Finite(-42), (-42).bound())
        assertEquals(Bound.Finite(Long.MIN_VALUE), Long.MIN_VALUE.bound())
        assertEquals(Bound.Finite(Long.MAX_VALUE), Long.MAX_VALUE.bound())
    }
}