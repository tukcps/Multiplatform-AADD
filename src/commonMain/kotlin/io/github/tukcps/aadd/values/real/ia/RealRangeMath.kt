package io.github.tukcps.aadd.values.real.ia

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBound.*
import io.github.tukcps.aadd.values.real.DoubleBoundMath
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.rounding.FMA
import io.github.tukcps.aadd.values.real.rounding.Rounding
import io.github.tukcps.aadd.values.real.unaryMinus
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.nextDown
import kotlin.math.nextUp
import kotlin.math.pow
import kotlin.ranges.rangeTo


fun negate(range: RealRange): RealRange =
    RealRange( (-range.max) ?: NegativeInfinity, (-range.min) ?: PositiveInfinity)

/**
 * Returns the sum of two real ranges.
 *
 * The operation follows set semantics and returns the smallest convex range
 * containing all valid sums.
 */
fun add(a: RealRange, b: RealRange): RealRange {
    if (a.isEmpty() || b.isEmpty()) return RealRange.Empty
    val min = DoubleBoundMath.add(a.min, b.min, Rounding.DOWN)
        ?: return RealRange.Reals
    val max = DoubleBoundMath.add(a.max, b.max, Rounding.UP)
        ?: return RealRange.Reals
    return RealRange(min, max)
}

/**
 * Subtracts two real ranges.
 */
fun subtract(a: RealRange, b: RealRange): RealRange = when {
        a.isEmpty() || b.isEmpty() -> RealRange.Empty
        else -> {
            val min = DoubleBoundMath.subtract(a.min, b.max, Rounding.DOWN)
            val max = DoubleBoundMath.subtract(a.max, b.min, Rounding.UP)
            if (min == null || max == null) RealRange.Reals
            else RealRange(min, max)
        }
    }

/**
 * @return value * alpha + delta
 */
fun affine(value: RealRange, alpha: Double, delta: Double): RealRange =
    value * alpha + delta

/**
 * Multiplies two real ranges.
 */
fun multiply(a: RealRange, b: RealRange): RealRange {
    when {
        (a.isEmpty() || b.isEmpty()) -> return RealRange.Empty
        a.isReals() || b.isEmpty() -> return RealRange.Reals
    }

    val lower = listOfNotNull(
        DoubleBoundMath.multiply(a.min, b.min, Rounding.DOWN),
        DoubleBoundMath.multiply(a.min, b.max, Rounding.DOWN),
        DoubleBoundMath.multiply(a.max, b.min, Rounding.DOWN),
        DoubleBoundMath.multiply(a.max, b.max, Rounding.DOWN)
    )

    val upper = listOfNotNull(
        DoubleBoundMath.multiply(a.min, b.min, Rounding.UP),
        DoubleBoundMath.multiply(a.min, b.max, Rounding.UP),
        DoubleBoundMath.multiply(a.max, b.min, Rounding.UP),
        DoubleBoundMath.multiply(a.max, b.max, Rounding.UP)
    )

    if (lower.isEmpty() || upper.isEmpty())
        return RealRange.Empty

    return RealRange(DoubleBoundMath.min(*lower.toTypedArray()), DoubleBoundMath.max(*upper.toTypedArray()))
}

/**
 * Affine transformation: a*alpha + delta, expanded by optional noise from Affine Form
 */
fun affine(a: RealRange, alpha: Double, delta: Double, noise: Double = 0.0): RealRange =
    (a * alpha) + delta + RealRange(-noise, noise)

/**
 * Divides two real ranges.
 */
fun divide(a: RealRange, b: RealRange): RealRange =
    multiply(a, inv(b))

fun RealRange.sqr(): RealRange =
    when {
        isEmpty() -> RealRange.Empty

        min >= Finite(0.0) ->
            RealRange(
                DoubleBoundMath.multiply(min, min, Rounding.DOWN)!!,
                DoubleBoundMath.multiply(max, max, Rounding.UP)!!
            )

        max <= Finite(0.0) ->
            RealRange(
                DoubleBoundMath.multiply(max, max, Rounding.DOWN)!!,
                DoubleBoundMath.multiply(min, min, Rounding.UP)!!
            )

        else ->
            RealRange(
                Finite(0.0),
                DoubleBoundMath.max(
                    DoubleBoundMath.multiply(min, min, Rounding.UP)!!,
                    DoubleBoundMath.multiply(max, max, Rounding.UP)!!
                )
            )
    }

