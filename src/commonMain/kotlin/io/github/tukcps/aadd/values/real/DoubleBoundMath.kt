package io.github.tukcps.aadd.values.real

import io.github.tukcps.aadd.values.real.DoubleBound.*
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.rounding.Rounding
import io.github.tukcps.aadd.values.real.rounding.RoundingMath

/**
 * Primitive arithmetic on [DoubleBound].
 * This object implements arithmetic on the extended floating-point
 * number line.
 *
 * IEEE-754 rounding is used explicitly through [RoundingMath] to guarantee
 * enclosure when used for interval arithmetic.
 *
 * NaN is not represented. Operations which do not have a unique bound
 * result return `null`.
 */
data object DoubleBoundMath {

    private val math: RoundingMath = IEEE754RoundingMath

    /**
     * Compares two bounds using the total ordering of the extended
     * floating-point number line.
     *
     * @param a first bound
     * @param b second bound
     * @return comparison result
     */
    fun compare(a: DoubleBound, b: DoubleBound): Int =
        when {
            a == b -> 0
            a === NegativeInfinity -> -1
            b === NegativeInfinity -> 1
            a === PositiveInfinity -> 1
            b === PositiveInfinity -> -1
            else -> (a as Finite).value.compareTo((b as Finite).value)
        }

    /**
     * Adds two bounds.
     *
     * Returns `null` for undefined cases on the extended number line,
     * such as +∞ + -∞.
     *
     * @param a left operand
     * @param b right operand
     * @param rounding rounding direction for finite values
     * @return resulting bound or null if undefined
     */
    fun add(a: DoubleBound, b: DoubleBound, rounding: Rounding): DoubleBound? =
        when {
            a === PositiveInfinity && b === NegativeInfinity -> null
            a === NegativeInfinity && b === PositiveInfinity -> null
            a === PositiveInfinity || b === PositiveInfinity -> PositiveInfinity
            a === NegativeInfinity || b === NegativeInfinity -> NegativeInfinity
            a is Finite && b is Finite -> Finite(math.add(a.value, b.value, rounding))
            else -> null
        }

    /**
     * Subtracts two bounds on the extended real number line.
     *
     * Undefined cases such as +∞ - +∞ are represented by `null`.
     *
     * @param a left operand
     * @param b right operand
     * @param rounding rounding direction for finite values
     * @return resulting bound or null if undefined
     */
    fun subtract(a: DoubleBound, b: DoubleBound, rounding: Rounding): DoubleBound? =
        add(a, negate(b), rounding)

    /**
     * Negates a bound on the extended real number line.
     *
     * @param value bound to negate
     * @return negated bound
     */
    fun negate(value: DoubleBound): DoubleBound = when (value) {
            PositiveInfinity -> NegativeInfinity
            NegativeInfinity -> PositiveInfinity
            is Finite -> Finite(-value.value)
        }

    /**
     * Multiplies two bounds.
     */
    fun multiply(
        a: DoubleBound,
        b: DoubleBound,
        rounding: Rounding
    ): DoubleBound? = when {
            (a.isZero && b.isInfinite) || (b.isZero && a.isInfinite) -> null
            a.isInfinite || b.isInfinite    -> if (sameSign(a, b)) PositiveInfinity else NegativeInfinity
            else                            -> Finite(math.mul((a as Finite).value, (b as Finite).value, rounding))
        }

    /**
     * Divides two bounds.
     */
    fun divide(a: DoubleBound, b: DoubleBound, rounding: Rounding): DoubleBound? = when {
            a.isZero && b.isZero -> null
            b.isZero ->
                if (sameSign(a, b)) PositiveInfinity
                else NegativeInfinity
            a.isInfinite && b.isInfinite -> null
            a.isInfinite ->
                if (sameSign(a, b)) PositiveInfinity
                else NegativeInfinity
            b.isInfinite -> Finite(0.0)
            else ->
                Finite(math.div((a as Finite).value, (b as Finite).value, rounding))
        }

    private fun signBit(value: Double): Boolean =
        (value.toRawBits() and Long.MIN_VALUE) != 0L

    private fun sameSign(a: DoubleBound, b: DoubleBound): Boolean =
        signBit(a.toDouble()) == signBit(b.toDouble())

    fun abs(value: DoubleBound?): DoubleBound? = when {
        value == null -> null
        value === PositiveInfinity -> NegativeInfinity
        value === NegativeInfinity -> PositiveInfinity
        value.isInfinite && value.finiteValue < 0.0 -> -value
        else -> value
    }

    fun min(vararg values: DoubleBound): DoubleBound {
        require(values.isNotEmpty())

        var result = values[0]
        for (i in 1 until values.size)
            if (compare(values[i], result) < 0)
                result = values[i]

        return result
    }

    fun max(vararg values: DoubleBound): DoubleBound {
        require(values.isNotEmpty())

        var result = values[0]

        for (i in 1 until values.size)
            if (compare(values[i], result) > 0)
                result = values[i]

        return result
    }

    fun sqrt(value: DoubleBound, rounding: Rounding): DoubleBound =
        when (value) {
            NegativeInfinity -> NegativeInfinity
            PositiveInfinity -> PositiveInfinity
            is Finite -> Finite(
                math.sqrt(value.value, rounding)
            )
        }

    fun exp(value: DoubleBound, rounding: Rounding): DoubleBound? =
        when (value) {
            NegativeInfinity -> Finite(0.0)
            PositiveInfinity -> PositiveInfinity
            is Finite -> math.exp(value.value, rounding).toDoubleBound()
        }

    fun ln(value: DoubleBound, rounding: Rounding): DoubleBound =
        when (value) {
            NegativeInfinity -> NegativeInfinity
            PositiveInfinity -> PositiveInfinity
            is Finite -> Finite(math.ln(value.value, rounding))
        }

    fun pow(value: DoubleBound, exponent: Double, rounding: Rounding): DoubleBound? =
        when (value) {
            NegativeInfinity -> NegativeInfinity
            PositiveInfinity -> PositiveInfinity
            is Finite -> math.pow(value.value, exponent, rounding).toDoubleBound()
        }

    /**
     * Converts a nullable [DoubleBound] into an IEEE-754 Double.
     *
     * A missing bound has no valid representation and is mapped to NaN.
     *
     * @return IEEE-754 representation or NaN for null
     */
    fun DoubleBound?.toDouble(): Double =
        when (this) {
            null -> Double.NaN
            PositiveInfinity -> Double.POSITIVE_INFINITY
            NegativeInfinity -> Double.NEGATIVE_INFINITY
            is Finite -> value
        }
}