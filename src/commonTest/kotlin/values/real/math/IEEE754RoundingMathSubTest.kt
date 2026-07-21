package values.real.math

import io.github.tukcps.aadd.values.real.math.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.math.Rounding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IEEE754RoundingMathSubTest {

    private val math = IEEE754RoundingMath

    @Test
    fun exactSubtractionIsNotExpanded() {
        assertEquals(1.0, math.sub(3.0, 2.0, Rounding.DOWN))
        assertEquals(1.0, math.sub(3.0, 2.0, Rounding.NEAREST))
        assertEquals(1.0, math.sub(3.0, 2.0, Rounding.UP))
    }

    @Test
    fun inexactSubtractionContainsNearestValue() {
        val towardnegative = math.sub(0.3, 0.2, Rounding.DOWN)
        val nearestEVEN = math.sub(0.3, 0.2, Rounding.NEAREST)
        val towardpositive = math.sub(0.3, 0.2, Rounding.UP)

        assertTrue(towardnegative <= nearestEVEN)
        assertTrue(towardpositive >= nearestEVEN)
    }

    @Test
    fun subtractZeroIsExact() {
        assertEquals(42.0, math.sub(42.0, 0.0, Rounding.DOWN))
        assertEquals(42.0, math.sub(42.0, 0.0, Rounding.UP))
    }

    @Test
    fun subtractSelfIsExact() {
        assertEquals(0.0, math.sub(1.0, 1.0, Rounding.DOWN))
        assertEquals(0.0, math.sub(1.0, 1.0, Rounding.UP))
    }

    @Test
    fun zeroMinusZeroIsExact() {
        assertEquals(0.0, math.sub(0.0, 0.0, Rounding.DOWN))
        assertEquals(0.0, math.sub(0.0, 0.0, Rounding.UP))
    }

    @Test
    fun positiveInfinity() {
        assertEquals(
            Double.POSITIVE_INFINITY,
            math.sub(Double.POSITIVE_INFINITY, 1.0, Rounding.DOWN)
        )
        assertEquals(
            Double.POSITIVE_INFINITY,
            math.sub(Double.POSITIVE_INFINITY, 1.0, Rounding.UP)
        )
    }

    @Test
    fun negativeInfinity() {
        assertEquals(
            Double.NEGATIVE_INFINITY,
            math.sub(Double.NEGATIVE_INFINITY, 1.0, Rounding.DOWN)
        )
        assertEquals(
            Double.NEGATIVE_INFINITY,
            math.sub(Double.NEGATIVE_INFINITY, 1.0, Rounding.UP)
        )
    }

    @Test
    fun infinityMinusInfinityProducesNaN() {
        assertTrue(
            math.sub(
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Rounding.DOWN
            ).isNaN()
        )
    }

    @Test
    fun negativeInfinityMinusNegativeInfinityProducesNaN() {
        assertTrue(
            math.sub(
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Rounding.UP
            ).isNaN()
        )
    }

    @Test
    fun finiteMinusInfinity() {
        assertEquals(
            Double.NEGATIVE_INFINITY,
            math.sub(1.0, Double.POSITIVE_INFINITY, Rounding.DOWN)
        )
    }

    @Test
    fun finiteMinusNegativeInfinity() {
        assertEquals(
            Double.POSITIVE_INFINITY,
            math.sub(1.0, Double.NEGATIVE_INFINITY, Rounding.UP)
        )
    }
}