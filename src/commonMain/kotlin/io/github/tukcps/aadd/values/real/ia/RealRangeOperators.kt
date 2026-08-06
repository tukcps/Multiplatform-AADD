package io.github.tukcps.aadd.values.real.ia

import io.github.tukcps.aadd.values.real.DoubleBoundMath

/**
 * Returns the additive inverse of this range.
 */
operator fun RealRange.unaryMinus(): RealRange =
    RealRange(DoubleBoundMath.negate(max), DoubleBoundMath.negate(min))

/**
 * overloaded contains-operator to allow "in" range notation
 */
operator fun ClosedFloatingPointRange<Double>.contains(range: ClosedFloatingPointRange<Double>): Boolean {
    if (this.start > range.start) return false
    if (this.endInclusive < range.endInclusive) return false
    return true
}

/**
 * Arithmetic operations on [RealRange].
 */

/**
 * Returns the sum of two real ranges.
 */
operator fun RealRange.plus(other: RealRange): RealRange =
    add(this, other)

operator fun RealRange.plus(other: Double): RealRange =
    add(this, RealRange(other, other))

/**
 * Returns the difference of two real ranges.
 */
operator fun RealRange.minus(other: RealRange): RealRange =
    subtract(this, other)

/**
 * Mathematical functions on [RealRange].
 *
 * Operations follow set semantics:
 *
 *     f(S) = { f(x) | x ∈ S and f(x) is defined }
 *
 * Results are represented by the smallest convex real range containing
 * all valid results.
 */
operator fun RealRange.times(other: RealRange): RealRange =
    multiply(this, other)

operator fun RealRange.times(other: Double): RealRange =
    multiply(this, RealRange(other, other))

/**
 * Returns the quotient of two real ranges.
 */
operator fun RealRange.div(other: RealRange): RealRange =
    divide(this, other)

