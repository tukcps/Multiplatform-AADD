package io.github.tukcps.aadd.values.real.math

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


    internal fun twoProd(a: Double, b: Double): Rounded {
        val value = a * b

        if (!value.isFinite())
            return Rounded(value, 0.0)

        val sa = split(a)
        val sb = split(b)

        val error =
            ((sa.hi * sb.hi - value)
                    + sa.hi * sb.lo
                    + sa.lo * sb.hi) +
                    sa.lo * sb.lo

        return Rounded(value, error)
    }

    internal data class DoubleDouble(
        val hi: Double,
        val lo: Double
    )
}