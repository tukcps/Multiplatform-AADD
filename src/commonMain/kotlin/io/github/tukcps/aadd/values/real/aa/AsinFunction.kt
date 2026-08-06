package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.asin
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.abs

/**
 * Arc sine function.
 */
internal object AsinFunction : UnaryFunction {

    private val D2_MAX = 2.0 / (3.0 * sqrt(3.0))

    override val domain = RealRange(-1.0..1.0)
    override val image = RealRange(-PI / 2.0..PI / 2.0)

    override fun approximationScheme(x: AffineForm) =
        if (x.min < DoubleBound.Finite(0.0) && x.max > DoubleBound.Finite(0.0))
            TaylorApproximation
        else
            MinimaxApproximation

    override fun curvature(x: RealRange) = when {
        x.max <= DoubleBound.Finite(0.0) -> Curvature.CONVEX
        x.min >= DoubleBound.Finite(0.0) -> Curvature.CONCAVE
        else         -> Curvature.MIXED
    }

    override fun splitPoints(x: RealRange) =
        if (x.contains(0.0)) doubleArrayOf(0.0) else DoubleArray(0)

    override fun value(x: Double, rounding: Rounding) =
        math.asin(x, rounding)

    override fun range(x: RealRange): RealRange = asin(x)

    override fun derivative(x: Double, rounding: Rounding): Double =
        math.div(
            1.0,
            sqrt(1.0 - x * x),
            rounding
        )

    private fun secondDerivative(x: Double): Double =
        abs(x / (1.0 - x * x).pow(1.5))

    override fun secondDerivativeBound(x: RealRange): Double = when {
        x.contains(-1.0) || x.contains(1.0) -> Double.POSITIVE_INFINITY
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
        val x = sqrt(1.0 - 1.0 / (slope * slope))
        return if (interval.max <= DoubleBound.Finite(0.0)) -x else x
    }
}

internal fun AsinFunction.linearize(x: AffineForm): LinearApproximation? =
    MinimaxApproximation.linearize(this, x)

fun asin(x: AffineForm): AffineForm = AsinFunction.approximate(x)