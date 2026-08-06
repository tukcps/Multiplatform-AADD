package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.containsPeriodic
import io.github.tukcps.aadd.values.real.ia.sin
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.*

/**
 * Sine function for affine approximation.
 */
internal object SinFunction : UnaryFunction {

    override val domain = RealRange.Reals
    override val image = RealRange(-1.0..1.0)

    override fun approximationScheme(x: AffineForm) =
        TaylorApproximation

    override fun curvature(x: RealRange) = when {
        sin(x).max.toDouble() <= 0.0 -> Curvature.CONCAVE
        sin(x).min.toDouble() >= 0.0 -> Curvature.CONVEX
        else -> Curvature.MIXED
    }

    override fun splitPoints(x: RealRange): DoubleArray =
        periodicPoints(x, 0.0, PI)

    override fun value(x: Double, rounding: Rounding) =
        math.sin(x, rounding)

    override fun range(x: RealRange) =
        sin(x)

    override fun derivative(x: Double, rounding: Rounding) =
        math.cos(x, rounding)

    override fun secondDerivativeBound(x: RealRange) =
        1.0
}

/**
 * Returns periodic points offset + k·period contained in x.
 */
internal fun periodicPoints(
    x: RealRange,
    offset: Double,
    period: Double
): DoubleArray {
    if (x.isEmpty()) return DoubleArray(0)

    val first = ceil((x.min.toDouble() - offset) / period).toLong()
    val last = floor((x.max.toDouble() - offset) / period).toLong()

    return DoubleArray((last - first + 1).coerceAtLeast(0).toInt()) { i ->
        offset + (first + i) * period
    }
}

internal fun SinFunction.linearize(x: AffineForm): LinearApproximation? =
    TaylorApproximation.linearize(this, x)

fun sin(x: AffineForm): AffineForm = SinFunction.approximate(x)