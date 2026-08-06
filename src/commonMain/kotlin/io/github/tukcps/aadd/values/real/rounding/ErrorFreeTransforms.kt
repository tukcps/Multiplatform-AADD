package io.github.tukcps.aadd.values.real.rounding

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble

/**
 * Error-free floating-point transformations according to
 * Knuth, Dekker and Shewchuk.
 *
 * Each function returns the rounded IEEE-754 result together with the exact
 * rounding error, so that
 *
 *     rounded + error == exact result.
 */
internal object ErrorFreeTransforms {

    /**
     * Knuth's TwoSum.
     *
     * Computes the rounded sum and the exact rounding error.
     */
    fun twoSum(a: Double, b: Double): Rounded {
        val sum = a + b
        if (!sum.isFinite()) return Rounded(sum, 0.0)
        val bp = sum - a
        val err = (a - (sum - bp)) + (b - bp)
        return Rounded(sum, err)
    }

    /**
     * Knuth's TwoSum.
     *
     * Computes the rounded sum and the exact rounding error.
     */
    fun twoSum(a: DoubleBound, b: DoubleBound): Rounded =
        twoSum(a.toDouble(), b.toDouble())

    /**
     * Computes the rounded sum and exact rounding error.
     *
     * Precondition: |a| >= |b|.
     */
    internal fun fastTwoSum(a: Double, b: Double): Rounded {
        val sum = a + b
        val err = b - (sum - a)
        return Rounded(sum, err)
    }

    private const val SPLITTER = 134217729.0 // 2^27 + 1
    internal fun split(a: Double): DoubleDouble {
        if (!a.isFinite()) return DoubleDouble(a, 0.0)
        val c = SPLITTER * a
        val hi = c - (c - a)
        val lo = a - hi
        return DoubleDouble(hi, lo)
    }

    internal fun twoProd(a: Double, b: Double): Rounded = twoProdImpl(a, b)

    internal data class DoubleDouble(
        val hi: Double,
        val lo: Double
    )
}

internal expect fun twoProdImpl(a: Double, b: Double): Rounded
