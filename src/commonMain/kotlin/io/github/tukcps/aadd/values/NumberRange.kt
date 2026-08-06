package io.github.tukcps.aadd.values

import io.github.tukcps.aadd.values.bool.XBool

/**
 * ## NumberRange
 * A closed range over an ordered domain given by bounds of type `Bound`.
 * Bound has null and one elements, and can be Finite, Infinite, or Unknown.
 *
 * A number range
 * - consists a lower and an upper bound,
 * - defines null and one elements, and
 * - can be Empty, a Scalar, or Finite/Infinite.
 *
 * Implementations provide operations on ranges:
 * - (in) equality.
 * - containment,
 * - intersection, and
 * - union.
 * Arithmetic operations are intentionally not part of this interface.
 * @param B the bound type
 */
interface NumberRange<B : Bound> : ClosedRange<B>, NumericValue {

    /** Lower bound of this range. */
    val min: B

    /** Upper bound of this range. */
    val max: B

    /**
     * To implement the ClosedRange interface
     */
    override val start: B
        get() = min

    override val endInclusive: B
        get() = max

    /**
     * Returns whether this range contains no values.
     */
    override fun isEmpty(): Boolean = min > max

    /**
     * Returns whether both bounds are finite.
     */
    fun isFinite(): Boolean = min.isInfinite && max.isInfinite && !isEmpty()

    /**
     * Returns whether this range represents exactly one value.
     */
    fun isScalar(): Boolean = !isEmpty() && min == max

    /**
     * Returns whether this range represents zero.
     */
    fun isZero(): Boolean = min.isZero && max.isZero

    /**
     * Returns whether this range represents one.
     */
    fun isOne(): Boolean = min.isOne && max.isOne

    /**
     * Returns whether this range contains the specified value.
     *
     * @param value the value to test
     * @return `true` iff the value is contained in this range
     */
    override operator fun contains(value: B): Boolean =
        !isEmpty() && value >= min && value <= max

    /**
     * Returns whether this range completely contains another range.
     *
     * @param other the range to test
     * @return `true` iff this range contains the other range
     */
    operator fun contains(other: NumberRange<B>): Boolean =
        !other.isEmpty() && other.min >= min && other.max <= max

    /**
     * Returns the intersection of this range and another range.
     *
     * @param other the other range
     * @return the common part of both ranges
     */
    infix fun intersect(other: NumberRange<B>): NumberRange<B>

    /**
     * Returns the smallest range containing this range and another range.
     *
     * @param other the other range
     * @return the convex hull of both ranges
     */
    infix fun join(other: NumberRange<B>): NumberRange<B>

    /**
     * Returns the union of this range and another range.
     * For convex ranges this is identical to [join].
     *
     * @param other the other range
     * @return the union of both ranges
     */
    infix fun union(other: NumberRange<B>): NumberRange<B> =
        join(other)

    /**
     * Compares this range with a scalar value.
     *
     * @param other the value to compare with
     * @return the three-valued comparison result
     */
    infix fun greaterThan(other: B): XBool

    /**
     * Compares this range with a scalar value.
     *
     * @param other the value to compare with
     * @return the three-valued comparison result
     */
    infix fun greaterThanOrEquals(other: B): XBool

    /**
     * Compares this range with a scalar value.
     *
     * @param other the value to compare with
     * @return the three-valued comparison result
     */
    infix fun lessThan(other: B): XBool

    /**
     * Compares this range with a scalar value.
     *
     * @param other the value to compare with
     * @return the three-valued comparison result
     */
    infix fun lessThanOrEquals(other: B): XBool

    /**
     * Compares this range with another range.
     *
     * @param other the other range
     * @return the three-valued comparison result
     */
    infix fun greaterThan(other: NumberRange<B>): XBool

    /**
     * Compares this range with another range.
     *
     * @param other the other range
     * @return the three-valued comparison result
     */
    infix fun greaterThanOrEquals(other: NumberRange<B>): XBool

    /**
     * Compares this range with another range.
     *
     * @param other the other range
     * @return the three-valued comparison result
     */
    infix fun lessThan(other: NumberRange<B>): XBool

    /**
     * Compares this range with another range.
     *
     * @param other the other range
     * @return the three-valued comparison result
     */
    infix fun lessThanOrEquals(other: NumberRange<B>): XBool
}