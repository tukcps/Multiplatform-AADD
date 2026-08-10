package io.github.tukcps.aadd.values.real

import io.github.tukcps.aadd.values.Bound
import kotlin.jvm.JvmInline

/**
 * A bound on the extended real number line.
 *
 * A [DoubleBound] represents either a finite IEEE-754 floating-point value
 * or an infinite boundary.
 *
 * IEEE-754 NaN is not a valid bound. Operations producing no meaningful
 * real result are handled at range level and represented by [io.github.tukcps.aadd.values.real.ia.RealRange.Empty].
 *
 * Signed zero is preserved because it carries information in IEEE-754
 * arithmetic:
 *
 * ```
 * 1 / +0.0 = +∞
 * 1 / -0.0 = -∞
 * ```
 *
 * Implemented as interface, extending the Double value type.
 * Hence, no performances losses.
 */
sealed interface DoubleBound : Bound {

    /**
     * Returns the finite value.
     *
     * @throws ClassCastException if this bound is infinite
     */
    val finiteValue: Double
        get() = (this as Finite).value

    /**
     * Finite floating-point bound.
     *
     * NaN and infinities are not allowed because they have dedicated
     * representations.
     */
    @JvmInline
    value class Finite(val value: Double) : DoubleBound {
        init {
            require(!value.isNaN())
            require(!value.isInfinite())
        }
    }

    /** Positive infinity. */
    data object PositiveInfinity : DoubleBound

    /** Negative infinity. */
    data object NegativeInfinity : DoubleBound

    override val isFinite: Boolean
        get() = this is Finite

    override val isInfinite: Boolean
        get() = this === PositiveInfinity ||
                this === NegativeInfinity

    override val isPositiveInfinity: Boolean
        get() = this === PositiveInfinity

    override val isNegativeInfinity: Boolean
        get() = this === NegativeInfinity

    /**
     * Tests for zero while preserving IEEE signed zero.
     */
    override val isZero: Boolean
        get() = this is Finite && value == 0.0

    override val isOne: Boolean
        get() = this is Finite && value == 1.0

    /**
     * Compares two bounds on the extended real number line.
     *
     * This is an internal total ordering used for sorting and selecting
     * bounds. Mathematical comparisons of ranges are handled separately.
     *
     * @param other bound to compare with
     * @return negative, zero or positive according to ordering
     */
    override fun compareTo(other: Bound): Int {
        require(other is DoubleBound) { "Cannot compare DoubleBound with ${other::class.simpleName}" }
        return DoubleBoundMath.compare(this, other)
    }

    /**
     * Returns a textual representation.
     *
     * Examples:
     *
     * ```
     * 1.5  -> "1.5"
     * -0.0 -> "-0.0"
     * +∞   -> "*"
     * -∞   -> "-*"
     * ```
     */
    fun asString(): String =
        when (this) {
            is Finite -> value.toString()
            PositiveInfinity -> "*"
            NegativeInfinity -> "-*"
        }

    fun create(value: Double): DoubleBound? = when(value) {
        Double.NEGATIVE_INFINITY -> NegativeInfinity
        Double.POSITIVE_INFINITY -> PositiveInfinity
        Double.NaN -> null
        else -> Finite(value)
    }
}

/**
 * Converts an IEEE-754 Double value into a [DoubleBound].
 *
 * Finite values and infinities are represented explicitly.
 * NaN has no valid bound representation and is returned as `null`.
 *
 * @return corresponding bound or `null` for NaN
 */
fun Double.toDoubleBound(): DoubleBound? =
    when {
        isNaN() -> null
        isInfinite() && this > 0.0 -> DoubleBound.PositiveInfinity
        isInfinite() && this < 0.0 -> DoubleBound.NegativeInfinity
        else -> DoubleBound.Finite(this)
    }