/**
 * Calculates the square root of this range.
 */
fun sqrt( x: RealRange): RealRange =
    when {
        x.isEmpty() -> RealRange.Empty
        x.max < Finite(0.0) -> RealRange.Empty

        else -> RealRange(
            DoubleBoundMath.sqrt(
                DoubleBoundMath.max(x.min, Finite(0.0)),
                Rounding.DOWN
            ),
            DoubleBoundMath.sqrt(x.max, Rounding.UP)
        )
    }

/**
 * Raises all values of a real range to the given exponent.
 */
fun pow(x: RealRange, exponent: Double): RealRange {
    if (x.isEmpty()) return RealRange.Empty

    // Trivial cases.
    if (exponent == 0.0)
        return RealRange(1.0..1.0)

    if (exponent == 1.0)
        return x

    // Negative exponents are undefined at zero.
    if (exponent < 0.0 && 0.0 in x)
        return RealRange.Reals

    val integerExponent = exponent.isFinite() && exponent == floor(exponent)

    // Noninteger exponents require a non-negative domain
    val domain =
        if (!integerExponent)
            x.intersect(RealRange(Finite(0.0), PositiveInfinity))
        else
            x

    //--------------------------------------------------------------------------
    // Odd integer exponents are monotone.
    //--------------------------------------------------------------------------
    if (integerExponent && exponent.toLong() and 1L == 1L) {
        return RealRange(
            DoubleBoundMath.pow(domain.min, exponent, Rounding.DOWN) ?: NegativeInfinity,
            DoubleBoundMath.pow(domain.max, exponent, Rounding.UP) ?: PositiveInfinity
        )
    }

    // Even integer exponents.
    if (integerExponent) {
        if (domain.max.toDouble() <= 0.0) {
            return RealRange(
                DoubleBoundMath.pow(domain.max, exponent, Rounding.DOWN) ?: Finite(0.0),
                DoubleBoundMath.pow(domain.min, exponent, Rounding.UP) ?: PositiveInfinity
            )
        }

        if (domain.min.toDouble() >= 0.0) {
            return RealRange(
                DoubleBoundMath.pow(domain.min, exponent, Rounding.DOWN) ?: Finite(0.0),
                DoubleBoundMath.pow(domain.max, exponent, Rounding.UP) ?: PositiveInfinity
            )
        }

        val upper = max(
            DoubleBoundMath.pow((-domain.min)?: NegativeInfinity, exponent, Rounding.UP)?.finiteValue ?: Double.POSITIVE_INFINITY,
            DoubleBoundMath.pow(domain.max, exponent, Rounding.UP)?.finiteValue ?: Double.POSITIVE_INFINITY
        )
        return RealRange(0.0..upper)
    }

    //--------------------------------------------------------------------------
    // Non-integer exponents.
    //--------------------------------------------------------------------------
    return if (exponent > 0.0) {
        RealRange(
            DoubleBoundMath.pow(domain.min, exponent, Rounding.DOWN) ?: Finite(0.0),
            DoubleBoundMath.pow(domain.max, exponent, Rounding.UP) ?: PositiveInfinity
        )
    } else {
        RealRange(
            DoubleBoundMath.pow(domain.max, exponent, Rounding.DOWN) ?: Finite(0.0),
            DoubleBoundMath.pow(domain.min, exponent, Rounding.UP) ?: PositiveInfinity
        )
    }
}

/**
 * Computes x^y for interval arguments.
 * The base is restricted to the mathematical domain x ≥ 0.
 */
fun pow(base: RealRange, exponent: RealRange): RealRange {
    val x = base.intersect(RealRange(Finite(0.0), PositiveInfinity))

    when {
        base.isEmpty() || exponent.isEmpty() -> return RealRange.Empty
        exponent.isZero() -> return RealRange.One
        base.isOne() -> return RealRange.One
        exponent.isOne() -> return base
        x.isEmpty() -> return RealRange.Empty
        x.min.toDouble() == 0.0 && exponent.min.toDouble() < 0.0 -> return RealRange.Reals
    }

    val values = buildList(4) {
        add(x.min.finiteValue.pow(exponent.min.finiteValue))
        add(x.min.finiteValue.pow(exponent.max.finiteValue))
        add(x.max.finiteValue.pow(exponent.min.finiteValue))
        add(x.max.finiteValue.pow(exponent.max.finiteValue))
    }.filter(Double::isFinite)

    return if (values.isEmpty()) RealRange.Reals
    else RealRange(values.min().nextDown(), values.max().nextUp())
}

