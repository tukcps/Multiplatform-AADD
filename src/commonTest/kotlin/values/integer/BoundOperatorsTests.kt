package values.integer

import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.integer.abs
import io.github.tukcps.aadd.values.integer.unaryMinus
import io.github.tukcps.aadd.values.integer.plus
import io.github.tukcps.aadd.values.integer.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BoundOperatorsTests {

    @Test
    fun unaryMinusFinite() {
        assertEquals(LongBound.Finite(-5), -LongBound.Finite(5))
        assertEquals(LongBound.Finite(5), -LongBound.Finite(-5))
    }

    @Test
    fun unaryMinusOverflow() {
        assertEquals(
            LongBound.PositiveInfinity,
            -LongBound.Finite(Long.MIN_VALUE)
        )
    }

    @Test
    fun unaryMinusInfinity() {
        assertEquals(LongBound.PositiveInfinity, -LongBound.NegativeInfinity)
        assertEquals(LongBound.NegativeInfinity, -LongBound.PositiveInfinity)
    }

    @Test
    fun absFinite() {
        assertEquals(LongBound.Finite(5), abs(LongBound.Finite(-5)))
        assertEquals(LongBound.Finite(5), abs(LongBound.Finite(5)))
    }

    @Test
    fun absInfinity() {
        assertEquals(LongBound.PositiveInfinity, abs(LongBound.PositiveInfinity))
        assertEquals(LongBound.PositiveInfinity, abs(LongBound.NegativeInfinity))
    }

    @Test
    fun plusFinite() {
        assertEquals(LongBound.Finite(8), LongBound.Finite(3) + LongBound.Finite(5))
    }

    @Test
    fun plusOverflow() {
        assertEquals(
            LongBound.PositiveInfinity,
            LongBound.Finite(Long.MAX_VALUE) + LongBound.Finite(1)
        )
    }

    @Test
    fun plusInfinity() {
        assertEquals(LongBound.PositiveInfinity, LongBound.PositiveInfinity + LongBound.Finite(5))
        assertEquals(LongBound.NegativeInfinity, LongBound.NegativeInfinity + LongBound.Finite(5))
    }

    @Test
    fun plusOppositeInfinity() {
        assertNull(LongBound.PositiveInfinity + LongBound.NegativeInfinity)
    }

    @Test
    fun minusFinite() {
        assertEquals(LongBound.Finite(2), LongBound.Finite(5) - LongBound.Finite(3))
    }
}