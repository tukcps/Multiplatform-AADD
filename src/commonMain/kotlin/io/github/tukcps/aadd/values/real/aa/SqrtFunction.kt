package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.sqrt
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.rounding.Rounding

/**
 * Square root function.
 *
 * The function is defined on the non-negative real numbers and is globally
 * concave. Affine approximations are computed using first-order Taylor
 * approximation with an interval enclosure.
 */
internal object SqrtFunction : UnaryFunction {

    private val math = IEEE754RoundingMath

    override val domain = RealRange(DoubleBound.Finite(0.0), DoubleBound.PositiveInfinity)
    override val image = RealRange(DoubleBound.Finite(0.0), DoubleBound.PositiveInfinity)
    override fun curvature(x: RealRange) = Curvature.CONCAVE

    override fun value(x: Double, rounding: Rounding) = math.sqrt(x, rounding)

    override fun range(x: RealRange) = sqrt(x)

    override fun derivative(x: Double, rounding: Rounding) =
        math.div(1.0, math.mul(2.0, math.sqrt(x, rounding), rounding), rounding)
    override fun inverseDerivative(slope: Double, interval: RealRange, rounding: Rounding): Double {
        val t = math.mul(2.0, slope, rounding)
        return math.div(1.0, math.mul(t, t, rounding), rounding)
    }
    override fun secondDerivativeBound(x: RealRange): Double {
        val min = x.min.toDouble()
        if (min == 0.0) return Double.POSITIVE_INFINITY
        val sqrt = math.sqrt(min, Rounding.DOWN)
        return math.div(
            1.0,
            math.mul(4.0, math.mul(math.mul(sqrt, sqrt, Rounding.DOWN), sqrt, Rounding.DOWN), Rounding.DOWN),
            Rounding.UP
        )
    }

    override fun splitPoints(x: RealRange) =
        if (0.0 in x) doubleArrayOf(0.0) else DoubleArray(0)

}
internal fun SqrtFunction.linearize(x: AffineForm) =
    MinimaxApproximation.linearize(this, x)

fun sqrt(x: AffineForm): AffineForm = SqrtFunction.approximate(x)