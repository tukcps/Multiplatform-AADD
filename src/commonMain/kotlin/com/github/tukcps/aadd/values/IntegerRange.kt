package com.github.tukcps.aadd.values

import com.github.tukcps.aadd.util.parseUSLongString
import kotlinx.serialization.Serializable
import kotlin.math.*

/**
 * The package jAADD implements an IntegerRange
 *
 * @author Christoph Grimm, Jack D. Martin
 */

@Serializable
class IntegerRange(
    override var min: Long,
    override var max: Long,
    override var maxIsInf: Boolean = false,
    override var minIsInf: Boolean = false,
) : NumberRange<Long> { // end class IntegerRange

    constructor(): this (Long.MIN_VALUE, Long.MAX_VALUE)
    constructor(c: Long) : this(c, c)
    constructor(r: ClosedRange<Long>): this(r.start, r.endInclusive)

    /**
     * new constructor
     * takes one Double parameter
     * It is necessary to make certain the Double falls within the range of the Long data type
     * This check must be performed.  Verify whether the Java Math library already handles this, or not.
     * If not, then a check must be performed before type conversion.
     */
    constructor(init : Double) : this (Long.MIN_VALUE, Long.MAX_VALUE) {
        min = floor(init).roundToLong()
        max = ceil(init).roundToLong()
    }

    /**
     * Constructor takes two Double parameters.
     * It is necessary to make certain the Double falls within the range of the Long data type
     * This check must be performed.  Verify whether the Java Math library already handles this or not.
     * If not, then a check must be performed before type conversion.
     */
    constructor(initL : Double, initH : Double) : this (Long.MIN_VALUE, Long.MAX_VALUE) {
        min = floor(initL).roundToLong()
        max = ceil(initH).roundToLong()
    }

    constructor(string: String) : this() {
        // val locale = Locale.getDefault() // Current locale (decimal point, etc.)
        var str = string.removePrefix("[")
        str = str.removeSuffix("]")
        val boundsStr = str.split("..")

        if (str == "") {
            min = Long.MIN_VALUE
            max = Long.MAX_VALUE
            return
        }
        val lbs = boundsStr[0].trim()
        min = when (lbs) {
            "-INF", "*" -> Long.MIN_VALUE
            "INF" -> Long.MAX_VALUE
            else -> parseUSLongString(lbs)
        }

        max = if (boundsStr.size > 1) {
            val ubs = boundsStr[1].trim()
            if (ubs == "INF" || ubs == "*") Long.MAX_VALUE
            else parseUSLongString(ubs)
        } else min
    }

    override fun copy(min: Long?, max: Long?): IntegerRange = IntegerRange(
        min = min?: this.min,
        max = max?: this.max
    )

    override fun isZero(): Boolean = min == 0L && max == 0L
    override fun isOne(): Boolean = min == 1L && max == 1L
    fun isIntegers(): Boolean = min == Long.MIN_VALUE && max == Long.MAX_VALUE

    companion object {
        val Empty get() = IntegerRange(Long.MAX_VALUE, Long.MIN_VALUE)
        val Integers get() = IntegerRange(Long.MIN_VALUE, Long.MAX_VALUE)
    }

    /**
     * A constructor that allows us to create an Integer Range from a string number .. number
     */
    // TODO
    /*
    constructor(string: String) : this() {
        // val locale = Locale.getDefault() // Current locale (decimal point, etc.)
        var str =  string.removePrefix("[")
        str = str.removeSuffix("]")
        val nf = NumberFormat.getInstance()
        val boundsStr = str.split("..")

        if (str == "") {
            min = Long.MIN_VALUE
            max = Long.MAX_VALUE
            return
        }
        val lbs = boundsStr[0].trim()
        min = when (lbs) {
            "-INF", "*" -> Long.MIN_VALUE
            "INF" -> Long.MAX_VALUE
            else -> nf.parse(lbs).toLong()
        }

        max = if (boundsStr.size > 1) {
            val ubs = boundsStr[1].trim()
            if (ubs == "INF" || ubs == "*") Long.MAX_VALUE
            else nf.parse(ubs).toLong()
        } else min
    }*/

    /** TODO */
    fun clone(): IntegerRange{
        return IntegerRange(this.min,this.max)
        //= super.clone() as IntegerRange
    }

    /**
     * intersection of two IntegerRanges
     * @param other : IntegerRange
     * @return IntegerRange
     */
    override infix fun intersect(other: NumberRange<Long>): IntegerRange {
        min = max(other.min, min)
        max = min(other.max, max)
        return IntegerRange(min, max)
    }

    /**
     * union of two IntegerRanges
     * @param other : IntegerRange
     * @return IntegerRange
     */
    override infix fun join(other: NumberRange<Long>): IntegerRange {
        min = min(other.min, min)
        max = max(other.max, max)
        return IntegerRange(min, max)
    }

    override infix fun union(other: NumberRange<Long>) : IntegerRange = join(other)

    override operator fun div(other: NumberRange<Long>): IntegerRange {
        if (min.toInt() == 0 && max.toInt() == 0 && 0 !in other) // 0:x=0 for x!=0
            return IntegerRange(0, 0)
        return if (other.min.toInt() == 0 || other.max.toInt() == 0)
            IntegerRange()
        else {
            val intermeds: MutableList<Double> = mutableListOf(
                asReal(min) / other.min, asReal(min) / other.max,
                asReal(max) / other.min, asReal(max) / other.max
            )
            IntegerRange(floor(intermeds.min()).toLong(), ceil(intermeds.max()).toLong())
        }
    }

    private fun asReal(value: Long): Double {
        return when (value) {
            Integers.max -> Double.POSITIVE_INFINITY
            Integers.min -> Double.NEGATIVE_INFINITY
            else -> value.toDouble()
        }
    }

    operator fun div(other: Int): IntegerRange {
        return if (0 in this) IntegerRange()
        else {
            val otherAsReal = when (other) { // Do not use As Real, because this works only for Long
                Int.MAX_VALUE -> Double.POSITIVE_INFINITY
                Int.MIN_VALUE -> Double.NEGATIVE_INFINITY
                else -> other.toDouble()
            }
            val otherinv: Double = 1.0 / otherAsReal
            val lb: Double = asReal(min) * otherinv
            val ub: Double = asReal(max) * otherinv
            IntegerRange(floor(min(lb, ub)).toLong(), ceil(max(lb, ub)).toLong())
        }
    }

    fun inv(): IntegerRange {
        return if (0 in this)
            IntegerRange(Long.MIN_VALUE, Long.MAX_VALUE)
        else {
            IntegerRange(floor(1.0.div(max.toDouble())).toLong(), ceil(1.0.div(min.toDouble())).toLong())
        }
    }

    override operator fun minus(other: NumberRange<Long>): IntegerRange =
        IntegerRange(minusOverflowDetection(min, other.max), minusOverflowDetection(max, other.min)) //a - b = a + (-b)

    operator fun plus(other: Int): NumberRange<Long> =
        IntegerRange(plusOverflowDetection(min, other.toLong()), plusOverflowDetection(max, other.toLong()))

    override operator fun plus(other: NumberRange<Long>): IntegerRange =
        IntegerRange(plusOverflowDetection(min, other.min), plusOverflowDetection(max, other.max))

    /**
     * Avoids overflow in addition value1 + value2
     * Smallest and biggest value for Long are used as Infinity, so adding and subtracting not infinite values do not change result.
     */
     fun plusOverflowDetection(value1: Long, value2: Long): Long {
        var result = value1 + value2
        //signs do not match > potential overflow
        if (value1.sign == value2.sign && result.sign != value1.sign) //detect Overflow (if signs of values are same, the result must have the same sign)
        // no overflow if 0 is included
            if (value1.sign != 0 && value2.sign != 0)
                result = if (value1.sign == 1) Integers.max else Integers.min //Set result to Infinity
        // cases: oo-x=oo, -oo+x=-oo
        if (value1.sign != value2.sign) {
            if (isInfinite(value1))
                result = if (value1.sign == 1) Integers.max else Integers.min
            if (isInfinite(value2))
                result = if (value2.sign == 1) Integers.max else Integers.min
        }
        return result
    }

    /**
     * Avoids overflow in subtraction value1 - value2
     * Smallest and biggest value for Long are used as Infinity, so adding and subtracting not infinite values do not change result.
     */
     fun minusOverflowDetection(value1: Long, value2: Long): Long {
        var result = value1 - value2
        //signs do not match > potential overflow
        if (value1.sign != value2.sign && result.sign != value1.sign) //detect Overflow (if signs of values are different, the result must have the same sign)
        // no overflow if 0 is included
            if (value1.sign != 0 && value2.sign != 0)
                result = if (value1.sign == 1) Integers.max else Integers.min //Set result to Infinity
        // cases: oo-x=oo, -oo+x=-oo
        if (value1.sign == value2.sign) {
            if (isInfinite(value1))
                result = if (value1.sign == 1) Integers.max else Integers.min
            if (isInfinite(value2))
                result = if (value2.sign == 1) Integers.min else Integers.max
        }
        return result
    }

    /**
     * Test if a value is infinite (either minimal or maximal Long value)
     */
    private fun isInfinite(value: Long): Boolean {
        return value == Integers.max || value == Integers.min || value == -Integers.min || value == -Integers.max
    }

    override operator fun times(other: NumberRange<Long>): IntegerRange {
        val iaMult = listOf(
            timesOverflowDetection(min, other.min), timesOverflowDetection(min, other.max),
            timesOverflowDetection(max, other.min), timesOverflowDetection(max, other.max)
        )
        //return IntegerRange(Collections.min(iaMult), Collections.max(iaMult))
        return IntegerRange(iaMult.min(), iaMult.max())
    }

    /**
     * Detect overflow for multiplication (value1 * value2)
     * Smallest and biggest value for Long are used as Infinity, so adding and subtracting not infinite values do not change result.
     */
    private fun timesOverflowDetection(value1: Long, value2: Long): Long {
        var result = value1 * value2
        //backwards calculation gives wrong result > overflow detected
        if (value1 != 0L && result / value1 != value2) //detect Overflow by using the reverse operation
            result = if (value1.sign == value2.sign) Integers.max else Integers.min //Set result to Infinity
        return result
    }

    override fun div(other: Long): NumberRange<Long> {
        return if (0 in this) IntegerRange()
        else {
            val otherinv: Double = 1.0 / asReal(other)
            val lb: Double = asReal(min) * otherinv
            val ub: Double = asReal(max) * otherinv
            IntegerRange(floor(min(lb, ub)).toLong(), ceil(max(lb, ub)).toLong())
        }
    }

    override fun minus(other: Long): NumberRange<Long> {
        return IntegerRange(minusOverflowDetection(min, other), minusOverflowDetection(max, other))
    }

    override fun plus(other: Long): NumberRange<Long> {
        return IntegerRange(plusOverflowDetection(min, other), plusOverflowDetection(max, other))
    }

    override fun times(other: Long): NumberRange<Long> {
        TODO("Not yet implemented")
    }

    override fun greaterThan(other: Long): XBool =
        if (other < min) XBool.True else if (other > max) XBool.False else XBool.X

    override fun greaterThanOrEquals(other: Long): XBool =
        if (other <= min) XBool.True else if (other >= max) XBool.False else XBool.X

    override fun lessThan(other: Long): XBool =
        if (other > max) XBool.True else if (other < min) XBool.False else XBool.X

    override fun lessThanOrEquals(other: Long): XBool =
        if (other >= max) XBool.True else if (other <= min) XBool.False else XBool.X

    override operator fun unaryMinus(): IntegerRange =
        IntegerRange(-max, -min)

    operator fun rem(r: IntegerRange): IntegerRange {
        val iaMult = listOf(min % r.min, min % r.max, max % r.min, max % r.max)
        //return IntegerRange(Collections.min(iaMult), Collections.max(iaMult))
        return IntegerRange(iaMult.min(), iaMult.max())
    }

    /** Exponentiation */
    override fun exp(): IntegerRange =
        IntegerRange(floor(exp(min.toDouble())).toLong(), ceil(exp(max.toDouble())).toLong())

    /**
     *  Natural Logarithm (with base e (ln))
     */
    override fun log(): NumberRange<Long> =
        IntegerRange(floor(log(min.toDouble(), E)).toLong(), ceil(log(max.toDouble(), E)).toLong())

    /**
     *  Logarithm with a given base
     *  @param other Base for logarithm
     */
    override fun log(other: NumberRange<Long>): NumberRange<Long> =
        IntegerRange(floor(log(asReal(min), asReal(other.min))).toLong(), ceil(log(asReal(max), asReal(other.max))).toLong())

    /**
     * Calculates pow for IntegerRange. That is, it is used for the following function: f(x,y) = x^y
     * It computes 'this' raised to the exponent.
     * @param other = the exponential power that the base is being raised to.
     */
    override fun pow(other: Long): IntegerRange {
        val lb = asReal(min).pow(other.toDouble()).roundToLong()
        val ub = asReal(max).pow(other.toDouble()).roundToLong()
        return IntegerRange(lb, ub)
    }

    /**
     * Calculates pow for IntegerRange
     * That is, it is used for the following function: f(x,y) = x^y
     * It computes 'this' raised to the exponent.
     * @param other = the exponential power that the base is being raised to.
     */
    override fun pow(other: NumberRange<Long>): IntegerRange {
        val lb: Long
        val ub: Long
        if (other.min >= 0) {
            lb = asReal(min).pow(asReal(other.min)).roundToLong()
            ub = asReal(max).pow(asReal(other.max)).roundToLong()
        } else if (other.max < 0) {
            lb = asReal(min).pow(asReal(other.min)).roundToLong()
            ub = asReal(max).pow(asReal(other.max)).roundToLong()
        } else { // other.min<0 and other.max>=0
            lb = asReal(min).pow(asReal(other.min)).roundToLong()
            ub = asReal(max).pow(asReal(other.max)).roundToLong()
        }
        return IntegerRange(lb, ub)
    }

    /** Computes 2 to the power of this. */
    fun power2(): IntegerRange =
        IntegerRange(2.0.pow(asReal(min)).toLong(), 2.0.pow(asReal(max)).toLong())

    /**
     * Computes the square of an IntegerRange
     */
    override fun sqr(): IntegerRange {
        val result = this * this
        if(min<0 && max>0) // if value is positive and negative, 0 is the smallest solution
            result.min = 0
        return result
    }
    /**
     * Computes the square root of an IntegerRange
     */
    override fun sqrt(): IntegerRange {
        val input = this
        val retval = IntegerRange(0, 0)
        retval.min = floor(sqrt(asReal(input.min))).toLong()
        retval.max = ceil(sqrt(asReal(input.max))).toLong()
        return retval
    }

    /**
     * Calculate nth root of an integer (nth root of x = x^(1/n))
     */
    override fun root(other: NumberRange<Long>): IntegerRange {
        val lb: Long
        val ub: Long
        if (other.min >= 0) {
            lb = floor(asReal(min).pow(1 / asReal(other.max))).toLong()
            ub = ceil(asReal(max).pow(1 / asReal(other.min))).toLong()
        } else if (other.max < 0) {
            lb = floor(asReal(min).pow(1 / asReal(other.min))).toLong()
            ub = ceil(asReal(max).pow(1 / asReal(other.max))).toLong()
        } else { // other.min<0 and other.max>=0
            lb = floor(asReal(min).pow(1 / asReal(other.min))).toLong()
            ub = ceil(asReal(max).pow(1 / asReal(other.min))).toLong()
        }
        return IntegerRange(lb, ub)
    }


    fun sum_i(i: Int = 0, N: Int, list: MutableList<IntegerRange>): IntegerRange {
        var sum = IntegerRange(0, 0)

        try {
            for (index in i..N)
                sum = sum.plus(list[index]) //uses OverflowDetection
        } catch (ioobe: IndexOutOfBoundsException) {
            println("i = $i")
            println("N = $N")
            println("message = " + ioobe.message)
            println("cause = " + ioobe.cause)
            ioobe.printStackTrace()
        }

        return sum
    }

    fun sum_i(N: Int, list: MutableList<IntegerRange>): IntegerRange = sum_i(0, N, list)

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this::class != other::class) return false
        if (this === other) return true
        val rr = other as IntegerRange
        return rr.min == min && rr.max == max
    }

    override fun greaterThan(other: NumberRange<Long>): XBool {
        if (this === other) return XBool.True
        var retval = XBool.False
        val rr = other as IntegerRange
        if (min > rr.max) retval = XBool.True
        if (rr.min == min && rr.max == max || max < rr.min)
            return XBool.False
        // Next 4 Comparisons could be combined into 1 and possibly simplified.  Investigate!
        // L is subset of R
        if (min > other.min && max < other.max || min == other.min && max < other.max || min > other.min && max == other.max)
            retval = XBool.NaB
        // R is subset of L
        if (min < other.min && max > other.max || min == other.min && max > other.max || min > other.min && max == other.max)
            retval = XBool.NaB
        // No subset, just overlapping
        if (min > other.min && min < other.max && max > other.max)
            retval = XBool.NaB
        if (other.min in (min + 1) until max && max < other.max)
            retval = XBool.NaB
        // Does it also need to be done with respect to the r.min & r.max being contained within L operand range?
        // Is there redundancy here? Investigate!
        if (other.min in (min + 1) until max && other.max > max)
            retval = XBool.NaB
        if (other.min < min && other.max > min && other.max < max)
            retval = XBool.NaB

        return retval
    }

    override fun greaterThanOrEquals(other: NumberRange<Long>): XBool {
        if (this === other) return XBool.True
        var retval = XBool.False
        val rr = other as IntegerRange
        if (min >= rr.min)
            retval = XBool.True

        return retval
    }

    override fun lessThan(other: NumberRange<Long>): XBool {
        if (this === other) return XBool.False
        var retval = XBool.False
        val rr = other as IntegerRange
        if (max < rr.min) retval = XBool.True
        if (rr.min == min && rr.max == max || min > rr.max)
            return XBool.False
        // Next 4 Comparisons could be combined into 1 and possibly simplified.  Investigate!
        // L is subset of R
        if (min > other.min && max < other.max || min == other.min && max < other.max || min > other.min && max == other.max)
            retval = XBool.NaB
        // R is subset of L
        if (min < other.min && max > other.max || min == other.min && max > other.max || min > other.min && max == other.max)
            retval = XBool.NaB
        // No subset, just overlapping
        if (min > other.min && min < other.max && max > other.max)
            retval = XBool.NaB
        if (other.min in (min + 1) until max && max < other.max)
            retval = XBool.NaB
        // Does it also need to be done with respect to the r.min & r.max being contained within L operand range?
        // Is there redundancy here? Investigate!
        if (other.min in (min + 1) until max && other.max > max)
            retval = XBool.NaB
        if (other.min < min && other.max > min && other.max < max)
            retval = XBool.NaB

        return retval
    }

    override fun lessThanOrEquals(other: NumberRange<Long>): XBool {
        if (this::class != other::class) return XBool.False
        if (this === other) return XBool.True
        var retval = XBool.False
        val rr = other as IntegerRange
        if (max <= rr.max)
            retval = XBool.True

        return retval
    }

    fun isProperSubsetof(b: IntegerRange): Boolean =
        min > b.min && max < b.max || min > b.min && max <= b.max || min >= b.min && max < b.max

    override fun hashCode(): Int =
        super.hashCode()

    override fun toString(): String {
        var output = ""
        if (isEmpty()) return "∅"

        output += if (min == Long.MIN_VALUE) "[MIN .. " else "[$min .. "
        output += if (max == Long.MAX_VALUE) " MAX]" else "$max]"

        return output
    }

    /** Overloaded contains operation for allowing "AADD in range" notation */
    /*
    operator fun ClosedRange<Long>.contains(r: IntegerRange): Boolean =
        if (r is IDDLeaf) {
            if (r.max < endInclusive )  true
            else if (r.min > start)  false
            else true
        } else
            r.T!!.contains(this) || r.F!!.contains(this)
     */
}


/**  Extension Functions developed by Jack **/
fun sqr(input: IntegerRange): IntegerRange = input.sqr()

fun sqrt(input: IntegerRange): IntegerRange = input.sqrt()
fun root(input: IntegerRange, n: IntegerRange): IntegerRange = input.root(n)

/**
 * Calculates pow for IntegerRange
 * That is, it is used for the following function: f(x,y) = x^y
 * @param base : IntegerRange the base, or number, which is being raised to an exponent, i.e. multiplied by itself that number of times
 * @param exp : Long = the exponential power that the base is being raised to.
 */
fun pow(base: IntegerRange, exp: Long): IntegerRange = base.pow(exp)

/**
 * Calculates pow for IntegerRange
 * That is, it is used for the following function: f(x,y) = x^y
 * @param base : IntegerRange the base, or number, which is being raised to an exponent, i.e. multiplied by itself that number of times
 * @param exp : IntegerRange = the exponential power that the base is being raised to.
 */
fun pow(base: IntegerRange, exp: IntegerRange): IntegerRange = base.pow(exp)
