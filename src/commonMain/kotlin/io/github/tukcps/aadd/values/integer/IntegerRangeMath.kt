package io.github.tukcps.aadd.values.integer

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * ## Mathematical functions on [IntegerRange]s.
 *
 * Every function is interpreted as the image of a convex subset of the
 * integer numbers under the corresponding mathematical function.
 *
 * Since an [IntegerRange] can represent only convex subsets of ℤ, the
 * result is always the smallest convex integer subset containing all
 * possible results.
 *
 * ### Constraint propagation
 *
 * Mathematical functions follow *constraint propagation semantics*.
 * Undefined operands do **not** automatically yield [IntegerRange.Empty].
 * Instead, every input value for which the operation is defined contributes
 * to the result.
 *
 * Consequently, [IntegerRange.Empty] is returned **only if no valid result
 * exists**.
 *
 * Examples:
 *
 * * `sqrt([-1,4]) = [0,2]`
 * * `sqrt([-4,-1]) = Empty`
 * * `log([-1,10]) = [0,ln(10)]`
 * * `1 / [0,0] = Empty`
 *
 * Thus, every operation computes the smallest convex integer subset
 * containing all valid results.
 *
 * `abs` returns the absolute value.
 */


/**
 * Returns the sum of two integer ranges.
 *
 * The operation follows set semantics:
 *
 *     A + B = { a + b | a ∈ A, b ∈ B }
 *
 * Undefined bound combinations are conservatively enclosed.
 *
 * @param a 1st summand
 * @param b 2nd summand
 * @return enclosing sum range
 */
fun add(a: IntegerRange, b: IntegerRange): IntegerRange {
    if (a.isEmpty() || b.isEmpty()) return IntegerRange.Empty
    val lower = a.min + b.min
    val upper = a.max + b.max
    return IntegerRange(lower ?: LongBound.NegativeInfinity, upper ?: LongBound.PositiveInfinity)
}

fun add(a: IntegerRange, b: Long): IntegerRange {
    if (a.isEmpty()) return IntegerRange.Empty
    val lower = a.min + LongBound.Finite(b)
    val upper = a.max + LongBound.Finite(b)
    return IntegerRange(lower ?: LongBound.NegativeInfinity, upper ?: LongBound.PositiveInfinity)
}

/**
 * Returns the difference of two integer ranges.
 * The operation follows set semantics:
 *
 *     A - B = { a - b | a ∈ A, b ∈ B }
 *
 * Undefined combinations are conservatively enclosed.
 * @param a range
 * @return enclosing difference range
 */
fun subtract(a: IntegerRange, b: IntegerRange): IntegerRange {
    if (a.isEmpty() || b.isEmpty()) return IntegerRange.Empty
    val lower = a.min - b.max
    val upper = a.max - b.min
    return IntegerRange(lower ?: LongBound.NegativeInfinity, upper ?: LongBound.PositiveInfinity)
}

fun subtract(a: IntegerRange, b: Long): IntegerRange {
    if (a.isEmpty()) return IntegerRange.Empty
    val lower = a.min - LongBound.Finite(b)
    val upper = a.max - LongBound.Finite(b)
    return IntegerRange(lower ?: LongBound.NegativeInfinity, upper ?: LongBound.PositiveInfinity)
}

/**
 * Returns the product of two integer ranges.
 *
 * The operation follows set semantics:
 *
 *     A * B = { a * b | a ∈ A, b ∈ B }
 *
 * The result is the smallest convex integer range containing
 * all possible products.
 *
 * @param a 1st range
 * @param b 2nd range
 * @return enclosing product range
 */