/**
 * Calculates the exponential function.
 */
fun exp(x: RealRange): RealRange =
    when {
        x.isEmpty() -> RealRange.Empty
        else -> RealRange(
            DoubleBoundMath.exp(x.min, Rounding.DOWN) ?: Finite(0.0),
            DoubleBoundMath.exp(x.max, Rounding.UP) ?: PositiveInfinity
        )
    }

/**
 * Calculates the natural logarithm.
 *
 * Values <= 0 are removed from the domain.
 */
fun ln(x: RealRange): RealRange =
    when {
        x.isEmpty() -> RealRange.Empty
        x.max <  Finite(0.0) -> RealRange.Empty
        x.max == Finite(0.0) -> RealRange(NegativeInfinity, NegativeInfinity)
        x.min <= Finite(0.0) -> RealRange(NegativeInfinity, DoubleBoundMath.ln(x.max, Rounding.UP))
        else -> RealRange(
            DoubleBoundMath.ln(x.min, Rounding.DOWN),
            DoubleBoundMath.ln(x.max, Rounding.UP)
        )
    }

/**
 * Computes the logarithm to the given base.
 */
fun log(value: RealRange, base: Double): RealRange =
    ln(value) * (1/ln(base))

/**
 * Calculates the reciprocal.
 *
 * The result follows constraint propagation semantics:
 *
 *     inv(S) = { 1/x | x ∈ S, x != 0 }
 */
fun inv(value: RealRange): RealRange = when {
        value.isEmpty() -> RealRange.Empty
        value.min == Finite(0.0) -> RealRange(
            DoubleBoundMath.divide(Finite(1.0), value.max, Rounding.DOWN)!!,
            PositiveInfinity
        )
        value.max == Finite(0.0) -> RealRange(
            NegativeInfinity,
            DoubleBoundMath.divide(Finite(1.0), value.min, Rounding.UP)!!
        )
        Finite(0.0) in value -> RealRange.Reals

        else -> {
            val low1 = DoubleBoundMath.divide(Finite(1.0), value.min, Rounding.DOWN)
            val low2 = DoubleBoundMath.divide(Finite(1.0), value.max, Rounding.DOWN)
            val high1 = DoubleBoundMath.divide(Finite(1.0), value.min, Rounding.UP)
            val high2 = DoubleBoundMath.divide(Finite(1.0), value.max, Rounding.UP)

            RealRange(
                DoubleBoundMath.min(low1!!, low2!!),
                DoubleBoundMath.max(high1!!, high2!!)
            )
        }
    }

/**
 * Calculates the absolute value.
 */
fun abs(x: RealRange): RealRange =
    when {
        x.isEmpty() -> RealRange.Empty
        x.min >= Finite(0.0) -> x
        x.max <= Finite(0.0) -> -x
        else -> RealRange(
            Finite(0.0),
            DoubleBoundMath.max(DoubleBoundMath.negate(x.min), x.max)
        )
    }

fun RealRange.relu(): RealRange =
    when {
        isEmpty() -> RealRange.Empty
        max <= Finite(0.0) -> RealRange.Zero
        min >= Finite(0.0) -> this
        else -> RealRange(Finite(0.0), max)
    }

fun floor(a: RealRange): RealRange = RealRange(floor(a.min.toDouble()), floor(a.max.toDouble()))
fun ceil(a: RealRange): RealRange = RealRange(ceil(a.min.toDouble()), ceil(a.max.toDouble()))

/**
 * Arc tangent for real ranges.
 */
fun atan(x: RealRange): RealRange = when {
    x.isEmpty()   -> RealRange.Empty
    x.isReals()   -> RealRange(-PI / 2.0..PI / 2.0)

    else -> RealRange(
        math.atan(x.min.toDouble(), Rounding.DOWN),
        math.atan(x.max.toDouble(), Rounding.UP)
    )
}

