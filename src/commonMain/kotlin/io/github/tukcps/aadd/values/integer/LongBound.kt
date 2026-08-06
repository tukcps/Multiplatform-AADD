package io.github.tukcps.aadd.values.integer

import io.github.tukcps.aadd.values.Bound
import kotlin.jvm.JvmInline

/**
 * A bound on the extended integer number line.
 *
 * A [LongBound] represents either a finite integer value or an infinite
 * boundary.
 *
 * Invalid mathematical results are not represented as bounds. If an
 * operation has no valid result, the corresponding range is represented
 * by [IntegerRange.Empty].
 */
sealed interface LongBound : Bound {

    /**
     * Returns the finite value.
     * @throws ClassCastException if this bound is not finite
     */
    val finiteValue: Long
        get() = (this as Finite).value

    /**
     * Finite integer bound.
     */
    @JvmInline
    value class Finite(val value: Long) : LongBound

    /**
     * Positive infinity.
     */
    data object PositiveInfinity : LongBound

    /**
     * Negative infinity.
     */
    data object NegativeInfinity : LongBound
    override val isFinite: Boolean
        get() = this is Finite
    override val isInfinite: Boolean
        get() = this === PositiveInfinity || this === NegativeInfinity

    override val isPositiveInfinity: Boolean
        get() = this === PositiveInfinity

    override val isNegativeInfinity: Boolean
        get() = this === NegativeInfinity

    override val isZero: Boolean
        get() = this is Finite && value == 0L

    override val isOne: Boolean
        get() = this is Finite && value == 1L

    /**
     * Compares two bounds on the extended integer number line.
     *
     * Only bounds of the same numeric domain can be compared.
     *
     * @param other bound to compare with
     * @return negative, zero, or positive according to ordering
     */
    override fun compareTo(other: Bound): Int {
        require(other is LongBound) { "Cannot compare LongBound with ${other::class.simpleName}" }
        return LongMath.compare(this, other)
    }

    fun compareTo(other: Long): Int {
        return LongMath.compare(this, LongBound.Finite(other))
    }

    /**
     * Returns a textual representation.
     *
     * Examples:
     *
     * ```
     * 42       -> "42"
     * +∞       -> "*"
     * -∞       -> "-*"
     * ```
     */
    fun asString(): String =
        when (this) {
            is Finite -> value.toString()
            PositiveInfinity -> "*"
            NegativeInfinity -> "-*"
        }
}