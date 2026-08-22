package io.github.tukcps.aadd.util

import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import kotlin.jvm.JvmName

/**
 * Assertions to support testing of applications that use AADD library.
 */
object Assertions {

    /**
     * Compare two DoubleBound values with tolerance.
     */
    fun assertEquals(
        expected: DoubleBound?,
        actual: DoubleBound?,
        tolerance: Double = 0.0
    ) {
        when (expected) {
            null if actual != null ->
                throw AssertionError("expected: $expected, actual: $actual")

            is DoubleBound.Finite if actual is DoubleBound.Finite ->
                if (actual.value !in expected.value-tolerance .. expected.value+tolerance)
                    throw AssertionError("expected: $expected, actual: $actual")

            else -> if (expected != actual)
                throw AssertionError("expected: $expected, actual: $actual")
        }
    }

    /**
     * Compare a Double with a DoubleBound values with tolerance.
     */
    fun assertEquals(
        expected: Double,
        actual: DoubleBound?,
        tolerance: Double = 0.0
    ) {
        when  {
            expected.isNaN() && actual != null ->
                throw AssertionError("expected: $expected, actual: $actual")

            expected.isFinite() && actual is DoubleBound.Finite ->
                if (actual.value !in expected-tolerance .. expected+tolerance)
                    throw AssertionError("expected: $expected, actual: $actual")

            expected == Double.NEGATIVE_INFINITY && actual != DoubleBound.NegativeInfinity ->
                throw AssertionError("expected: $expected, actual: $actual")

            expected == Double.POSITIVE_INFINITY && actual != DoubleBound.PositiveInfinity ->
                throw AssertionError("expected: $expected, actual: $actual")

            else ->
                throw AssertionError("expected: $expected, actual: $actual")
        }
    }

    /**
     * Compare a Double with a DoubleBound values with tolerance.
     */
    fun assertEquals(
        expected: Long,
        actual: LongBound?,
    ) {
        if (actual is LongBound.Finite && actual.value == expected) return
        else throw AssertionError("expected: $expected, actual: $actual")
    }

    @Deprecated("Replace with assertSafeInclusion", replaceWith = ReplaceWith("assertSafeInclusion(expected, actual, tolerance)"))
    @JvmName("assertEqualsDoubleBoundRangeWithTolerance")
    fun assertEquals(
        expected: ClosedRange<DoubleBound>,
        actual: ClosedRange<DoubleBound>,
        tolerance: Double = 0.0
    ) = assertSafeInclusion(expected, actual, tolerance)

    fun assertSafeInclusion(
        expected: ClosedRange<DoubleBound>,
        actual: ClosedRange<DoubleBound>,
        tolerance: Double = 0.0
    ) = assertSafeInclusion(expected.start.toDouble().. expected.endInclusive.toDouble(), actual, tolerance)


    @Deprecated("Replace with assertSafeInclusion", replaceWith = ReplaceWith("assertSafeInclusion(expected, actual, tolerance)"))
    fun assertEquals(
        expected: ClosedRange<Double>,
        actual: ClosedRange<DoubleBound>,
        tolerance: Double = 0.0
    ) = assertSafeInclusion(expected, actual, tolerance)


    @JvmName("assertEqualDoubleBoundRangeWithTolerance")
    fun assertSafeInclusion(
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
        if(expected.start != actual.start) throw AssertionError("Expected: $expected, Actual: $actual")
        if(expected.endInclusive != actual.endInclusive) throw AssertionError("Expected: $expected, Actual: $actual")
    }

    @JvmName("assertEqualsLongRange2")
    fun assertEquals(
        expected: ClosedRange<Long>,
        actual: ClosedRange<LongBound>
    ) {
        if (expected.start != actual.start.finiteValue) throw AssertionError("Expected: $expected, Actual: $actual")
        if (expected.endInclusive != actual.endInclusive.finiteValue) throw AssertionError("Expected: $expected, Actual: $actual")
    }
}