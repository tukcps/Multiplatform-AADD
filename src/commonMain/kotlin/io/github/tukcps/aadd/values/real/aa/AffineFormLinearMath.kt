package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.create
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.range
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.affine
import io.github.tukcps.aadd.values.real.ia.plus
import io.github.tukcps.aadd.values.real.rounding.FMA
import io.github.tukcps.aadd.values.real.rounding.Rounding
import io.github.tukcps.aadd.values.real.toDoubleBound
import kotlin.math.max
import kotlin.math.min
import io.github.tukcps.aadd.values.real.ia.negate as negateRange

/** Adds a (possibly negative) scalar to an affine form. */
fun add(a: AffineForm, b: Double): AffineForm {
    when {
        a.isEmpty() -> return a.builder.AF.Empty
        a.isReals() -> return a.builder.AF.All
        b.isNaN()   -> return a.builder.AF.Empty
        b == Double.POSITIVE_INFINITY -> return AffineForm.scalar(a.builder, Double.POSITIVE_INFINITY)
        b == Double.NEGATIVE_INFINITY -> return AffineForm.scalar(a.builder, Double.NEGATIVE_INFINITY)
    }
    val newNoiseTerms = HashMap(a.xi)
    val (newCentral, err) = math.addRounded(a.central, b)
    return create(a.builder, a as RealRange + RealRange(b), newCentral, err, newNoiseTerms)
}

/**
 * Adds two affine forms.
 * The resulting interval is tightened by intersecting the affine
 * approximation with the interval arithmetic result.
 * @param a left affine form
 * @param b right affine form
 * @return affine enclosure of the sum
 */
fun add(a: AffineForm, b: AffineForm): AffineForm {
    check(a.builder == b.builder)
    when {
        a.isEmpty() || b.isEmpty()  -> return a.builder.AF.Empty
        a.isReals() || b.isReals()  -> return a.builder.AF.All
        a.isZero()                  -> return b
        b.isZero()                  -> return a
    }

    val newXi = HashMap<Long, Double>(2 * a.builder.settings.affineFormHashMapSize)
    val (newCentral, errNewCentral) = math.addRounded(a.central, b.central)

    for (i in a.xi.keys + b.xi.keys) {
        val v1 = a.xi[i] ?: 0.0
        val v2 = b.xi[i] ?: 0.0
        val sum = math.add(v1, v2, Rounding.AWAY)
        if (sum != 0.0) newXi[i] = sum
    }

    return create(a.builder, a as RealRange + b as RealRange, newCentral, errNewCentral, newXi)
}

/** Negation; no roundoff error  */
fun negate(value: AffineForm): AffineForm {
    when {
        value.isEmpty() -> return value.builder.AF.Empty
        value.isReals() -> return value.builder.AF.All
        value.isZero() -> return value.builder.AF.Zero
    }
    val nc = -value.central
    val nts = HashMap<Long, Double>(value.builder.settings.affineFormHashMapSize)
    value.xi.keys.forEach { nts[it] = -value.xi[it]!! }
    return create(value.builder, negateRange(value as RealRange), nc, 0.0, nts)
}

/**
 * Subtracts two affine forms.
 * The resulting interval is tightened by intersecting the affine
 * approximation with the interval arithmetic result.
 * @param a left affine form
 * @param b right affine form
 * @return affine enclosure of the difference
 */
fun subtract(a: AffineForm, b: AffineForm): AffineForm = add(a, negate(b))
fun subtract(a: AffineForm, b: Double): AffineForm = add(a, -b)

/** Scalar addition, multiplication and noise increment on a single form */
fun affine(value: AffineForm, alpha: Double, delta: Double, noise: Double): AffineForm {
    when {
        value.isEmpty()     -> return value.builder.AF.Empty
        !value.isFinite()   -> return range(value.builder, affine(value as RealRange, alpha, delta, noise))
    }
    val newCenter = FMA.compute(value.central, alpha, delta)
    val newR = noise
    val newXi = HashMap<Long, Double>(value.xi.size + 1)
    for ((id, coeff) in value.xi)
        newXi[id] = math.mul(coeff, alpha, Rounding.AWAY)
    val nMin = math.add(math.mul(value.min.toDouble(), alpha, Rounding.DOWN), delta, Rounding.DOWN)
    val nMax = math.add(math.mul(value.max.toDouble(), alpha, Rounding.UP), delta, Rounding.UP)
    return create(value.builder,
        RealRange(
            min(math.sub(nMin, noise, Rounding.DOWN), math.sub(nMax, noise, Rounding.DOWN))
                .toDoubleBound()?: DoubleBound.NegativeInfinity,
            max(math.add(nMin, noise, Rounding.UP), math.add(nMax, noise, Rounding.UP))
                .toDoubleBound()?: DoubleBound.PositiveInfinity,
        ), newCenter, newR, newXi)
}


/** Scalar addition, multiplication and noise increment on a single form */
fun affine(
    value: AffineForm,
    range: RealRange,
    alpha: Double,
    delta: Double,
    noise: Double
): AffineForm {
    when {
        value.isReals()     -> return value.builder.AF.All
        value.isEmpty()     -> return value.builder.AF.Empty
        !value.isFinite()   -> return range(value.builder, range)
    }
    val newCenter = FMA.compute(value.central, alpha, delta)
    val newNoise = noise

    val newXi = HashMap<Long, Double>(value.xi.size + 1)
    for ((id, coeff) in value.xi)
        newXi[id] = math.mul(coeff, alpha, Rounding.AWAY)

    val nMin = math.add(math.mul(value.min.toDouble(), alpha, Rounding.DOWN), delta, Rounding.DOWN)
    val nMax = math.add(math.mul(value.max.toDouble(), alpha, Rounding.UP), delta, Rounding.UP)

    val affineRange = RealRange(
        min(math.sub(nMin, noise, Rounding.DOWN), math.sub(nMax, noise, Rounding.DOWN)).toDoubleBound()
            ?: DoubleBound.NegativeInfinity,
        max(math.add(nMin, noise, Rounding.UP), math.add(nMax, noise, Rounding.UP)).toDoubleBound()
            ?: DoubleBound.PositiveInfinity
    )

    return create(
        value.builder,
        range.intersect(affineRange),
        newCenter,
        newNoise,
        newXi
    )
}