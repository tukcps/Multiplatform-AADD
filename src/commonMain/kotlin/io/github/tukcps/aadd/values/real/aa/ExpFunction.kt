package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.exp
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.exp

object ExpFunction : UnaryFunction {
    override val domain = RealRange.Reals
    override val image = RealRange(DoubleBound.Finite(0.0),DoubleBound.PositiveInfinity)
    override fun curvature(x: RealRange) = Curvature.CONVEX
    override fun value(x: Double, rounding: Rounding): Double = exp(x)
    override fun range(x: RealRange): RealRange = exp(x)
    override fun derivative(x: Double, rounding: Rounding): Double = exp(x)
    override fun secondDerivativeBound(x: RealRange): Double = exp(x.max.toDouble())
    override fun splitPoints(x: RealRange): DoubleArray = DoubleArray(0)
}

internal fun ExpFunction.linearize(x: AffineForm): LinearApproximation? =
    MinimaxApproximation.linearize(this, x)

fun exp(x: AffineForm): AffineForm = ExpFunction.approximate(x)