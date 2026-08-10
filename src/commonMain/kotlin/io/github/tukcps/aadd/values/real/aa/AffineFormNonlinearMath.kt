package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.abs
import io.github.tukcps.aadd.values.real.DoubleBoundMath.max
import io.github.tukcps.aadd.values.real.DoubleBoundMath.min
import io.github.tukcps.aadd.values.real.DoubleBoundMath.negate
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.create
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.range
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.scalar
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.multiply
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath.midpoint
import io.github.tukcps.aadd.values.real.rounding.Rounding
import io.github.tukcps.aadd.values.real.times
import io.github.tukcps.aadd.values.real.toDoubleBound
import kotlin.math.*
import io.github.tukcps.aadd.values.real.ia.inv as invRange

//
// ------------------ Non-linear functions that do not (yet?) use central approximation methods -----------------
//

/**
 * Absolute value define with abs(x)=if(x<0) return -x
 * Taking the widest subinterval if 0.0 is contained and restraining the min max values
 */
fun abs(value: AffineForm): AffineForm {
    // in that cause the interval can only be reduced and the longest overlapping of correlation be used
    if(value.contains(0.0)){
        val highValue= max(value.max, negate(value.min))
        val negated = negate(value)
        return if (value.central < 0.0)
            create(value.builder, 0.0.toDoubleBound()!!, highValue, negated.central, 0.0, negated.xi)
        else
            create(value.builder, 0.0.toDoubleBound()!!, highValue, value.central, 0.0, value.xi)
    } else {
        return if (value.min.toDouble() > 0.0) value
        else negate(value)
    }
}

/**
 * Multiplies an affine form by a given scalar.
 * FP roundoff is considered as 1 ulp in each result.
 */
fun multiply(a: AffineForm, b: Double): AffineForm {
    when {
        a.isEmpty()      -> return a.builder.AF.Empty
        b.isNaN()        -> return a.builder.AF.Empty
        a.isReals()      -> return a.builder.AF.All
        a.isZero()       -> return scalar(a.builder, 0.0)
        a.isOne()        -> return scalar(a.builder, b)
        b == 0.0         -> return scalar(a.builder, 0.0)
        b == 1.0         -> return a
    }
    val newXi = HashMap<Long, Double>(a.builder.settings.affineFormHashMapSize)
    a.xi.keys.forEach { newXi[it] = math.mul(a.xi[it]!!, b, Rounding.AWAY) }
    val newCentralRounded = math.mulRounded(a.central, b)
    return create(a.builder,
        multiply(a as RealRange, RealRange(b)) ,
        newCentralRounded.value,
        newCentralRounded.error,
        newXi)
}

/**
 * Multiplication. Uses the simpler approximation proposed by Stolfi et al.
 * instead of the more precise but costlier version. Computes interval product
 * as well and keeps the intersection of the results, minimizing error propagation.
 */
fun multiply(a: AffineForm, b: AffineForm): AffineForm {
    check(a.builder == b.builder)
    when {
        a.isEmpty() || b.isEmpty()  -> return a.builder.AF.Empty
        a.isZero() && b.isFinite()  -> return scalar(a.builder, 0.0)
        b.isZero() && a.isFinite()  -> return scalar(a.builder, 0.0)
        a.isOne()                   -> return b
        b.isOne()                   -> return a
        a.isReals() || b.isReals()  -> return a.builder.AF.All
        a.min.isInfinite || a.max.isInfinite|| b.min.isInfinite || b.max.isInfinite
            -> return range(a.builder, multiply(a as RealRange, b as RealRange))
    }
    if (!a.isFinite() || !b.isFinite())  // Open range? --> IA
        return range(a.builder, multiply(a as RealRange, b as RealRange))

    val newCentral = math.mulRounded(a.central, b.central)
    var noise = math.mul(a.radius, b.radius, Rounding.AWAY)
    val nts = HashMap<Long, Double>()
    val idx: MutableSet<Long> = HashSet(a.xi.keys)
    idx.addAll(b.xi.keys)
    idx.forEach {
        val xi = if (a.xi.containsKey(it)) a.xi[it]!! else 0.0
        val yi = if (b.xi.containsKey(it)) b.xi[it]!! else 0.0
        nts[it] = xi * b.central + yi * a.central
        noise += nts[it]!!.ulp
    }
    return create(a.builder, multiply(a as RealRange, b as RealRange), newCentral.value, noise, nts)
}


/**
 * Reciprocal, a min-Range Approximations which gives us division.
 * Based on "Self-validated numerical methods and applications" by Stolfi and de Figueiredo (p.69-70 3.12 Reciprocal)
 */
