package values.real.math

import io.github.tukcps.aadd.values.real.math.ErrorFreeTransforms
import io.github.tukcps.aadd.values.real.math.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.math.Rounding
import kotlin.math.nextDown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IEEE754RoundingMathAddTest {

    private val math = IEEE754RoundingMath

    @Test
    fun exactAdditionIsNotExpanded() {
        assertEquals(3.0, math.add(1.0, 2.0, Rounding.DOWN))
        assertEquals(3.0, math.add(1.0, 2.0, Rounding.NEAREST))
        assertEquals(3.0, math.add(1.0, 2.0, Rounding.UP))
    }

    @Test
    fun inexactAdditionContainsExactValue() {
        val towardnegative = math.add(0.1, 0.2, Rounding.DOWN)
        val nearestEVEN = math.add(0.1, 0.2, Rounding.NEAREST)
        val towardpositive = math.add(0.1, 0.2, Rounding.UP)

        assertTrue(towardnegative <= nearestEVEN)
        assertTrue(towardpositive >= nearestEVEN)
    }

    @Test
    fun addZeroIsExact() {
        assertEquals(42.0, math.add(42.0, 0.0, Rounding.DOWN))
        assertEquals(42.0, math.add(42.0, 0.0, Rounding.UP))
    }

    @Test
    fun cancellationIsExact() {
        assertEquals(0.0, math.add(1.0, -1.0, Rounding.DOWN))
        assertEquals(0.0, math.add(1.0, -1.0, Rounding.UP))
    }

    @Test
    fun nextRepresentableValues() {
        val x = 1.0.nextDown()

        assertEquals(1.0, math.add(x, 1.0 - x, Rounding.DOWN))
        assertEquals(1.0, math.add(x, 1.0 - x, Rounding.UP))
    }

    @Test
    fun positiveInfinity() {
        assertEquals(
            Double.POSITIVE_INFINITY,
            math.add(Double.POSITIVE_INFINITY, 1.0, Rounding.DOWN)
        )
        assertEquals(
            Double.POSITIVE_INFINITY,
            math.add(Double.POSITIVE_INFINITY, 1.0, Rounding.UP)
        )
    }

    @Test
    fun negativeInfinity() {
        assertEquals(
            Double.NEGATIVE_INFINITY,
            math.add(Double.NEGATIVE_INFINITY, -1.0, Rounding.DOWN)
        )
        assertEquals(
            Double.NEGATIVE_INFINITY,
            math.add(Double.NEGATIVE_INFINITY, -1.0, Rounding.UP)
        )
    }

    @Test
    fun infinityPlusInfinity() {
        assertEquals(
            Double.POSITIVE_INFINITY,
            math.add(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Rounding.UP)
        )
    }

    @Test
    fun oppositeInfinitiesProduceNaN() {
        assertTrue(
            math.add(
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Rounding.DOWN
            ).isNaN()
        )
    }

    @Test
    fun nanPropagates() {
        assertTrue(math.add(Double.NaN, 1.0, Rounding.DOWN).isNaN())
        assertTrue(math.add(1.0, Double.NaN, Rounding.UP).isNaN())
    }

    @Test
    fun overflowProducesInfinity() {
        assertEquals(
            Double.POSITIVE_INFINITY,
            math.add(Double.MAX_VALUE, Double.MAX_VALUE, Rounding.UP)
        )
    }

    @Test
    fun underflowRemainsFinite() {
        val x = math.add(Double.MIN_VALUE, Double.MIN_VALUE, Rounding.NEAREST)
        assertTrue(x > 0.0)
        assertTrue(x.isFinite())
    }

    @Test
    fun exactErrorIsZero() {
        val r = ErrorFreeTransforms.twoSum(1.0, 2.0)
        assertEquals(3.0, r.value)
        assertEquals(0.0, r.error)
    }

    @Test
    fun inexactErrorIsNonZero() {
        val r = ErrorFreeTransforms.twoSum(0.1, 0.2)
        assertTrue(r.error != 0.0)
    }
}