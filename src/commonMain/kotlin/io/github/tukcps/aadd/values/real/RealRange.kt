package io.github.tukcps.aadd.values.real

import io.github.tukcps.aadd.util.minusUlp
import io.github.tukcps.aadd.util.parseUSNumberString
import io.github.tukcps.aadd.util.plusUlp
import io.github.tukcps.aadd.values.BoundKind
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.XBool
import io.github.tukcps.aadd.values.real.math.RoundingMath
import io.github.tukcps.aadd.values.real.math.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.math.Rounding
import kotlinx.serialization.Serializable
import kotlin.math.*


/**
 * This class models a range of values from min to max.
 * @param min the minimum value, by default NEGATIVE_INFINITY
 * @param max the maximum value, by default POSITIVE_INFINITY
 */
@Serializable
open class RealRange(
    final override var min: Double = Double.NEGATIVE_INFINITY,
    final override var max: Double = Double.POSITIVE_INFINITY
) : NumberRange<Double> {

    override val minKind: BoundKind
        get() = if (min == Double.NEGATIVE_INFINITY) BoundKind.NEGATIVE_INFINITY else BoundKind.FINITE

    override val maxKind: BoundKind
        get() = if (max == Double.POSITIVE_INFINITY) BoundKind.POSITIVE_INFINITY else BoundKind.FINITE

    /**
     * Implementation of up/down/nearest rounding
     */
    protected open val math: RoundingMath
        get() = IEEE754RoundingMath

    constructor(c: Double) : this(c, c)
    constructor(r: ClosedRange<Double>) : this(r.start, r.endInclusive)
    constructor(value: String) : this(parse(value).min, parse(value).max)

    /** Checks if the range is finite */
    fun isFinite() = (min.isFinite()) && (max.isFinite())
    fun isReals() = (min == Double.NEGATIVE_INFINITY) && (max == Double.POSITIVE_INFINITY)
    override fun isZero(): Boolean = max == 0.0 && min == 0.0
    override fun isOne():  Boolean = max == 1.0 && min == 1.0

    /**
     * ceiling function for Range
     */
    open fun ceil() : RealRange {
        var lb = ceil(this.min)
        var ub = ceil(this.max)
        lb -= lb.ulp
        ub += ub.ulp

        return RealRange(lb, ub)
    }

    open fun invCeil() : RealRange {
        var lb = this.min - 1.0
        var ub = this.max

        lb -= lb.ulp
        ub += ub.ulp

        return RealRange(lb, ub)
    }

    /**
     * ceiling function for Range
     */
    open fun ceilAsLong() : Long  = ceil(this.max).toLong()

    /**
     * floor function for Range
     */
    open fun floor() = RealRange(floor(this.min), floor(this.max))
    open fun invFloor() : RealRange = RealRange(min, max+1.0)

    /**
     * floor function for Range
     */
    open fun floorAsLong() : Long = floor(this.min).toLong()

    /**
     * TODO Rework this function see TODO below and check for its semantic meaning potentially many tests need to be reworked
     * */
    override fun equals(other: Any?): Boolean {

        if (other == null) return false
        if (this::class != other::class) return false
        if (this === other) return true
        val rr = other as RealRange
        val minLowerBound = min - (3*min.ulp)
        val minUpperBound = min + (3*min.ulp)

        val maxLowerBound = max - (3*max.ulp)
        val maxUpperBound = max + (3*max.ulp)

        // TODO: Make tolerance below parameterizable.
        if (this.min.isInfinite()) return this.max == other.max
        if (this.max.isInfinite()) return this.min == other.min
        return rr.min in (minLowerBound .. minUpperBound) && rr.max in (maxLowerBound .. maxUpperBound)
    }

    override fun greaterThan(other: NumberRange<Double>): XBool {
        if (this === other) return XBool.True
        var retval = XBool.False
        val rr = other as RealRange
        if (min > rr.max) retval = XBool.True
        if (rr.min == min && rr.max == max || max < rr.min)
            return XBool.False
        // The Next 4 Comparisons could be combined into 1 and possibly simplified.  Investigate!
        // L is a subset of R
        if (min > other.min && max < other.max || min == other.min && max < other.max || min > other.min && max == other.max)
            retval = XBool.NaB
        // R is a subset of L
        if (min < other.min && max > other.max || min == other.min && max > other.max || min > other.min && max == other.max)
            retval = XBool.NaB
        // No subset, just overlapping
        if (min > other.min && min < other.max && max > other.max)
            retval = XBool.NaB
        if (min < other.min && max > other.min && max < other.max)
            retval = XBool.NaB
        // Does it also need to be done with respect to the r.min & r.max being contained within L operand range?
        // Is there redundancy here? Investigate!
        if (other.min > min && other.min < max && other.max > max)
            retval = XBool.NaB
        if (other.min < min && other.max > min && other.max < max)
            retval = XBool.NaB

        return retval
    }


    override fun greaterThanOrEquals(other: NumberRange<Double>): XBool {
        if (this === other) return XBool.True
        var retval = XBool.False
        val rr = other as RealRange
        if (min >= rr.min)
            retval = XBool.True

        return retval
    }

    override fun lessThan(other: NumberRange<Double>): XBool {
        if (this === other) return XBool.False
        var retval = XBool.False
        val rr = other as RealRange
        if (max < rr.min) retval = XBool.True
        if (rr.min == min && rr.max == max || min > rr.max)
            return XBool.False
        // The Next 4 Comparisons could be combined into 1 and possibly simplified.  Investigate!
        // L is a subset of R
        if (min > other.min && max < other.max || min == other.min && max < other.max || min > other.min && max == other.max)
            retval = XBool.NaB
        // R is a subset of L
        if (min < other.min && max > other.max || min == other.min && max > other.max || min > other.min && max == other.max)
            retval = XBool.NaB
        // No subset, just overlapping
        if (min > other.min && min < other.max && max > other.max)
            retval = XBool.NaB
        if (min < other.min && max > other.min && max < other.max)
            retval = XBool.NaB
        // Does it also need to be done with respect to the r.min & r.max being contained within L operand range?
        // Is there redundancy here? Investigate!
        if (other.min > min && other.min < max && other.max > max)
            retval = XBool.NaB
        if (other.min < min && other.max > min && other.max < max)
            retval = XBool.NaB

        return retval
    }

    override fun lessThanOrEquals(other: NumberRange<Double>) =
        when {
            this === other -> XBool.True
            this.isEmpty() || other.isEmpty() -> XBool.NaB
            this.min == other.min && this.max == other.max -> XBool.True
            this.max <= other.min -> XBool.True
            this.min >= other.max -> XBool.False
            else -> XBool.X
        }

    override fun lessThanOrEquals(other: Double): XBool =
        when {
            other.isNaN() || this.isEmpty() -> XBool.NaB
            this.max <= other-> XBool.True
            this.min > other -> XBool.False
            else -> XBool.X
        }

    override fun lessThan(other: Double): XBool =
        when {
            other.isNaN() || this.isEmpty() -> XBool.NaB
            this.max < other-> XBool.True
            this.min >= other -> XBool.False
            else -> XBool.X
        }

    override fun greaterThanOrEquals(other: Double): XBool =
        when {
            other.isNaN() || this.isEmpty() -> XBool.NaB
            this.min >= other-> XBool.True
            this.max < other -> XBool.False
            else -> XBool.X
        }


    override fun greaterThan(other: Double): XBool =
        when {
            other.isNaN() || this.isEmpty() -> XBool.NaB
            this.min > other-> XBool.True
            this.max <= other -> XBool.False
            else -> XBool.X
        }

    override fun times(other: Double): RealRange = when {
        other == 0.0 -> RealRange(0.0, 0.0)
        other == 1.0 -> this.clone()
        this.isZero() -> RealRange(0.0, 0.0)
        this.isOne() -> RealRange(other, other)
        else -> RealRange(min(this.min*other, this.max*other).minusUlp(), max(this.min*other, this.max*other).plusUlp())
    }

    override fun div(other: Double): RealRange =
        this * (1.0 / other)

    override fun union(other: NumberRange<Double>) =
        when {
            this.isEmpty() -> other
            other.isEmpty() -> this
            else -> RealRange(min(this.min, other.min), max(this.max, other.max))
        }

    override fun isEmpty(): Boolean = min > max || min.isNaN() || max.isNaN()

    override operator fun plus(other: NumberRange<Double>) = RealRange(
        math.add(min, other.min, Rounding.DOWN),
        math.add(max, other.max, Rounding.UP)
    )

    override operator fun plus(other: Double) = RealRange(
        math.add(min, other, Rounding.DOWN),
        math.add(max, other, Rounding.UP)
    )

    override operator fun minus(other: NumberRange<Double>) = RealRange(
        math.sub(min, other.max, Rounding.DOWN),
        math.sub(max, other.min, Rounding.UP)
    )

    override operator fun minus(other: Double) = RealRange(
        math.sub(min, other, Rounding.DOWN),
        math.sub(max, other, Rounding.UP)
    )

    override operator fun unaryMinus() = RealRange(-max, -min)

    // FIXME Interval exponentiation is only approximated.
    // Proper handling depends on exponent type and monotonicity.
    override fun pow(other: NumberRange<Double>): NumberRange<Double> {
        require(this.min >= 0.0)
        return RealRange(this.min.pow(other.min) .. this.max.pow(other.max))
    }

    override fun pow(other: Double): NumberRange<Double> {
        TODO("Not yet implemented")
    }

    override operator fun times(other: NumberRange<Double>): RealRange {
        val mins = listOf(
            math.mul(min, other.min, Rounding.DOWN),
            math.mul(min, other.max, Rounding.DOWN),
            math.mul(max, other.min, Rounding.DOWN),
            math.mul(max, other.max, Rounding.DOWN)
        )

        val maxs = listOf(
            math.mul(min, other.min, Rounding.UP),
            math.mul(min, other.max, Rounding.UP),
            math.mul(max, other.min, Rounding.UP),
            math.mul(max, other.max, Rounding.UP)
        )

        return RealRange(mins.min(), maxs.max())
    }


    open fun inv(): RealRange = when {
        min == 0.0 -> RealRange(math.div(1.0, max, Rounding.DOWN), Double.POSITIVE_INFINITY)
        max == 0.0 -> RealRange(Double.NEGATIVE_INFINITY, math.div(1.0, min, Rounding.UP))
        0.0 in this -> Reals
        else -> {
            val dTOWARDNEGATIVE1 = math.div(1.0, min, Rounding.DOWN)
            val dTOWARDNEGATIVE2 = math.div(1.0, max, Rounding.DOWN)
            val dTOWARDPOSITIVE1 = math.div(1.0, min, Rounding.UP)
            val dTOWARDPOSITIVE2 = math.div(1.0, max, Rounding.UP)

            RealRange(min(dTOWARDNEGATIVE1, dTOWARDNEGATIVE2), max(dTOWARDPOSITIVE1, dTOWARDPOSITIVE2))
        }
    }

    override operator fun div(other: NumberRange<Double>): RealRange =
        this*RealRange(other).inv()

    override infix fun join(other: NumberRange<Double>) =
        RealRange(min(other.min, min), max(other.max, max))

    override infix fun intersect(other: NumberRange<Double>) =
        RealRange(max(other.min, min), min(other.max, max))

    /** Subset of, named following operator of Kotlin */
    operator fun contains(other: RealRange): Boolean =
        this.min <= other.min && this.max >= other.max

    override operator fun contains(value: Double): Boolean =
        value in min .. max


    fun isProperSubsetOf(b: RealRange): Boolean = min > b.min && max < b.max
    val isStrictlyPositive: Boolean get() = min > 0.0
    val isStrictlyNegative: Boolean get() = max < 0.0
    val isWeaklyPositive: Boolean get() = min >= 0.0
    val isWeaklyNegative: Boolean get() = max <= 0.0

    override fun hashCode()= super.hashCode()

    /** Returns the range as a string, considering also trap representations in format 0000.000E0 */
    override fun toString(): String =
        when {
            isEmpty() -> "∅"
            isScalar() -> {
                if (min.isInfinite()) return "$min"
                if (min.isNaN()) return "∅"
                min.toString()
            }
            this == Reals -> return "Real"
            else -> {
                // capture near-inf as inf ...
                val minStr = if (min < -Double.MAX_VALUE / 2.0) "-*" else min.toString()
                val maxStr = if (max > Double.MAX_VALUE / 2.0) "*" else max.toString()
                "$minStr..$maxStr"
            }
        }

    open fun clone() = RealRange(this.min, this.max)

    override fun sqr(): NumberRange<Double> = when {
        min >= 0.0 -> RealRange(math.mul(min, min, Rounding.DOWN), math.mul(max, max, Rounding.UP))
        max <= 0.0 -> RealRange(math.mul(max, max, Rounding.DOWN), math.mul(min, min, Rounding.UP))
        else -> RealRange(0.0, max(math.mul(min, min, Rounding.UP), math.mul(max, max, Rounding.UP)))
    }

    override fun sqrt(): NumberRange<Double> = when {
        isEmpty() -> Empty
        max < 0.0 -> Empty
        else -> RealRange(math.sqrt(max(0.0, min), Rounding.DOWN), math.sqrt(max, Rounding.UP))
    }

    override fun exp(): NumberRange<Double> = when {
        isEmpty() -> Empty
        else -> RealRange(math.exp(min, Rounding.DOWN), math.exp(max, Rounding.UP))
    }

    override fun log(): NumberRange<Double> = when {
        isEmpty() -> Empty
        max <= 0.0 -> Empty
        min <= 0.0 -> RealRange(Double.NEGATIVE_INFINITY, math.log(max, Rounding.UP))
        else -> RealRange(math.log(min, Rounding.DOWN), math.log(max, Rounding.UP))
    }

    override fun log(other: NumberRange<Double>): NumberRange<Double> =
        this.log() / other.log()

    /**
     * Some constants for different kind of special cases:
     *  * EMPTY is an empty range, i.e. (+infinity, -infinity)
     *  * RANGE is a finite range.
     *  * REAL is a  range that includes all Reals representable as Double
     */
    companion object {
        val Empty = RealRange(Double.MAX_VALUE, -Double.MAX_VALUE)
        val Reals = RealRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)

        private data class Bounds(val min: Double, val max: Double)

        private fun parse(value: String): Bounds {
            if (value.isBlank()) {
                return Bounds(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            }

            val bounds = value.split("..", limit = 2)
            val min = parseLowerBound(bounds[0].trim())
            val max = if (bounds.size == 2) parseUpperBound(bounds[1].trim()) else min

            return Bounds(min, max)
        }

        private fun parseLowerBound(value: String): Double = when (value.uppercase()) {
            "-INF", "-*" -> Double.NEGATIVE_INFINITY
            "INF", "*"   -> Double.POSITIVE_INFINITY
            else         -> parseUSNumberString(value)
        }

        private fun parseUpperBound(value: String): Double = when (value.uppercase()) {
            "-INF", "-*" -> Double.NEGATIVE_INFINITY
            "INF", "*" -> Double.POSITIVE_INFINITY
            else       -> parseUSNumberString(value)
        }
    }
}