fun multiply(a: IntegerRange, b: IntegerRange): IntegerRange {
    if (a.isEmpty() || b.isEmpty()) return IntegerRange.Empty

    // Exact zero absorbs all products incl. infinities.
    if (a.isZero() || b.isZero()) return IntegerRange.Zero

    val products = listOf(
        LongMath.multiply(a.min, b.min),
        LongMath.multiply(a.min, b.max),
        LongMath.multiply(a.max, b.min),
        LongMath.multiply(a.max, b.max)
    )

    val defined = products.filterNotNull()

    return if (defined.isEmpty()) {
        IntegerRange.All
    } else {
        IntegerRange(
            LongMath.min(*defined.toTypedArray()),
            LongMath.max(*defined.toTypedArray())
        )
    }
}


/**
 * Returns the quotient of two integer ranges.
 *
 * Division follows set semantics:
 *
 *     A / B = { a / b | a ∈ A, b ∈ B, b != 0 }
 *
 * Divisor intervals containing zero are split into their valid
 * positive and negative parts.
 *
 * @param a dividend range
 * @param b divisor range
 * @return enclosing quotient range
 */
fun divide(a: IntegerRange, b: IntegerRange): IntegerRange {

    /**
     * Splits a range at zero.
     *
     * Returns the non-zero parts of this range.
     */
    fun IntegerRange.splitAtZero(): List<IntegerRange> =
        when {
            isEmpty() ->
                emptyList()

            max < LongBound.Finite(0) ||
                    min > LongBound.Finite(0) ->
                listOf(this)

            isZero() ->
                emptyList()

            else -> {
                val result = mutableListOf<IntegerRange>()

                if (min < LongBound.Finite(0))
                    result += IntegerRange(
                        min,
                        LongBound.Finite(-1)
                    )

                if (max > LongBound.Finite(0))
                    result += IntegerRange(
                        LongBound.Finite(1),
                        max
                    )

                result
            }
        }

    if (a.isEmpty() || b.isEmpty()) return IntegerRange.Empty

    val divisors = b.splitAtZero()

    if (divisors.isEmpty()) return IntegerRange.Empty

    var result = IntegerRange.Empty

    for (divisor in divisors) {
        val quotients = listOf(
            LongMath.divide(a.min, divisor.min),
            LongMath.divide(a.min, divisor.max),
            LongMath.divide(a.max, divisor.min),
            LongMath.divide(a.max, divisor.max)
        )

        val defined = quotients.filterNotNull()

        if (defined.isNotEmpty()) {
            val part = IntegerRange(
                LongMath.min(*defined.toTypedArray()),
                LongMath.max(*defined.toTypedArray())
            )

            result =
                if (result.isEmpty())
                    part
                else
                    result join part
        }
    }
    return result
}


/**
 * Returns the absolute value of an IntegerRange:
 *
 *     abs(S) = { |x| | x ∈ S }
 *
 * The result overapproximates the smallest convex integer set containing
 * all possible absolute values.
 * @param value input range
 * @return range containing all absolute values
 */
fun abs(value: IntegerRange): IntegerRange =
    when {
        value.isEmpty() ->
            IntegerRange.Empty

        value.max <= LongBound.Finite(0) ->
            IntegerRange(
                absBound(value.max),
                absBound(value.min)
            )

        value.min >= LongBound.Finite(0) ->
            value

        else ->
            IntegerRange(
                LongBound.Finite(0),
                maxOf(
                    absBound(value.min),
                    absBound(value.max)
                )
            )
    }

private fun absBound(value: LongBound): LongBound =
    LongMath.abs(value)

/**
 * Calculates the square of this integer range.
 * The operation follows set semantics:
 *
 *     sqr(S) = { x² | x ∈ S }
 *
 * @return enclosing range of all squared values
 */
fun sqr(x: IntegerRange): IntegerRange =
    when {
        x.isEmpty() -> IntegerRange.Empty
        x.min >= LongBound.Finite(0) -> x * x
        x.max <= LongBound.Finite(0) -> x * x
        else -> IntegerRange(
                    LongBound.Finite(0),
                    LongMath.max(LongMath.multiply(x.min, x.min)
                            ?: LongBound.PositiveInfinity,
                        LongMath.multiply(x.max, x.max)
                            ?: LongBound.PositiveInfinity
                    )
                )
    }

