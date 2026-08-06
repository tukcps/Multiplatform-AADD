package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.ln as lnIA
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.ln

internal object LnFunction : UnaryFunction {

    private val math = IEEE754RoundingMath

    override val domain = RealRange(DoubleBound.Finite(0.0), DoubleBound.PositiveInfinity)
    override val image = RealRange(DoubleBound.NegativeInfinity, DoubleBound.PositiveInfinity)
    override fun curvature(x: RealRange) = Curvature.CONCAVE
    override fun value(x: Double, rounding: Rounding) = math.ln(x, rounding)
    override fun range(x: RealRange) = lnIA(x)
    override fun derivative(x: Double, rounding: Rounding) =
        math.div(1.0, x, rounding)
    override fun inverseDerivative(slope: Double, interval: RealRange, rounding: Rounding) =
        math.div(1.0, slope, rounding)
    override fun secondDerivativeBound(x: RealRange): Double {
        val min2 = math.mul(x.min.toDouble(), x.min.toDouble(), Rounding.DOWN)
        return math.div(1.0, min2, Rounding.UP)
    }

    override fun splitPoints(x: RealRange) =
        if (0.0 in x) doubleArrayOf(0.0) else DoubleArray(0)

}

internal fun LnFunction.linearize(x: AffineForm): LinearApproximation? =
    MinimaxApproximation.linearize(this, x)

fun ln(x: AffineForm): AffineForm =
    LnFunction.approximate(x)

fun log(value: AffineForm, base: Double): AffineForm =
    affine(ln(value), 1.0 / ln(base), 0.0, 0.0)
