package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.cos
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.PI

internal object CosFunction : UnaryFunction {

    override val domain = RealRange.Reals
    override val image = RealRange(-1.0..1.0)

    override fun approximationScheme(x: AffineForm) =
        TaylorApproximation

    override fun value(x: Double, rounding: Rounding) =
        math.cos(x, rounding)

    override fun splitPoints(x: RealRange) =
        periodicPoints(x, PI / 2.0, PI)

    override fun range(x: RealRange) =
        cos(x)

    override fun derivative(x: Double, rounding: Rounding) =
        -math.sin(x, rounding)

    override fun secondDerivativeBound(x: RealRange) =
        1.0

    override fun curvature(x: RealRange) = when {
        cos(x).max.toDouble() <= 0.0 -> Curvature.CONVEX
        cos(x).min.toDouble() >= 0.0 -> Curvature.CONCAVE
        else -> Curvature.MIXED
    }
}

internal fun CosFunction.linearize(x: AffineForm): LinearApproximation? =
    TaylorApproximation.linearize(this, x)

fun cos(x: AffineForm): AffineForm = CosFunction.approximate(x)