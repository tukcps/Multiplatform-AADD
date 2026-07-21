package io.github.tukcps.aadd.values.integer

/**
 * Safe integer arithmetic for 64-bit signed integers.
 *
 * All operations are total and never throw exceptions. Results that cannot be
 * represented as a finite {@code Long} are mapped to the corresponding
 * {@link Bound}. Undefined operations return {@link Bound.NaN}.
 */
object IntegerMath {

    private fun finite(value: Long): Bound = Bound.Finite(value)

    /**
     * Returns the sum of [a] and [b].
     */
    fun add(a: Long, b: Long): Bound {
        val result = a + b

        return when {
            a > 0 && b > 0 && result < 0 -> Bound.PositiveInfinity
            a < 0 && b < 0 && result >= 0 -> Bound.NegativeInfinity
            else -> finite(result)
        }
    }

    /**
     * Returns the difference of [a] and [b].
     */
    fun sub(a: Long, b: Long): Bound {
        val result = a - b

        return when {
            a >= 0 && b < 0 && result < 0 -> Bound.PositiveInfinity
            a < 0 && b > 0 && result >= 0 -> Bound.NegativeInfinity
            else -> finite(result)
        }
    }
    /**
     * Returns the product of [a] and [b].
     *
     * Finite results are returned as [Bound.Finite].
     * Positive and negative overflows are mapped to the corresponding infinite bounds.
     */
    fun mul(a: Long, b: Long): Bound = when {
        a == 0L || b == 0L -> finite(0)
        a == 1L -> finite(b)
        b == 1L -> finite(a)
        a == -1L -> if (b == Long.MIN_VALUE) Bound.PositiveInfinity else finite(-b)
        b == -1L -> if (a == Long.MIN_VALUE) Bound.PositiveInfinity else finite(-a)
        else -> {
            val result = a * b
            if (result / b == a)
                finite(result)
            else if ((a > 0) == (b > 0))
                Bound.PositiveInfinity
            else
                Bound.NegativeInfinity
        }
    }

    /**
     * Returns the quotient of [a] divided by [b].
     */
    fun div(a: Long, b: Long): Bound =
        when {
            b == 0L -> Bound.NaN
            a == Long.MIN_VALUE && b == -1L -> Bound.PositiveInfinity
            else -> finite(a / b)
        }

    /**
     * Returns the remainder of [a] divided by [b].
     */
    fun rem(a: Long, b: Long): Bound =
        when {
            b == 0L -> Bound.NaN
            else -> finite(a % b)
        }

    /**
     * Returns the negation of [value].
     */
    fun neg(value: Long): Bound =
        if (value == Long.MIN_VALUE)
            Bound.PositiveInfinity
        else
            finite(-value)

    /**
     * Returns the absolute value of [value].
     */
    fun abs(value: Long): Bound =
        if (value == Long.MIN_VALUE)
            Bound.PositiveInfinity
        else
            finite(kotlin.math.abs(value))
    /**
     * Returns [value] incremented by one.
     */
    fun inc(value: Long): Bound = add(value, 1)

    /**
     * Returns [value] decremented by one.
     */
    fun dec(value: Long): Bound = sub(value, 1)
}