/**
 * Calculates the square root of this integer range.
 *
 * The square root is only defined for non-negative values.
 * Negative parts of the input range are removed.
 *
 * The operation follows set semantics:
 *
 *     sqrt(S) = { sqrt(x) | x ∈ S and x >= 0 }
 *
 * @param value the value from which sqrt is calculated
 * @return enclosing range of all square roots
 */
fun sqrt(value: IntegerRange): IntegerRange =
    when {
        value.isEmpty() -> IntegerRange.Empty
        value.max < LongBound.Finite(0) -> IntegerRange.Empty
        else -> {
            val lower =
                if (value.min < LongBound.Finite(0)) 0L
                else ceil(kotlin.math.sqrt(value.min.finiteValue.toDouble())).toLong()

            val upper =
                when (value.max) {
                    LongBound.PositiveInfinity -> LongBound.PositiveInfinity
                    is LongBound.Finite -> LongBound.Finite(
                            ceil(kotlin.math.sqrt(value.max.value.toDouble())).toLong()
                    )
                    LongBound.NegativeInfinity -> LongBound.Finite(0)
                }

            IntegerRange(LongBound.Finite(lower), upper)
        }
    }

/**
 * Calculates the n-th root of this integer set.
 *
 * The operation follows constraint propagation semantics:
 *
 *     root(S,n) = { x^(1/n) | x ∈ S and x^(1/n) is defined }
 *
 * Invalid input values are discarded. [IntegerRange.Empty] is returned only
 * if no valid result exists.
 *
 * The result is the smallest convex integer subset containing all valid
 * roots.
 *
 * @param n integer root degree
 * @return enclosing integer range of all valid roots
 */
fun root(value: IntegerRange, n: Long): IntegerRange {
    if (value.isEmpty() || n == 0L) return IntegerRange.Empty
    if (n == 1L) return value

    val degree = kotlin.math.abs(n)

    // Remove invalid values for even roots.
    val input = if (degree % 2L == 0L) {
        when {
            value.max < LongBound.Finite(0) -> return IntegerRange.Empty
            value.min < LongBound.Finite(0) -> IntegerRange(LongBound.Finite(0), value.max)
            else -> value
        }
    } else { value }

    fun nthRoot(value: LongBound): Double =
        when (value) {
            LongBound.NegativeInfinity ->
                if (degree % 2L == 1L)
                    Double.NEGATIVE_INFINITY
                else
                    0.0

            LongBound.PositiveInfinity ->
                Double.POSITIVE_INFINITY

            is LongBound.Finite ->
                if (value.value < 0) {
                    -(-value.value.toDouble())
                        .pow(1.0 / degree.toDouble())
                } else {
                    value.value.toDouble()
                        .pow(1.0 / degree.toDouble())
                }
        }

    val lower = nthRoot(input.min)
    val upper = nthRoot(input.max)

    val result = IntegerRange(
        LongBound.Finite(floor(lower).toLong()),
        LongBound.Finite(ceil(upper).toLong())
    )

    return if (n < 0)
        IntegerRange.One / result
    else
        result
}

/**
 * Calculates roots for a range of possible root degrees.
 *
 * The operation follows constraint propagation semantics:
 *
 *     root(S,N) = { x^(1/n) | x ∈ S, n ∈ N and defined }
 *
 * Every valid degree contributes to the result. The returned range is the
 * smallest convex integer set containing all possible roots.
 *
 * @param value value x
 * @param degree possible root degrees n
 * @return enclosing integer range of all valid roots
 */
