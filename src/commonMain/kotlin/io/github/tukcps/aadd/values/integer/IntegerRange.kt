package io.github.tukcps.aadd.values.integer

import io.github.tukcps.aadd.util.parseUSLongString
import io.github.tukcps.aadd.values.BoundKind
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.XBool
import kotlin.math.*


class IntegerRange private constructor(
    override val min: Long,
    override val max: Long,
    override val minKind: BoundKind,
    override val maxKind: BoundKind
) : NumberRange<Long> { // end class IntegerRange

    private constructor(min: LongBound, max: LongBound) : this(min.value, max.value, min.kind, max.kind)
    constructor(min: Bound, max: Bound) : this(min.toLongBound(), max.toLongBound())
    constructor(): this(Long.MIN_VALUE, Long.MAX_VALUE, BoundKind.NEGATIVE_INFINITY, BoundKind.POSITIVE_INFINITY)
    constructor(c: Long) : this(c, c, BoundKind.FINITE, BoundKind.FINITE)
    constructor(r: ClosedRange<Long>): this(r.start, r.endInclusive, BoundKind.FINITE, BoundKind.FINITE)
    constructor(min: Long, max: Long): this(min, max, BoundKind.FINITE, BoundKind.FINITE)
    constructor(init : Double) : this (floor(init).roundToLong(), ceil(init).roundToLong())
    constructor(initL : Double, initH : Double) : this (floor(initL).roundToLong(), ceil(initH).roundToLong())

    init {
        require(minKind.isValid(min))
        require(maxKind.isValid(max))
    }

    val minBound: Bound get() = min.bound(minKind)
    val maxBound: Bound get() = max.bound(maxKind)

    val isFinite: Boolean
        get() = minKind == BoundKind.FINITE &&
                maxKind == BoundKind.FINITE

    val isPoint: Boolean
        get() = isFinite && min == max

    override fun isZero(): Boolean = min == 0L && max == 0L
    override fun isOne(): Boolean = min == 1L && max == 1L
    fun isPositive(): Boolean = minBound > 0L.bound()
    fun isNegative(): Boolean = maxBound < 0L.bound()
    fun isIntegers(): Boolean = (minKind == BoundKind.NEGATIVE_INFINITY) && (maxKind == BoundKind.POSITIVE_INFINITY)

    /**
     * Cloning
     */
    fun clone(): IntegerRange = IntegerRange(this.min, this.max, this.minKind, this.maxKind)

    /**
     * intersection of two IntegerRanges
     * @param other : IntegerRange
     * @return IntegerRange
     */
    override infix fun intersect(other: NumberRange<Long>): IntegerRange =
        IntegerRange(
            max(min, other.min), min(max, other.max),
            // minIsInf && other.minIsInf, maxIsInf && other.maxIsInf
        )

    /**
     * Returns the smallest range containing this range and [other]
     * (the convex hull / join).
     */
    override infix fun join(other: NumberRange<Long>) = IntegerRange(
        min = min(min, other.min),
        max = max(max, other.max),
       // minIsInf = minIsInf || other.minIsInf,
       // maxIsInf = maxIsInf || other.maxIsInf
    )

    override infix fun union(other: NumberRange<Long>) : IntegerRange = join(other)


    /**
     * Converts an integer bound to its floating-point representation.
     *
     * Integer infinities are mapped to IEEE-754 infinities, all finite values are
     * converted using [Long.toDouble].
     */
    fun asReal(value: Long): Double = when (value) {
        Integers.max -> Double.POSITIVE_INFINITY
        Integers.min -> Double.NEGATIVE_INFINITY
        else -> value.toDouble()
    }

    override operator fun plus(other: Long): NumberRange<Long> =
        IntegerRange(minBound + other.bound(), maxBound + other.bound())

    operator fun plus(other: Int): NumberRange<Long> =
        IntegerRange(minBound + other.bound(), maxBound + other.bound())

    override operator fun plus(other: NumberRange<Long>): IntegerRange =
        IntegerRange(minBound + other.min.bound(other.minKind), maxBound + other.max.bound(other.maxKind))

    override operator fun minus(other: NumberRange<Long>): IntegerRange =
        IntegerRange(minBound - other.max.bound(other.maxKind), maxBound - other.min.bound(other.minKind))

    operator fun minus(other: Int): NumberRange<Long> =
        IntegerRange(minBound - other.bound(), maxBound - other.bound())

    override operator fun minus(other: Long): NumberRange<Long> =
        IntegerRange(minBound - other.bound(), maxBound - other.bound())

    /**
     * Test if a value is infinite (either minimal or maximal Long value)
     */
    private fun isInfinite(value: Long): Boolean {
        return value == Integers.max || value == Integers.min || value == -Integers.min || value == -Integers.max
    }

    override operator fun times(other: NumberRange<Long>): IntegerRange {
        val p1 = minBound * other.min.bound(other.minKind)
        val p2 = minBound * other.max.bound(other.maxKind)
        val p3 = maxBound * other.min.bound(other.minKind)
        val p4 = maxBound * other.max.bound(other.maxKind)

        return IntegerRange(minOf(p1, p2, p3, p4), maxOf(p1, p2, p3, p4))
    }

    override operator fun times(other: Long): IntegerRange {
        val factor = other.bound()
        val p1 = minBound * factor
        val p2 = maxBound * factor
        return IntegerRange(minOf(p1, p2), maxOf(p1, p2))
    }

    override operator fun div(other: NumberRange<Long>): IntegerRange {
        val divisor = IntegerRange(other)

        // Division by exactly {0}
        if (divisor.isZero()) {
            return when {
                isPositive() ->
                    IntegerRange(
                        Bound.PositiveInfinity,
                        Bound.PositiveInfinity
                    )

                isNegative() ->
                    IntegerRange(
                        Bound.NegativeInfinity,
                        Bound.NegativeInfinity
                    )

                else ->
                    IntegerRange(
                        Bound.NegativeInfinity,
                        Bound.PositiveInfinity
                    )
            }
        }

        // TODO Split divisor at zero for better precision.
        // For now, conservatively return the universal interval.
        if (divisor.contains(0L)) {
            return IntegerRange(
                Bound.NegativeInfinity,
                Bound.PositiveInfinity
            )
        }

        val q1 = minBound / divisor.minBound
        val q2 = minBound / divisor.maxBound
        val q3 = maxBound / divisor.minBound
        val q4 = maxBound / divisor.maxBound

        return IntegerRange(minOf(q1, q2, q3, q4), maxOf(q1, q2, q3, q4))
    }

    operator fun div(other: Int): IntegerRange {
        if (other == 0) {
            return when {
                isPositive() -> IntegerRange(Bound.PositiveInfinity, Bound.PositiveInfinity)
                isNegative() -> IntegerRange(Bound.NegativeInfinity, Bound.NegativeInfinity)
                else -> IntegerRange(Bound.NegativeInfinity, Bound.PositiveInfinity)
            }
        }
        val divisor = other.bound()
        return IntegerRange(minBound / divisor, maxBound / divisor)
    }

    override operator fun div(other: Long): IntegerRange {
        if (other == 0L) {
            return when {
                isPositive() -> IntegerRange(Bound.PositiveInfinity, Bound.PositiveInfinity)
                isNegative() -> IntegerRange(Bound.NegativeInfinity, Bound.NegativeInfinity)
                else -> IntegerRange(Bound.NegativeInfinity, Bound.PositiveInfinity)
            }
        }

        val divisor = other.bound()
        return IntegerRange(minBound / divisor, maxBound / divisor)
    }

    override fun greaterThan(other: Long): XBool =
        if (other < min) XBool.True else if (other >= max) XBool.False else XBool.X

    override fun greaterThanOrEquals(other: Long): XBool =
        if (other <= min) XBool.True else if (other > max) XBool.False else XBool.X

    override fun lessThan(other: Long): XBool =
        if (other > max) XBool.True else if (other <= min) XBool.False else XBool.X

    override fun lessThanOrEquals(other: Long): XBool =
        if (other >= max) XBool.True else if (other < min) XBool.False else XBool.X

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
        val product = this * this
        if (min<0 && max >0)
            return IntegerRange(0.bound(), product.maxBound)
        else
            return product
    }

    /**
     * Computes the square root of an IntegerRange
     */
    override fun sqrt(): IntegerRange {
        val input = this
        val min = floor(sqrt(asReal(input.min))).toLong()
        val max = ceil(sqrt(asReal(input.max))).toLong()
        return IntegerRange(min, max)
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
    override fun toString(): String {
        val lower = when (minKind) {
            BoundKind.FINITE -> min.toString()
            BoundKind.NEGATIVE_INFINITY -> "-*"
            BoundKind.POSITIVE_INFINITY -> "+*"
        }

        val upper = when (maxKind) {
            BoundKind.FINITE -> max.toString()
            BoundKind.NEGATIVE_INFINITY -> "-*"
            BoundKind.POSITIVE_INFINITY -> "+*"
        }

        // point interval
        if (minKind == maxKind &&
            min == max &&
            minKind == BoundKind.FINITE)
            return lower

        // complete range
        if (minKind == BoundKind.NEGATIVE_INFINITY &&
            maxKind == BoundKind.POSITIVE_INFINITY)
            return "*"

        return "$lower..$upper"
    }

    companion object {

        val Empty get() = IntegerRange(Long.MAX_VALUE, Long.MIN_VALUE)
        val Integers get() = IntegerRange(Long.MIN_VALUE, Long.MAX_VALUE)

        fun parse(text: String): IntegerRange {
            val s = text
                .trim()
                .removePrefix("[")
                .removeSuffix("]")

            if (s.isEmpty() || s == "*")
                return IntegerRange()

            val parts = s.split("..", limit = 2)

            if (parts.size == 1) {
                val (value, kind) = parseBound(parts[0].trim(), lower = true)
                return IntegerRange(value, value, kind, kind)
            }

            val (min, minKind) = parseBound(parts[0].trim(), lower = true)
            val (max, maxKind) = parseBound(parts[1].trim(), lower = false)

            return IntegerRange(min, max, minKind, maxKind)
        }

        private fun parseBound(
            text: String,
            lower: Boolean
        ): Pair<Long, BoundKind> =
            when (text.trim()) {

                "*" ->
                    if (lower)
                        Long.MIN_VALUE to BoundKind.NEGATIVE_INFINITY
                    else
                        Long.MAX_VALUE to BoundKind.POSITIVE_INFINITY

                "-*" ->
                    Long.MIN_VALUE to BoundKind.NEGATIVE_INFINITY

                "+*" ->
                    Long.MAX_VALUE to BoundKind.POSITIVE_INFINITY

                else ->
                    parseUSLongString(text) to BoundKind.FINITE
            }


        internal fun fromBounds(min: Bound, max: Bound): IntegerRange {
            require(min <= max) { "Invalid interval: $min > $max" }

            val (minValue, minKind) = min.toLongBound()
            val (maxValue, maxKind) = max.toLongBound()

            return IntegerRange(minValue, maxValue, minKind, maxKind)
        }
    }

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
