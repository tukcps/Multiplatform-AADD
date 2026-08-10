package io.github.tukcps.aadd.values.real.ia

import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBound.*
import io.github.tukcps.aadd.values.real.DoubleBoundMath
import io.github.tukcps.aadd.values.real.toDoubleBound

/**
 * A convex subset of the real numbers.
 *
 * A [RealRange] represents a possibly empty convex subset of ℝ.
 * Bounds may be finite or infinite.
 *
 * IEEE-754 NaN is not represented. Invalid mathematical results are handled
 * by returning [Empty] if no valid values exist.
 *
 * Arithmetic operations are implemented in `RealRangeOperators.kt`.
 * Mathematical functions are implemented in `RealRangeMath.kt`.
 */
open class RealRange(
    override val min: DoubleBound,
    override val max: DoubleBound
) : NumberRange<DoubleBound> {

    /** Creates the set of all real numbers. */
    constructor() : this(NegativeInfinity, PositiveInfinity)

    /** Creates a point interval. */
    constructor(value: Double) : this(Finite(value), Finite(value))

    /** Creates the closed interval [min,max]. */
    constructor(min: Double, max: Double) : this(Finite(min), Finite(max))

    /** Creates a range from another closed range. */
    constructor(range: ClosedRange<Double>) : this(range.start, range.endInclusive)

    /** Creates a range from another NumberRange<DoubleBound> */
    constructor(range: NumberRange<DoubleBound>) : this(range.min, range.max)

    override fun isFinite(): Boolean =
        min.isFinite && max.isFinite

    /** Returns whether this range represents ℝ. */
    fun isReals(): Boolean =
        min === NegativeInfinity && max === PositiveInfinity

    override fun isZero(): Boolean =
        isScalar() && min.isZero

    override fun isOne(): Boolean =
        isScalar() && min.isOne

    override infix fun intersect(other: NumberRange<DoubleBound>): RealRange =
        RealRange(
            DoubleBoundMath.max(min, other.min),
            DoubleBoundMath.min(max, other.max)
        )

    override infix fun join(other: NumberRange<DoubleBound>): RealRange = when {
            isEmpty() -> RealRange(other.min, other.max)
            other.isEmpty() -> this
            else -> RealRange(
                DoubleBoundMath.min(min, other.min),
                DoubleBoundMath.max(max, other.max)
            )
        }

    override infix fun union(other: NumberRange<DoubleBound>): RealRange =
        join(other)

    override fun contains(value: DoubleBound): Boolean =
        !isEmpty() && value >= min && value <= max

    operator fun contains(value: Double): Boolean =
        contains(Finite(value))

    override fun contains(other: NumberRange<DoubleBound>): Boolean =
        !other.isEmpty() && other.min >= min && other.max <= max

    override fun greaterThan(other: NumberRange<DoubleBound>): XBool =
        when {
            isEmpty() || other.isEmpty() -> XBool.Empty
            min > other.max -> XBool.True
            max <= other.min -> XBool.False
            else -> XBool.All
        }

    override fun greaterThanOrEquals(other: NumberRange<DoubleBound>): XBool =
        when {
            isEmpty() || other.isEmpty() -> XBool.Empty
            min >= other.max -> XBool.True
            max < other.min -> XBool.False
            else -> XBool.All
        }

    override fun lessThan(other: NumberRange<DoubleBound>): XBool =
        other.greaterThan(this)

    override fun lessThanOrEquals(other: NumberRange<DoubleBound>): XBool =
        other.greaterThanOrEquals(this)

    override fun greaterThan(other: DoubleBound): XBool =
        greaterThan(RealRange(other, other))

    override fun greaterThanOrEquals(other: DoubleBound): XBool =
        greaterThanOrEquals(RealRange(other, other))

    override fun lessThan(other: DoubleBound): XBool =
        lessThan(RealRange(other, other))

    override fun lessThanOrEquals(other: DoubleBound): XBool =
        lessThanOrEquals(RealRange(other, other))

    override fun toString(): String =
        when {
            isEmpty() -> "∅"
            isScalar() -> min.asString()
            isReals() -> "Real"
            else -> "${min.asString()}..${max.asString()}"
        }

    /**
     * Copy like in data class.
     */
    open fun copy(
        min: DoubleBound = this.min,
        max: DoubleBound = this.max
    ): RealRange =
        RealRange(min, max)

    /**
     * Otherwise, only referential equality.
     */
    override fun equals(other: Any?): Boolean =
        other is RealRange && min == other.min && max == other.max

    override fun hashCode(): Int =
        31 * min.hashCode() + max.hashCode()

    companion object {

        /** The empty set ∅. */
        val Empty = RealRange(PositiveInfinity, NegativeInfinity)

        /** The set of all real numbers ℝ. */
        val Reals = RealRange()
        val Zero = RealRange(0.0)
        val One = RealRange(1.0)

        fun create(value: Double): RealRange =
            if (value.isNaN()) Empty
            else RealRange(value.toDoubleBound()!!, value.toDoubleBound()!!)

        fun create(min: Double, max: Double): RealRange =
            if (min.isNaN() || max.isNaN()) Empty
            else RealRange(min.toDoubleBound()!!, max.toDoubleBound()!!)

        /**
         * Parses a real range.
         *
         * Supported syntax:
         *
         * ```
         * 1.5
         * 1.0..2.0
         * -*..*
         * Real
         * ∅
         * ```
         */
        fun parse(text: String): RealRange {
            val value = text.trim()

            return when {
                value == "∅" -> Empty
                value.equals("Real", true) -> Reals
                ".." in value -> {
                    val parts = value.split("..", limit = 2)
                    RealRange(
                        parseBound(parts[0]),
                        parseBound(parts[1])
                    )
                }
                else -> RealRange(parseBound(value), parseBound(value))
            }
        }

        private fun parseBound(value: String): DoubleBound =
            when (value.trim().uppercase()) {
                "-*" , "-INF" -> NegativeInfinity
                "*" , "INF" -> PositiveInfinity
                else -> Finite(value.trim().toDouble())
            }
    }
}