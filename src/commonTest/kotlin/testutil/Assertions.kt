package testutil

import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import kotlin.jvm.JvmName
import kotlin.test.assertEquals

object Assertions {
    /**
     * Compare two DoubleBound values with tolerance.
     */
    fun assertEquals(
        expected: DoubleBound?,
        actual: DoubleBound?,
        tolerance: Double = 0.0
    ) {
        when {
            expected is DoubleBound.Finite && actual is DoubleBound.Finite ->
                assertEquals(expected.value, actual.value, tolerance)

            else -> kotlin.test.assertEquals(expected, actual)
        }
    }

    fun assertEquals(
        expected: ClosedRange<DoubleBound>,
        actual: ClosedRange<DoubleBound>
    ) {
        assertEquals(expected.start, actual.start, "start")
        assertEquals(expected.endInclusive, actual.endInclusive, "endInclusive")
    }

    @JvmName("assertEqualsDoubleBoundRangeWithTolerance")
    fun assertEquals(
        expected: ClosedRange<DoubleBound>,
        actual: ClosedRange<DoubleBound>,
        tolerance: Double = 0.0
    ) {
        assertEquals(expected.start, actual.start, tolerance)
        assertEquals(expected.endInclusive, actual.endInclusive, tolerance)
    }

    fun assertEquals(
        expected: ClosedRange<Double>,
        actual: ClosedRange<DoubleBound>,
        tolerance: Double = 0.0
    ) {
        val expectedMin = expected.start
        val expectedMax = expected.endInclusive

        val actualMin = actual.start.toDouble()
        val actualMax = actual.endInclusive.toDouble()

        when {
            actualMin > expectedMin ->
                throw AssertionError("Lower bound is too high: expected ≤ $expectedMin, actual = $actualMin")
            expectedMin - actualMin > tolerance ->
                throw AssertionError("Lower bound over-approximation too large: expected = $expectedMin, actual = $actualMin, error = ${expectedMin - actualMin}, tolerance = $tolerance")
            actualMax < expectedMax -> throw AssertionError("Upper bound is too low: expected ≥ $expectedMax, actual = $actualMax")
            actualMax - expectedMax > tolerance ->
                throw AssertionError("Upper bound over-approximation too large: expected = $expectedMax, actual = $actualMax, error = ${actualMax - expectedMax}, tolerance = $tolerance")
        }
    }

    @JvmName("assertEqualsLongRange")
    fun assertEquals(
        expected: ClosedRange<LongBound>,
        actual: ClosedRange<LongBound>
    ) {
        assertEquals(expected.start, actual.start)
        assertEquals(expected.endInclusive, actual.endInclusive)
    }

    @JvmName("assertEqualsLongRange2")
    fun assertEquals(
        expected: ClosedRange<Long>,
        actual: ClosedRange<LongBound>
    ) {
        assertEquals(expected.start, actual.start.finiteValue)
        assertEquals(expected.endInclusive, actual.endInclusive.finiteValue)
    }
}