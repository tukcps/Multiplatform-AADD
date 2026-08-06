package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.acos
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.abs
import kotlin.math.max

/**
 * Arc cosine function.
 */
internal object AcosFunction : UnaryFunction {

    override val domain = RealRange(-1.0..1.0)
    override val image = RealRange(0.0..PI)

    override fun approximationScheme(x: AffineForm) =
        if (x.min < DoubleBound.Finite(0.0) && x.max > DoubleBound.Finite(0.0))
            TaylorApproximation
        else MinimaxApproximation

    override fun curvature(x: RealRange) = when {
        x.max <= DoubleBound.Finite(0.0) -> Curvature.CONCAVE
        x.min >= DoubleBound.Finite(0.0) -> Curvature.CONVEX
        else -> Curvature.MIXED
    }

    override fun splitPoints(x: RealRange) =
        if (x.contains(0.0)) doubleArrayOf(0.0) else DoubleArray(0)

    override fun value(x: Double, rounding: Rounding) =
        math.acos(x, rounding)

    override fun range(x: RealRange): RealRange = acos(x)

    override fun derivative(x: Double, rounding: Rounding) =
        math.div(-1.0, sqrt(1.0 - x * x), rounding)

    private fun secondDerivative(x: Double) =
        abs(x / (1.0 - x * x).pow(1.5))

    override fun secondDerivativeBound(x: RealRange) = when {
        x.contains(-1.0) || x.contains(1.0) -> Double.POSITIVE_INFINITY
        else -> max(
            secondDerivative(x.min.toDouble()),
            secondDerivative(x.max.toDouble())
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