fun asin(x: RealRange): RealRange {
    val y = x.intersect(RealRange(-1.0..1.0))
    if (y.isEmpty()) return RealRange.Empty

    return RealRange(
        math.asin(y.min.toDouble(), Rounding.DOWN),
        math.asin(y.max.toDouble(), Rounding.UP)
    )
}

/**
 * Arc cosine for real ranges.
 */
fun acos(x: RealRange): RealRange {
    val y = x intersect RealRange(-1.0..1.0)
    if (y.isEmpty()) return RealRange.Empty

    return RealRange(
        math.acos(y.max.toDouble(), Rounding.DOWN),
        math.acos(y.min.toDouble(), Rounding.UP)
    )
}

/**
 * Returns whether the interval contains a point
 *
 *     offset + k * period
 *
 * for some integer k.
 */
/**
 * Returns whether this interval contains a point
 *
 *     offset + k · period
 *
 * for some integer k.
 */
internal fun RealRange.containsPeriodic(
    offset: Double,
    period: Double
): Boolean {
    if (isEmpty()) return false

    val k = ceil((min.toDouble() - offset) / period).toLong()
    val x = FMA.compute(k.toDouble(), period, offset)

    return x <= max.toDouble()
}

/**
 * Sine for real ranges.
 */
/**
 * Sine for real ranges.
 */
fun sin(x: RealRange): RealRange {
    if (x.isEmpty()) return RealRange.Empty
    if (x.isReals()) return RealRange(-1.0..1.0)

    val width = math.sub(x.max.toDouble(), x.min.toDouble(), Rounding.UP)
    if (width >= 2.0 * PI)
        return RealRange(-1.0..1.0)

    val a = x.min.toDouble()
    val b = x.max.toDouble()

    var lo = min(
        math.sin(a, Rounding.DOWN),
        math.sin(b, Rounding.DOWN)
    )

    var hi = max(
        math.sin(a, Rounding.UP),
        math.sin(b, Rounding.UP)
    )

    // Exact zeros at k·π.
    if (x.containsPeriodic(0.0, PI)) {
        lo = min(lo, 0.0)
        hi = max(hi, 0.0)
    }

    // Maxima at π/2 + 2kπ.
    if (x.containsPeriodic(PI / 2.0, 2.0 * PI))
        hi = 1.0

    // Minima at 3π/2 + 2kπ.
    if (x.containsPeriodic(3.0 * PI / 2.0, 2.0 * PI))
        lo = -1.0

    return RealRange(lo..hi)
}

/**
 * Cosine for real ranges.
 */
fun cos(x: RealRange): RealRange {
    if (x.isEmpty()) return RealRange.Empty
    if (x.isReals()) return RealRange(-1.0..1.0)

    val width = math.sub(x.max.toDouble(), x.min.toDouble(), Rounding.UP)
    if (width >= 2.0 * PI)
        return RealRange(-1.0..1.0)

    val a = x.min.toDouble()
    val b = x.max.toDouble()

    var lo = min(
        math.cos(a, Rounding.DOWN),
        math.cos(b, Rounding.DOWN)
    )

    var hi = max(
        math.cos(a, Rounding.UP),
        math.cos(b, Rounding.UP)
    )

    // Exact zeros at π/2 + kπ.
    if (x.containsPeriodic(PI / 2.0, PI)) {
        lo = min(lo, 0.0)
        hi = max(hi, 0.0)
    }

    // Maxima at 2kπ.
    if (x.containsPeriodic(0.0, 2.0 * PI))
        hi = 1.0

    // Minima at π + 2kπ.
    if (x.containsPeriodic(PI, 2.0 * PI))
        lo = -1.0

    return RealRange(lo..hi)
}

/**
 * Tangent for real ranges.
 */
fun tan(x: RealRange): RealRange {
    if (x.isEmpty()) return RealRange.Empty
    if (x.isReals()) return RealRange.Reals

    // Poles at π/2 + kπ.
    if (x.containsPeriodic(PI / 2.0, PI))
        return RealRange.Reals

    return RealRange(
        math.tan(x.min.toDouble(), Rounding.DOWN),
        math.tan(x.max.toDouble(), Rounding.UP)
    )
}