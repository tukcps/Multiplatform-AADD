package io.github.tukcps.aadd.values.integer

import io.github.tukcps.aadd.values.IntegerValue
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.bool.XBool
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToLong

/**
 * A convex subset of the integer numbers.
 *
 * An [IntegerRange] represents a possibly empty, convex subset of
 * the integer numbers. Bounds may be finite or infinite.
 *
 * The predefined constant [Integers] represents the complete set ℤ.
 *
 * Arithmetic operators are implemented in `IntegerRangeOperators.kt`.
 * Mathematical functions are implemented in `IntegerRangeMath.kt`.
 */
class IntegerRange(
    override val min: LongBound,
    override val max: LongBound
) : NumberRange<LongBound>, IntegerValue {

    /**
     * Creates the set of all integer numbers (ℤ).
     */
    constructor() : this(
        LongBound.NegativeInfinity,
        LongBound.PositiveInfinity)

    /**
     * Creates the singleton set {value}.
     *
     * @param value integer value
     */
    constructor(value: LongBound) : this(
        value,
        value)

    /**
     * Creates a convex integer interval [min,max].
     * Overflows cannot be modeled with Long; better use IntegerRange.
     * @param min lower bound
     * @param max upper bound
     */
    constructor(min: Long, max: Long) : this(
        LongBound.Finite(min),
        LongBound.Finite(max))

    /**
     * Creates the smallest convex integer subset enclosing
     * the floating-point interval.
     * @param min lower real bound
     * @param max upper real bound
     */
    constructor(min: Double, max: Double) : this(
        floor(min).roundToLong(),
        ceil(max).roundToLong())

    constructor(numberRange: ClosedRange<LongBound>) : this(
        numberRange.start, numberRange.endInclusive)

    constructor(value: Long) : this(value, value)

    /**
     * Returns whether both bounds are finite.
     */
    override fun isFinite(): Boolean =
        min.isFinite && max.isFinite

    /**
     * Returns whether this set is the singleton {0}.
     */
    override fun isZero(): Boolean =
        min == LongBound.Finite(0) && max == LongBound.Finite(0)

    /**
     * Returns whether this set is the singleton {1}.
     */
    override fun isOne(): Boolean =
        min == LongBound.Finite(1) && max == LongBound.Finite(1)

    /**
     * Returns whether this set equals ℤ.
     */
    fun isIntegers(): Boolean =
        min === LongBound.NegativeInfinity && max === LongBound.PositiveInfinity

    //
    // ------------------ set operations -----------------
    //
    /**
     * Returns whether this set contains [other].
     *
     * @param other subset candidate
     */
    override operator fun contains(other: NumberRange<LongBound>): Boolean =
        !other.isEmpty() &&
                !isEmpty() &&
                other.min >= min &&
                other.max <= max

    /**
     * Returns the intersection of two integer ranges.
     *
     * @param other other range
     * @return intersection
     */
    override infix fun intersect(other: NumberRange<LongBound>): IntegerRange {
        val result = IntegerRange(LongMath.max(min, other.min), LongMath.min(max, other.max))
        return if (result.min > result.max) Empty
        else result
    }

    /**
     * Returns the smallest convex range containing both operands.
     *
     * @param other other range
     * @return convex hull
     */
    override infix fun join(other: NumberRange<LongBound>): IntegerRange =
        IntegerRange(LongMath.min(min, other.min), LongMath.max(max, other.max))

    /**
     * Returns the union of two ranges.
     *
     * For convex integer ranges this equals [join].
     */
    override infix fun union(other: NumberRange<LongBound>): IntegerRange =
        join(other)

    /**
     * Evaluates a comparison between two convex sets.
     * The result is the set of possible Boolean values of the comparison.
     *
     * A comparison is
     * - [XBool.True]  if it holds for every pair of values,
     * - [XBool.False] if it holds for no pair of values,
     * - [XBool.XBool] if it holds for some pairs but not for others,
     * - [XBool.Empty] if one of the operand sets is empty.
     *
     * All interval comparisons reduce to the same algorithm by specifying:
     * - the pair of bounds proving the comparison to be always true, and
     * - the pair of bounds proving the comparison to be always false.
     *
     * For example, `A > B` is
     * - always true iff `A.min > B.max`,
     * - always false iff `A.max <= B.min`,
     * - otherwise both truth values are possible.
     *
     * @param lhs the left-hand bound determining truth
     * @param rhs the right-hand bound determining truth
     * @param lhsFalse the left-hand bound determining falsity
     * @param rhsFalse the right-hand bound determining falsity
     * @param strict whether the comparison is strict (includes equal)
     */
    private fun compare(
        lhs: LongBound,
        rhs: LongBound,
        lhsFalse: LongBound,
        rhsFalse: LongBound,
        strict: Boolean
    ): XBool = when {
            isEmpty() -> XBool.Empty
            strict && lhs > rhs -> XBool.True
            strict && lhsFalse <= rhsFalse -> XBool.False
            !strict && lhs >= rhs -> XBool.True
            !strict && lhsFalse < rhsFalse -> XBool.False
            else -> XBool.All
        }

    //
    // ------------------ (In)Equalities -----------------
    //
    override infix fun greaterThan(other: NumberRange<LongBound>) =
        compare(lhs = min, rhs = other.max, lhsFalse = max, rhsFalse = other.min, strict = true)

    override fun greaterThanOrEquals(other: NumberRange<LongBound>) =
        compare(lhs = min, rhs = other.max, lhsFalse = max, rhsFalse = other.min, strict = false)

    override fun lessThan(other: NumberRange<LongBound>) =
        other.greaterThan(this)

    override fun lessThanOrEquals(other: NumberRange<LongBound>) =
        other.greaterThanOrEquals(this)

    override fun greaterThan(other: LongBound) =
        greaterThan(IntegerRange(other, other))

    override fun greaterThanOrEquals(other: LongBound) =
        greaterThanOrEquals(IntegerRange(other, other))

    override fun lessThan(other: LongBound) =
        lessThan(IntegerRange(other, other))

    override fun lessThanOrEquals(other: LongBound) =
        lessThanOrEquals(IntegerRange(other, other))

    //
    // ---------------- I/O, misc -----------------------
    //
    /**
     * Textual representation of an integer range.
     *
     * Format:
     *
     * ```
     * finite value       -> 42
     * finite interval    -> -5..10
     * negative infinity  -> -*
     * positive infinity  -> *
     * empty range        -> *..-*
     * ```
     *
     * The empty range is represented by an inverted interval where
     * the lower bound is greater than the upper bound.
     */
    override fun toString(): String = when {
            isEmpty() -> "*..-*"
            isScalar() -> min.asString()
            else -> "${min.asString()}..${max.asString()}"
        }

    //
    // ----------------- Singletons, Factory methods ----------------------
    //
    companion object {

        /** The empty set ∅. */
        val Empty = IntegerRange(LongBound.PositiveInfinity, LongBound.NegativeInfinity)

        /** The set of all integers (ℤ). */
        val Integers = IntegerRange()

        /** The singleton set {0}. */
        val Zero = IntegerRange(LongBound.Finite(0))

        /** The singleton set {1}. */
        val One = IntegerRange(LongBound.Finite(1))

        /**
         * Parses an integer range.
         *
         * Supported syntax:
         *
         * ```
         * 42          -> {42}
         * -5..10      -> [-5,10]
         * -*..10      -> [-∞,10]
         * 5..*        -> [5,+∞]
         * -*..*       -> ℤ
         * *..-*       -> Empty
         * ```
         *
         * @param text textual representation
         * @return parsed integer range
         */
        fun parse(text: String): IntegerRange {
            val value = text.trim()

            require(value.isNotEmpty()) {
                "Empty IntegerRange string"
            }

            val parts = value.split("..", limit = 2)

            return if (parts.size == 1) {
                val bound = parseBound(parts[0])
                IntegerRange(bound, bound)
            } else {
                IntegerRange(
                    parseBound(parts[0]),
                    parseBound(parts[1])
                )
            }
        }

        /**
         * Parses a single bound.
         *
         * @param text textual bound
         * @return parsed bound
         */
        private fun parseBound(text: String): LongBound =
            when (text.trim()) {
                "*" -> LongBound.PositiveInfinity
                "-*" -> LongBound.NegativeInfinity
                else -> LongBound.Finite(text.trim().toLong())
            }
    }
}