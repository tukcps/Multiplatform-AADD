package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.atan
import io.github.tukcps.aadd.values.real.rounding.FMA
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Arc tangent function.
 */
internal object AtanFunction : UnaryFunction {

    private const val X_MAX = 0.5773502691896258           // 1/sqrt(3)
    private const val D2_MAX = 0.649519052838329           // 9/(8*sqrt(3))

    override val domain = RealRange.Reals
    override val image = RealRange(-PI / 2.0..PI / 2.0)

    override fun approximationScheme(x: AffineForm) =
        if (x.min.toDouble() < 0.0 && x.max.toDouble() > 0.0) TaylorApproximation
        else MinimaxApproximation

    override fun curvature(x: RealRange) = when {
        x.max.toDouble() <= 0.0 -> Curvature.CONVEX
        x.min.toDouble() >= 0.0 -> Curvature.CONCAVE
        else         -> Curvature.MIXED
    }

    override fun splitPoints(x: RealRange): DoubleArray =
        if (x.contains(0.0)) doubleArrayOf(0.0) else DoubleArray(0)

    override fun value(x: Double, rounding: Rounding) =
        math.atan(x, rounding)

    override fun range(x: RealRange) =
        atan(x)

    override fun derivative(x: Double, rounding: Rounding) =
        math.div(1.0, FMA.compute(x, x, 1.0), rounding)

    private fun secondDerivative(x: Double): Double {
        val t = FMA.compute(x, x, 1.0)
        return abs(math.div(-2.0 * x, t * t, Rounding.UP))
    }

    override fun secondDerivativeBound(x: RealRange): Double = when {
        x.contains(-X_MAX) || x.contains(X_MAX) -> D2_MAX
        else -> max(
            secondDerivative(x.min.finiteValue),
            secondDerivative(x.max.finiteValue)
        )
    }

    override fun inverseDerivative(
        slope: Double,
        interval: RealRange,
        rounding: Rounding
    ): Double {
        val x: Double = sqrt(math.div(1.0, slope, rounding) - 1.0)
        return if (interval.max.toDouble() <= 0.0) -x else x
    }
}

internal fun AtanFunction.linearize(x: AffineForm): LinearApproximation? =
    MinimaxApproximation.linearize(this, x)

fun atan(x: AffineForm): AffineForm = AtanFunction.approximate(x)