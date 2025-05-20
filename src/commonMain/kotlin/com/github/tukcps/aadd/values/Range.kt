package com.github.tukcps.aadd.values

import com.github.tukcps.aadd.util.minusUlp
import com.github.tukcps.aadd.util.parseUSNumberString
import com.github.tukcps.aadd.util.plusUlp
import kotlinx.serialization.Serializable
import kotlin.math.*


/**
 * This class models a range of values from min to max.
 * @param min the minimum value, by default NEGATIVE_INFINITY
 * @param max the maximum value, by default POSITIVE_INFINITY
 */
@Serializable
open class Range(
    final override var min: Double = Double.NEGATIVE_INFINITY,
    final override var max: Double = Double.POSITIVE_INFINITY
) : NumberRange<Double> {
    override val maxIsInf: Boolean get() = max.isInfinite()
    override val minIsInf: Boolean get() = min.isInfinite()

    /** Checks if the range is finite */
    fun isFinite() = (min.isFinite()) && (max.isFinite())
    fun isReals() = (min == Double.NEGATIVE_INFINITY) && (max == Double.POSITIVE_INFINITY)
    override fun isZero(): Boolean = max == 0.0 && min == 0.0
    override fun isOne():  Boolean = max == 1.0 && min == 1.0

    constructor(range: NumberRange<Double>): this(range.min, range.max)
    constructor(other: Range) : this( other.min, other.max)
    constructor(c: Double) : this(c, c)
    constructor(r: ClosedRange<Double>) : this(r.start, r.endInclusive)

    constructor(str: String) : this() {
        val boundsStr = str.split("..")

        if(str == "") {
            min = -Double.MAX_VALUE
            max = Double.MAX_VALUE
            return
        }

        val lbs = boundsStr[0].trim()
        min = when(lbs) {
            "-INF","*","-*" -> -Double.MAX_VALUE
            "INF" -> Double.MAX_VALUE
            else -> parseUSNumberString(lbs)
        }

        max = if(boundsStr.size>1) {
            val ubs = boundsStr[1].trim()
            if(ubs=="INF" || ubs == "*") Double.MAX_VALUE
            else parseUSNumberString(ubs)
        } else min
    }

    /**
     * ceiling function for Range
     */
    open fun ceil() : Range {
        var lb = ceil(this.min)
        var ub = ceil(this.max)
        lb -= lb.ulp
        ub += ub.ulp

        return Range(lb, ub)
    }

    open fun invCeil() : Range {
        var lb = this.min - 1.0
        var ub = this.max

        lb -= lb.ulp
        ub += ub.ulp

        return Range(lb, ub)
    }

    /**
     * ceiling function for Range
     */
    open fun ceilAsLong() : Long  = ceil(this.max).toLong()

    /**
     * floor function for Range
     */
    open fun floor() = Range(floor(this.min), floor(this.max))
    open fun invFloor() : Range = Range(min, max+1.0)

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
        val rr = other as Range
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
        val rr = other as Range
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
        val rr = other as Range
        if (min >= rr.min)
            retval = XBool.True

        return retval
    }

    /**
     * Quick and dirty relu implementation over real valued intervals
     * */
    fun relu() : Range {
        return Range(max(0.0,this.min),max(0.0,this.max))
    }

    override fun lessThan(other: NumberRange<Double>): XBool {
        if (this === other) return XBool.False
        var retval = XBool.False
        val rr = other as Range
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

    override fun times(other: Double): Range = when {
        other == 0.0 -> Range(0.0, 0.0)
        other == 1.0 -> this.clone()
        this.isZero() -> Range(0.0, 0.0)
        this.isOne() -> Range(other, other)
        else -> Range(min(this.min*other, this.max*other).minusUlp(), max(this.min*other, this.max*other).plusUlp())
    }

    override fun div(other: Double): Range { return this * (1.0 / other) }

    override fun union(other: NumberRange<Double>) =
        when {
            this.isEmpty() -> other
            other.isEmpty() -> this
            else -> Range(min(this.min, other.min), max(this.max, other.max))

        }
    override fun copy(min: Double?, max: Double?) = Range(min = min?: this.min, max = max?: this.max)
    override fun isEmpty(): Boolean = min > max || min.isNaN() || max.isNaN()

    /**
     * Sets minimum and maximum to the values of the given range.
     * @param other value from which values will be copied into this.
     */
    fun becomes(other:Range) { min = other.min; max = other.max }

    override operator fun plus(other: NumberRange<Double>): Range {
        var rMin = min + other.min
        if (rMin.isFinite()) rMin -= rMin.ulp
        var rMax = max + other.max
        if (rMax.isFinite()) rMax += rMax.ulp
        return Range(rMin, rMax)
    }

    override operator fun plus(other: Double): Range {
        var rMin = min + other
        rMin -= rMin.ulp
        var rMax = max + other
        rMax += rMax.ulp
        return Range(rMin, rMax)
    }

    /**
     * Subtraction of two ranges including FP round-off error.
     */
    override operator fun minus(other: NumberRange<Double>): Range {
        var rMin = min - other.max
        rMin -= rMin.ulp
        var rMax = max - other.min
        rMax += rMax.ulp
        return Range(rMin, rMax)
    }

    override operator fun minus(other: Double): Range {
        var rMin = min - other
        rMin -= rMin.ulp
        var rMax = max - other
        rMax += rMax.ulp
        return Range(rMin, rMax)
    }

    override operator fun unaryMinus() = Range(min(-max, -min), max(-max, -min))

    override fun pow(other: NumberRange<Double>): NumberRange<Double> {
        require(this.min >= 0.0)
        return Range(this.min.pow(other.min) .. this.max.pow(other.max))
    }

    override fun pow(other: Double): NumberRange<Double> {
        TODO("Not yet implemented")
    }

    /**
     * The multiplication of two ranges including FP round-off error.
     */
    override operator fun times(other: NumberRange<Double>): Range {
        val iaMult = listOf(min * other.min, min * other.max, max * other.min, max * other.max)
        var resultMin = iaMult.min()
        var resultMax = iaMult.max()
        resultMin -= resultMin.ulp
        resultMax += resultMax.ulp
        return Range(resultMin, resultMax)
    }


    open fun inv(): Range = when {
            min == 0.0  -> Range( (1/max).minusUlp(), Double.POSITIVE_INFINITY)
            max == 0.0  -> Range( Double.NEGATIVE_INFINITY, (-1/min).plusUlp())
            0.0 in this -> Reals
            else        -> Range(min(1/min, 1/max).minusUlp(), max(1/min, 1/max).plusUlp())
        }

    override operator fun div(other: NumberRange<Double>): Range =
        this*Range(other).inv()

    override infix fun join(other: NumberRange<Double>) =
        Range(min(other.min, min), max(other.max, max))

    override infix fun intersect(other: NumberRange<Double>) =
        Range(max(other.min, min), min(other.max, max))

    /** Subset of, named following operator of Kotlin */
    operator fun contains(other: Range): Boolean =
        this.min <= other.min && this.max >= other.max

    override operator fun contains(value: Double): Boolean =
        value in min .. max

    fun isProperSubsetof(b: Range): Boolean {
        return min > b.min && max < b.max
    }

    val isStrictlyPositive: Boolean get() = min > 0.0
    val isStrictlyNegative: Boolean get() = max < 0.0
    val isWeaklyPositive: Boolean get() = min >= 0.0
    val isWeaklyNegative: Boolean get() = max <= 0.0

    override fun hashCode()= super.hashCode()

    fun toIntegerRange(): IntegerRange =
        IntegerRange(min.toLong(), max.toLong())

    /** Returns the range as a string, considering also trap representations in format 0000.000E0 */
    override fun toString(): String {
        when {
            isEmpty() -> return "∅"
            isScalar() -> {
                if (min.isInfinite()) return "$min"
                if (min.isNaN()) return "∅"
                return min.toString()
            }
            this == Reals -> return "Real"
            else -> {
                // capture near-inf as inf ...
                val minstr = if (min < -Double.MAX_VALUE / 2.0) "-*" else min.toString()
                val maxstr = if (max > Double.MAX_VALUE / 2.0) "*" else max.toString()
                return "$minstr..$maxstr"
            }
        }
    }

    open fun clone() = Range(this.min, this.max)


     /**
     * The square function.
     */
    override fun sqr(): NumberRange<Double> =
        Range(min(this.min*this.min, this.max*this.max), max(this.min*this.min, this.max*this.max))

    override fun sqrt(): NumberRange<Double> = when {
            isEmpty() -> Empty
            max < 0   -> Empty
            else      -> Range(sqrt(min).minusUlp(), sqrt(max).plusUlp())
        }


    override fun root(other: NumberRange<Double>): NumberRange<Double> =
        TODO("Not yet implemented")

    override fun exp(): NumberRange<Double> {
        TODO("Not yet implemented")
    }

    override fun log(): NumberRange<Double> {
        TODO("Not yet implemented")
    }
    
    override fun log(other: NumberRange<Double>): NumberRange<Double> {
        TODO("Not yet implemented")
    }
    /**
     * Some constants for different kind of special cases:
     *  * EMPTY is an empty range, i.e. (+infinity, -infinity)
     *  * RANGE is a finite range.
     *  * REAL is a  range that includes all Reals representable as Double
     */
    companion object { /** Some constants that simplify work ...  */
        val Empty = Range(Double.MAX_VALUE, -Double.MAX_VALUE)
        val Reals = Range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)

        @Deprecated("Dont use; use Empty instead to represent that there is no valid result.", ReplaceWith("Empty"))
        val RealsNaN = Range(-Double.NaN, Double.NaN)
    }
}

/** overloaded contains-operator to allow "in" range notation */
operator fun ClosedFloatingPointRange<Double>.contains(range: ClosedFloatingPointRange<Double>): Boolean {
    if (this.start > range.start) return false
    if (this.endInclusive < range.endInclusive) return false
    return true
}

/**  Extension Functions developed by Jack **/
fun ceil(input : Range) : Range = input.ceil()

fun invCeil(input : Range) : Range = input.invCeil()

fun floor(input : Range) : Range = input.floor()

fun invFloor(input : Range) : Range = input.invFloor()
