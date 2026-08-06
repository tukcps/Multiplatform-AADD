package io.github.tukcps.aadd.values.integer

import io.github.tukcps.aadd.values.integer.LongBound.*
import kotlin.math.pow

/**
 * ## Arithmetic on integer bounds.
 *
 * This object implements the arithmetic of the extended integer number line,
 * consisting of finite integer values together with positive infinity and
 * negative infinity, and an undefined value represented by zero.
 * Representation by zero allows and enforces handling at the level of ranges.
 *
 * All arithmetic on [LongBound] is centralized here. The operator overloads
 * defined in `LongBoundOperators.kt` delegate to these functions.
 *
 * The implementation is platform-independent and suitable for Kotlin
 * Multiplatform projects.
 *
 * Finite arithmetic detects overflow and conservatively returns
 * [PositiveInfinity] or [NegativeInfinity] instead of wrapping around.
 * Undefined operations, such as `+∞ + -∞`, evaluate to null.
 * They must be handled at the level of ranges.
 *
 * Comparison follows the total ordering
 *
 *     -∞ < finite values < +∞
 *
 * This ordering is used throughout the interval arithmetic library to compare
 * and sort bounds.
 *
 * @author Christoph Grimm
 */
data object LongMath {

    /**
     * Compares two integer bounds according to their total ordering.
     *
     * The ordering is
     *
     *     -∞ < finite values < +∞
     *
     * @param a the first bound
     * @param b the second bound
     * @return a negative value if `a < b`, zero if `a == b`,
     *   or a positive value if `a > b`
     */
    fun compare(a: LongBound, b: LongBound): Int =
        when {
            a === b -> 0
            a === NegativeInfinity -> -1
            b === NegativeInfinity -> 1
            a === PositiveInfinity -> 1
            b === PositiveInfinity -> -1
            else -> (a as Finite).value.compareTo((b as Finite).value)
        }

    /**
     * Returns the finite value of a bound.
     *
     * The caller must ensure that the bound is finite.
     *
     * @receiver the finite bound
     * @return the stored integer value
     */
    private fun LongBound.value(): Long =
        (this as Finite).value

    /**
     * Returns whether two bounds have the same sign.
     *
     * @param a the first bound
     * @param b the second bound
     * @return `true` if both bounds are positive or both are negative
     */
    private fun sameSign(a: LongBound, b: LongBound): Boolean =
        (a.isPositive() && b.isPositive()) ||
                (a.isNegative() && b.isNegative())

    fun LongBound.isPositive(): Boolean = when (this) {
        PositiveInfinity -> true
        NegativeInfinity -> false
        is Finite -> value > 0
    }

    fun LongBound.isNegative(): Boolean = when (this) {
        NegativeInfinity -> true
        PositiveInfinity -> false
        is Finite -> value < 0
    }

    /**
     * Returns the arithmetic negation of a bound.
     * @param value the operand
     * @return the negated bound
     */
    fun negate(value: LongBound): LongBound =
        when (value) {
            PositiveInfinity -> NegativeInfinity
            NegativeInfinity -> PositiveInfinity
            is Finite ->        {
                if (value.value == Long.MIN_VALUE) return PositiveInfinity
                else {
                    val m = -value.value
                    Finite(-value.value)
                }
            }
        }

    /**
     * Returns the absolute value of a bound.
     *
     * @param value the operand
     * @return the absolute value
     */
    fun abs(value: LongBound): LongBound =
        when (value) {
            PositiveInfinity, NegativeInfinity -> PositiveInfinity
            is Finite ->
                if (value.value == Long.MIN_VALUE) PositiveInfinity
                else Finite(kotlin.math.abs(value.value))
        }

    /**
     * Adds two integer bounds.
     * If the result cannot be represented by a single bound
     * (for example `+∞ + -∞`), `null` is returned.
     * @param a left operand
     * @param b right operand
     * @return sum or `null` if no unique bound exists
     */
    fun add(a: LongBound, b: LongBound): LongBound? =
        when {
            a === PositiveInfinity && b === NegativeInfinity -> null
            a === NegativeInfinity && b === PositiveInfinity -> null
            a === PositiveInfinity || b === PositiveInfinity -> PositiveInfinity
            a === NegativeInfinity || b === NegativeInfinity -> NegativeInfinity
            else -> {
                val x = a.finiteValue
                val y = b.finiteValue
                val r = x + y
                if (((x xor r) and (y xor r)) < 0) {
                    return if (x >= 0) PositiveInfinity else NegativeInfinity
                }
                return Finite(r)
            }
        }

    /**
     * Subtracts one bound from another.
     * @param a minuend
     * @param b subtrahend
     * @return difference or `null` if no unique bound exists
     */
    fun subtract(a: LongBound, b: LongBound): LongBound? {
        if (b.isFinite && b.value() == Long.MIN_VALUE) {
            val inRange = add(b, Finite(1))!!
            return add(add(a, negate(inRange))!!, Finite(1))!!
        } else
            return add(a, negate(b))
    }