fun root(value: IntegerRange, degree: IntegerRange): IntegerRange {
    if (value.isEmpty() || degree.isEmpty()) return IntegerRange.Empty
    if (!degree.isFinite()) return IntegerRange.All
    if (degree.isScalar()) return root(value, degree.min.finiteValue)

    val minDegree = degree.min.finiteValue
    val maxDegree = degree.max.finiteValue

    if (minDegree <= 0L && maxDegree >= 0L)
        return value.rootRangeExcludingZero(minDegree, maxDegree)

    /*
     * Exact enumeration for small degree ranges.
     */
    val count = maxDegree - minDegree + 1

    if (count <= 32) {
        var result = IntegerRange.Empty

        for (degree in minDegree..maxDegree) {
            val current = root(value, degree)

            result = if (result.isEmpty())
                current
            else
                result join current
        }
        return result
    }

    /*
     * Large finite ranges:
     * use the extremal degrees.
     */
    val low = root(value, minDegree)
    val high = root(value, maxDegree)

    return low join high
}

/**
 * Handles root degree ranges containing zero.
 *
 * Degree zero is undefined, but other degrees may still produce results.
 */
private fun IntegerRange.rootRangeExcludingZero(
    minDegree: Long,
    maxDegree: Long
): IntegerRange {
    var result = IntegerRange.Empty

    for (degree in minDegree..maxDegree) {
        if (degree == 0L)
            continue

        val current = root(this, degree)

        result = if (result.isEmpty())
            current
        else
            result join current
    }
    return result
}

/**
 * Calculates the exponential function of this integer set.
 * The operation follows constraint propagation semantics:
 *
 *     exp(S) = { e^x | x ∈ S }
 *
 * Since exp is monotonically increasing, the result is determined by the
 * interval boundaries.
 * The returned range is the smallest convex integer set containing all
 * possible results.
 *
 * @return enclosing integer range of all exponential values
 */
fun exp(x: IntegerRange): IntegerRange =
    if (x.isEmpty()) IntegerRange.Empty
    else IntegerRange(LongMath.exp(x.min), LongMath.exp(x.max))


/**
 * Calculates the natural logarithm.
 * Only positive input values are considered.
 * @return enclosing range of logarithmic values
 */
fun ln(x: IntegerRange): IntegerRange {
    if (x.max <= LongBound.Finite(0)) return IntegerRange.Empty
    val lower = when {
            x.min <= LongBound.Finite(0) -> LongBound.NegativeInfinity
            else -> LongMath.ln(x.min)!!
        }
    val upper = LongMath.ln(x.max) ?: return IntegerRange.Empty
    return IntegerRange(lower, upper)
}

fun log(x: IntegerRange, base: IntegerRange): IntegerRange =
    ln(x) / ln(base)

fun log(x: IntegerRange, base: Long): IntegerRange =
    ln(x) / ln(IntegerRange(base))

fun log2(x: IntegerRange): IntegerRange =
    ln(x) / ln(IntegerRange(2L))

fun log10(x: IntegerRange): IntegerRange =
    ln(x) / ln(IntegerRange(10L))


/** Returns the additive inverse. */
fun negate(x: IntegerRange): IntegerRange =
    IntegerRange(-x.max, -x.min)

/** Returns b^exp. */
fun pow(base: IntegerRange, exp: Long): IntegerRange =
    pow(base, exp)

/** Returns b^e for b∈base and e∈exp. */
fun pow(base: IntegerRange, exp: IntegerRange): IntegerRange =
    pow(base, exp)

/**
 * Computes 2^x for a non-negative integer interval.
 *
 * @throws IllegalArgumentException if [x] contains negative values.
 */
fun pow2(x: IntegerRange): IntegerRange = when {
    x.isEmpty() -> IntegerRange.Empty
    x.min < 0L -> throw IllegalArgumentException("pow2 requires x >= 0")
    x.max > 62L -> IntegerRange.All
    else -> IntegerRange(1L shl x.min.finiteValue.toInt(), 1L shl x.max.finiteValue.toInt())
}