fun inv(value: AffineForm): AffineForm {
    when {
        value.isEmpty() -> return value.builder.AF.Empty
        value.max.isInfinite && value.min.isInfinite -> return value.builder.AF.All
        value.isScalar() ->
            return if (value.central == 0.0) value.builder.AF.Empty
            else range(
                value.builder,
                math.div(1.0, value.central, Rounding.DOWN).toDoubleBound()?: DoubleBound.NegativeInfinity,
                math.div(1.0, value.central, Rounding.UP).toDoubleBound()?: DoubleBound.PositiveInfinity
            )
        value.max.toDouble() == 0.0 -> return range(value.builder, invRange(value as RealRange))
    }

    if (0.0 in value)
        return value.builder.AF.All // The result is infinity if 0 is included, but not in bounds

    // Infinities etc. of value are handled above ...
    val l = min(abs(value.min.finiteValue), abs(value.max.finiteValue))
    val u = max(abs(value.min.finiteValue), abs(value.max.finiteValue))
    var alpha = -1.0 / (u * u)
    val den = if (value.min.finiteValue < 0.0) -2.0 else 2.0
    var delta = (u+l)*(u+l)/(den*u*u*l)
    var noise = (u-l)*(u-l)/(2*u*u*l)
    noise += (noise.ulp + alpha.ulp + delta.ulp) + (u+l).ulp + (u-l).ulp

    if(value.builder.settings.affineFormLinearizationScheme==DDBuilder.ApproximationScheme.Chebyshev) {
        alpha=-1.0/(value.max.finiteValue*value.min.finiteValue)
        var touchingPoint = sqrt(1/-alpha)
        if(value.min.finiteValue<0)touchingPoint*=-1
        delta = (1.0/value.min.finiteValue + 1.0/touchingPoint - alpha * (value.min.finiteValue + touchingPoint)) / 2.0
        noise = abs(1.0/touchingPoint - 1/value.min.finiteValue - alpha * (touchingPoint - value.min.finiteValue) )/ 2.0
    }
    return affine(value,
        invRange(RealRange(value)),
        alpha, delta, max(0.0, noise))
}

fun divide(numerator: AffineForm, denominator: AffineForm): AffineForm =
    numerator * inv(denominator)

fun divide(numerator: AffineForm, denominator: Double): AffineForm =
    numerator * (1/denominator)

/**
 * ceiling function for AFs
 */
fun ceil(value: AffineForm) : AffineForm {
    when {
        value.isEmpty()   -> return value.builder.AF.Empty
        !value.isFinite() -> return range(value.builder, value.min, value.max)
    }
    val newMin = ceil(value.min.finiteValue)
    val newMax = ceil(value.max.finiteValue)
    return range(value.builder,
        newMin.toDoubleBound()?: DoubleBound.NegativeInfinity,
        newMax.toDoubleBound()?: DoubleBound.PositiveInfinity)
}

/**
 * Inverse operation of inv.
 */
fun invCeil(value: AffineForm) : AffineForm {
    when {
        value.isEmpty()   -> return value.builder.AF.Empty
        !value.isFinite() -> return range(value.builder, value.min, value.max)
    }
    val newMin = math.sub(value.min.finiteValue, 1.0, Rounding.DOWN)
    val newMax = floor(value.max.finiteValue) // , 1.0, Rounding.UP)
    return range(value.builder,
        min(newMin, newMax).toDoubleBound()?: DoubleBound.NegativeInfinity,
        max(newMin, newMax).toDoubleBound()?: DoubleBound.PositiveInfinity)
}

/**
 * ceiling function for AFs
 */
fun ceilAsLong(value: AffineForm) : LongBound =
    if (!value.max.isFinite) LongBound.PositiveInfinity
    else LongBound.Finite(ceil(value.max.finiteValue).toLong())

/**
 * floor function for AFs
 */
fun floor(value: AffineForm) : AffineForm {
    when {
        value.isEmpty()     -> return value.builder.AF.Empty
        (!value.isFinite()) -> return range(value.builder, value.min, value.max)
    }
    val lb = floor(value.min.finiteValue).toDoubleBound()?: DoubleBound.NegativeInfinity
    val ub = floor(value.max.finiteValue).toDoubleBound()?: DoubleBound.PositiveInfinity
    val (c, err) = midpoint(lb, ub)
    val r = (abs(lb.finiteValue - ub.finiteValue) * 0.5) + err
    val xi: HashMap<Long, Double> = HashMap() // not being used
    return create(value.builder, lb, ub, c, r, xi)
}

/**
 * Inverse operation to `floor`.
 */
fun invFloor(value: AffineForm) : AffineForm {
    val lb = value.min
    val ub = math.add(value.max, DoubleBound.Finite(1.0), Rounding.UP)
    val c = midpoint(lb, ub!!).value
    val r = (abs(math.sub(ub, lb, Rounding.UP)) ?: (DoubleBound.PositiveInfinity * 0.5))
    val xi: HashMap<Long, Double> = HashMap() // not being used
    return create(value.builder, lb, ub, c, r!!.finiteValue, xi)
}

/**
 * floor function for AFs
 */
fun floorAsLong(value: AffineForm) : Long = floor(value.min.finiteValue).toLong()

/**
 * floor function for AFs
 */
fun floorToIntRange(value: AffineForm) : IntegerRange =
    IntegerRange(LongBound.Finite(floor(value.min.finiteValue).toLong()))

/**
 * 2^x function for AffineForms
 */
fun power2(value: AffineForm): AffineForm = exp(value * ln(2.0))
fun log2(value: AffineForm): AffineForm = ln(value) * (1/ln(2.0))
fun log(value: AffineForm, base: AffineForm): AffineForm = divide(ln(value), ln(base))

/**
 * Intersection of a leaf and an interval returns a leaf.
 */
fun constrainTo(af: AffineForm, range: RealRange): AffineForm {

    // Scalars
    if (af.isScalar() && af.min in range) return scalar(af.builder, af.min)
    if (range.isScalar() && range.start in af) return scalar(af.builder, range.start)

    // range is in this, so we return range as new AF
    if ((af.min < range.start) && (range.endInclusive < af.max))
        return range(af.builder, range)

    // complete inclusion of this in range; we return this
    if ((range.start <= af.min) && range.endInclusive >= af.max)
        return range(af.builder, af)

    // no complete inclusion, so we create an AADD with range constraints
    val newMin = max(af.min, range.start)
    val newMax = min(af.max, range.endInclusive)
    val result = if (newMin == newMax)
        scalar(af.builder, newMin)
    else
        range(af.builder, newMin, newMax)
    return result
}

fun sqr(af: AffineForm): AffineForm = af*af