    /**
     * Multiplies two integer bounds.
     * Returns `null` for indeterminate cases such as `0 * ∞`.
     * @param a left operand
     * @param b right operand
     * @return product or `null` if no unique bound exists
     */
    fun multiply(a: LongBound, b: LongBound): LongBound? = when {
            (a.isZero && b.isInfinite) || (b.isZero && a.isInfinite) -> null
            a.isInfinite || b.isInfinite ->
                if (sameSign(a, b)) PositiveInfinity
                else NegativeInfinity

            else -> {
                val x = a.finiteValue
                val y = b.finiteValue

                if (x == 0L || y == 0L)
                    Finite(0)
                else if (x == Long.MIN_VALUE && y == -1L ||
                    y == Long.MIN_VALUE && x == -1L)
                    PositiveInfinity
                else {
                    val r = x * y

                    when {
                        r / x == y -> Finite(r)
                        (x > 0) == (y > 0) -> PositiveInfinity
                        else -> NegativeInfinity
                    }
                }
            }
        }

    /**
     * Divides two integer bounds.
     *
     * Undefined bound combinations such as `0/0` or `∞/∞`
     * return `null`.
     *
     * @param a dividend
     * @param b divisor
     * @return quotient or `null` if no unique bound exists
     */
    fun divide(a: LongBound, b: LongBound): LongBound? =
        when {
            a.isZero && b.isZero -> null
            b.isZero -> null
            a.isInfinite && b.isInfinite -> null
            a.isInfinite -> if (sameSign(a, b))
                    PositiveInfinity
                else
                    NegativeInfinity
            b.isInfinite -> Finite(0)
            else -> {
                val x = a.finiteValue
                val y = b.finiteValue
                if (x == Long.MIN_VALUE && y == -1L) PositiveInfinity
                else Finite(x / y)
            }
        }

    fun min(vararg values: LongBound): LongBound {
        require(values.isNotEmpty())
        var result = values[0]
        for (i in 1 until values.size)
            if (compare(values[i], result) < 0)
                result = values[i]

        return result
    }

    fun max(vararg values: LongBound): LongBound {
        require(values.isNotEmpty())
        var result = values[0]
        for (i in 1 until values.size)
            if (compare(values[i], result) > 0)
                result = values[i]
        return result
    }

    /**
     * Returns the exponential function of a bound.
     *
     * Infinite and undefined operands follow the semantics of the extended
     * integer number line. Finite results are conservatively rounded upwards.
     *
     * @param value the operand
     * @return `e` raised to the power of `value`
     */
    fun exp(value: LongBound): LongBound =
        when (value) {
            NegativeInfinity -> Finite(0)
            PositiveInfinity -> PositiveInfinity
            is Finite -> {
                val result = kotlin.math.exp(value.value.toDouble())
                if (result > Long.MAX_VALUE) PositiveInfinity
                else Finite(result.toLong())
            }
        }

    /**
     * Calculates the natural logarithm of a bound.
     *
     * The logarithm is only defined for positive values.
     *
     * The value zero is mapped to negative infinity according
     * to the mathematical limit:
     *
     *     lim(x→0+) log(x) = -∞
     *
     * @param value input bound
     * @return logarithm result or `null` if undefined
     */
    fun ln(value: LongBound): LongBound? =
        when (value) {
            NegativeInfinity -> null
            PositiveInfinity -> PositiveInfinity
            is Finite -> when {
                    value.value < 0L -> null
                    value.value == 0L -> NegativeInfinity
                    else -> Finite(kotlin.math.ln(value.value.toDouble()).toLong())
            }
        }

    /**
     * Calculates the square root of a bound.
     * The square root is defined only for non-negative values.
     * @param value input bound
     * @return square root result or `null` if undefined
     */
    fun sqrt(value: LongBound): LongBound? =
        when(value) {
            NegativeInfinity -> null
            PositiveInfinity -> PositiveInfinity
            is Finite        -> if (value.value < 0L) null
                                else Finite(kotlin.math.sqrt(value.value.toDouble()).toLong())
        }

    /**
     * Raises a bound to an integer power.
     *
     * Only non-negative integer exponents are supported.
     * Results outside the Long domain are represented by infinity.
     *
     * Undefined or non-representable cases return `null`.
     *
     * @param base base bound
     * @param exponent non-negative integer exponent
     * @return powered bound or `null`
     */
    fun pow(base: LongBound, exponent: Long): LongBound? {
        if (exponent < 0) return null
        if (exponent == 0L) return when (base) {
                is Finite -> Finite(1)
                PositiveInfinity,
                NegativeInfinity -> Finite(1)
            }

        return when (base) {
            PositiveInfinity -> PositiveInfinity
            NegativeInfinity ->
                if (exponent % 2L == 0L)
                    PositiveInfinity
                else
                    NegativeInfinity

            is Finite -> {
                val value = base.value

                if (value == 0L)
                    Finite(0)
                else {
                    val negative = value < 0L && exponent % 2L != 0L

                    val result = kotlin.math.abs(value.toDouble()).pow(exponent.toDouble())

                    if (result > Long.MAX_VALUE)
                        if (negative)
                            NegativeInfinity
                        else
                            PositiveInfinity
                    else {
                        val r = result.toLong()

                        if (negative) Finite(-r)
                        else Finite(r)
                    }
                }
            }
        }
    }
}