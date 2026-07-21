package values.integer

import io.github.tukcps.aadd.values.integer.Bound
import io.github.tukcps.aadd.values.integer.abs
import io.github.tukcps.aadd.values.integer.unaryMinus
import io.github.tukcps.aadd.values.integer.plus
import io.github.tukcps.aadd.values.integer.minus
import kotlin.test.Test
import kotlin.test.assertEquals

class BoundOperatorsTests {

    @Test
    fun unaryMinusFinite() {
        assertEquals(Bound.Finite(-5), -Bound.Finite(5))
        assertEquals(Bound.Finite(5), -Bound.Finite(-5))
    }

    @Test
    fun unaryMinusOverflow() {
        assertEquals(
            Bound.PositiveInfinity,
            -Bound.Finite(Long.MIN_VALUE)
        )
    }

    @Test
    fun unaryMinusInfinity() {
        assertEquals(Bound.PositiveInfinity, -Bound.NegativeInfinity)
        assertEquals(Bound.NegativeInfinity, -Bound.PositiveInfinity)
    }

    @Test
    fun absFinite() {
        assertEquals(Bound.Finite(5), Bound.Finite(-5).abs())
        assertEquals(Bound.Finite(5), Bound.Finite(5).abs())
    }

    @Test
    fun absInfinity() {
        assertEquals(Bound.PositiveInfinity, Bound.PositiveInfinity.abs())
        assertEquals(Bound.PositiveInfinity, Bound.NegativeInfinity.abs())
    }

    @Test
    fun plusFinite() {
        assertEquals(
            Bound.Finite(8),
            Bound.Finite(3) + Bound.Finite(5)
        )
    }

    @Test
    fun plusOverflow() {
        assertEquals(
            Bound.PositiveInfinity,
            Bound.Finite(Long.MAX_VALUE) + Bound.Finite(1)
        )
    }

    @Test
    fun plusInfinity() {
        assertEquals(
            Bound.PositiveInfinity,
            Bound.PositiveInfinity + Bound.Finite(5)
        )

        assertEquals(
            Bound.NegativeInfinity,
            Bound.NegativeInfinity + Bound.Finite(5)
        )
    }

    @Test
    fun plusOppositeInfinity() {
        assertEquals(
            Bound.NaN,
            Bound.PositiveInfinity + Bound.NegativeInfinity
        )
    }

    @Test
    fun minusFinite() {
        assertEquals(
            Bound.Finite(2),
            Bound.Finite(5) - Bound.Finite(3)
        )
    }
}