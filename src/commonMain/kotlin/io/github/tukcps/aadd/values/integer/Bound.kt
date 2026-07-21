package io.github.tukcps.aadd.values.integer

import io.github.tukcps.aadd.values.BoundKind

/**
 * A bound on the extended integer number line.
 *
 * Besides finite integer values, bounds may also represent positive or
 * negative infinity or an undefined value ([NaN]).
 *
 * The total ordering is
 *
 *     -∞ < finite values < +∞ < NaN
 */
sealed interface Bound : Comparable<Bound> {

    /**
     * A finite integer value.
     */
    data class Finite(val value: Long) : Bound

    /**
     * Positive infinity.
     */
    data object PositiveInfinity : Bound

    /**
     * Negative infinity.
     */
    data object NegativeInfinity : Bound

    /**
     * Undefined value.
     */
    data object NaN : Bound

    /**
     * Returns whether this bound is finite.
     */
    val isFinite: Boolean
        get() = this is Finite

    /**
     * Returns whether this bound is infinite.
     */
    val isInfinite: Boolean
        get() = this === PositiveInfinity || this === NegativeInfinity

    /**
     * Returns whether this bound is undefined.
     */
    val isNaN: Boolean
        get() = this === NaN

    /**
     * Returns whether this bound represents zero.
     */
    val isZero: Boolean
        get() = this is Finite && value == 0L

    /**
     * Returns the sign of this bound.
     *
     * * -1 for negative values and -∞
     * *  0 for zero and NaN
     * * +1 for positive values and +∞
     */
    val sign: Int
        get() = when (this) {
            is Finite -> value.compareTo(0)
            PositiveInfinity -> 1
            NegativeInfinity -> -1
            NaN -> 0
        }

    val isPositive: Boolean
        get() = sign > 0

    val isNegative: Boolean
        get() = sign < 0

    /**
     * Returns the finite value.
     *
     * The receiver must be finite.
     */
    val finiteValue: Long
        get() = (this as Finite).value

    /**
     * Compares this bound with [other].
     *
     * Ordering:
     *
     *     -∞ < finite values < +∞ < NaN
     */
    override fun compareTo(other: Bound): Int = when {
        this === other -> 0

        this === NaN -> 1
        other === NaN -> -1

        this === NegativeInfinity -> -1
        other === NegativeInfinity -> 1

        this === PositiveInfinity -> 1
        other === PositiveInfinity -> -1

        else -> finiteValue.compareTo(other.finiteValue)
    }
}

/**
 * Returns this value as a finite bound.
 */
internal fun Long.bound(): Bound =
    Bound.Finite(this)

internal fun Long.bound(kind: BoundKind): Bound =
    when (kind) {
        BoundKind.FINITE -> Bound.Finite(this)
        BoundKind.NEGATIVE_INFINITY -> Bound.NegativeInfinity
        BoundKind.POSITIVE_INFINITY -> Bound.PositiveInfinity
    }

/**
 * Returns this value as a finite bound.
 */
internal fun Int.bound(): Bound =
    Bound.Finite(this.toLong())

/**
 * Saves a value of type Long and a representation of its limitation or bound kind.
 * - FINITE
 * - NEGATIVE_INFINITY
 * - POSITIVE_INFINITY
 */
internal data class LongBound(
    val value: Long,
    val kind: BoundKind
)

/**
 * Brings a Bound into its canonical representation.
 */
internal fun Bound.toLongBound(): LongBound =
    when (this) {
        is Bound.Finite -> LongBound(value, BoundKind.FINITE)
        Bound.NegativeInfinity -> LongBound(Long.MIN_VALUE, BoundKind.NEGATIVE_INFINITY)
        Bound.PositiveInfinity -> LongBound(Long.MAX_VALUE, BoundKind.POSITIVE_INFINITY)
        Bound.NaN -> error("NaN cannot be stored in IntegerRange.")
    }

internal fun Bound.sameSign(other: Bound): Boolean =
    sign == other.sign

/**
 * Multiplication.
 */
operator fun Bound.times(other: Bound): Bound = when {

    // NaN propagates
    this === Bound.NaN || other === Bound.NaN -> Bound.NaN

    // finite × finite
    this is Bound.Finite && other is Bound.Finite -> IntegerMath.mul(value, other.value)

    // 0 × ∞  (undefined)
    this is Bound.Finite && value == 0L && other.isInfinite -> Bound.NaN

    // ∞ × 0  (undefined)
    other is Bound.Finite && other.value == 0L && this.isInfinite -> Bound.NaN

    // finite × ±∞
    this is Bound.Finite && other.isInfinite -> if (value > 0) other else -other

    // ±∞ × finite
    other is Bound.Finite && this.isInfinite ->
        if (other.value > 0) this else -this

    // ±∞ × ±∞
    this.isInfinite && other.isInfinite ->
        if (this.sign == other.sign) Bound.PositiveInfinity
        else Bound.NegativeInfinity

    else -> Bound.NaN
}

/**
 * Divides this bound by [other].
 *
 * The operation is defined on extended integer values and follows solver-oriented
 * semantics. Division by zero yields an infinity with the sign of the dividend
 * whenever possible, allowing interval computations to preserve useful bounds.
 *
 * The following rules apply:
 *
 *     finite / finite     -> finite (using IntegerMath.div)
 *     finite / ±∞         -> 0
 *     ±∞ / finite         -> ±∞
 *     ±∞ / ±∞             -> NaN
 *
 *     positive / 0        -> +∞
 *     negative / 0        -> -∞
 *     0 / 0               -> NaN
 *     +∞ / 0              -> +∞
 *     -∞ / 0              -> -∞
 *
 * Any operation involving [Bound.NaN] yields [Bound.NaN].
 */
operator fun Bound.div(other: Bound): Bound = when {

    isNaN || other.isNaN -> Bound.NaN

    other.isZero -> when {
            isPositive -> Bound.PositiveInfinity
            isNegative -> Bound.NegativeInfinity
            else -> Bound.NaN
        }

    isFinite && other.isFinite -> IntegerMath.div(finiteValue, other.finiteValue)

    isFinite && other.isInfinite -> Bound.Finite(0)

    isInfinite && other.isFinite -> if (sign == other.sign) Bound.PositiveInfinity
        else Bound.NegativeInfinity

    isInfinite && other.isInfinite -> Bound.NaN
    else -> Bound.NaN
}