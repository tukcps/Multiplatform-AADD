package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.create
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.pow as powIA
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.nextUp
import kotlin.math.pow

private val Double.isInteger: Boolean
    get() = this == toLong().toDouble()

private val Double.isEvenInteger: Boolean
    get() = isInteger && toLong() % 2L == 0L

private val Double.isOddInteger: Boolean
    get() = isInteger && toLong() % 2L != 0L

/**
 * Power function x^c with constant exponent.
 *
 * Affine approximations are computed using first-order Taylor approximation
 * with an interval enclosure.
 */
internal class PowFunction(
    private val exponent: Double
) : UnaryFunction {

    private val math = IEEE754RoundingMath

    override val domain = when {
            exponent.isInteger && exponent >= 0.0 -> RealRange.Reals
            exponent.isInteger -> RealRange.Reals    // TODO ℝ \ {0}
            else -> RealRange(DoubleBound.Finite(0.0), DoubleBound.PositiveInfinity)
        }

    override val image = when {
            exponent == 0.0 -> RealRange(1.0..1.0)
            exponent.isEvenInteger -> RealRange(DoubleBound.Finite(0.0), DoubleBound.PositiveInfinity)
            else -> RealRange.Reals
        }

    override fun curvature(x: RealRange) = when {
        exponent == 0.0 -> Curvature.CONVEX
        exponent == 1.0 -> Curvature.MIXED
        exponent < 0.0  -> Curvature.CONVEX
        exponent < 1.0  -> Curvature.CONCAVE
        exponent.isEvenInteger -> Curvature.CONVEX
        exponent.isOddInteger -> Curvature.MIXED
        else -> Curvature.CONVEX
    }

    override fun value(x: Double, rounding: Rounding) =
        math.pow(x, exponent, rounding)

    override fun range(x: RealRange) =
        powIA(x, exponent)

    override fun derivative(x: Double, rounding: Rounding) =
        math.mul(exponent, math.pow(x, exponent - 1.0, rounding), rounding)

    override fun secondDerivativeBound(x: RealRange): Double {
        if (exponent == 0.0 || exponent == 1.0)
            return 0.0

        val factor = abs(exponent * (exponent - 1.0))
        val arg = if (exponent > 2.0) x.max.finiteValue else x.min.finiteValue

        return math.mul(
            factor,
            math.pow(arg, exponent - 2.0, Rounding.UP),
            Rounding.UP
        )
    }
}
internal fun PowFunction.linearize(x: AffineForm) =
    MinimaxApproximation.linearize(this, x)

fun pow(x: AffineForm, exponent: Double): AffineForm = PowFunction(exponent).approximate(x)

/**
 * Computes the power function x^y for affine arguments.
 *
 * A first-order affine approximation is constructed around the central values
 * of the operands. The nonlinear remainder is estimated from the four corner
 * values of the interval enclosure and added as an independent noise symbol.
 * The final affine form is created using the interval enclosure obtained by
 * interval arithmetic.
 */
fun pow(base: AffineForm, exponent: AffineForm): AffineForm {
    when {
        base.isEmpty() || exponent.isEmpty() -> return base.builder.AF.Empty
        base.isReals() || exponent.isReals() -> return base.builder.AF.All

        exponent.isZero() -> return base.builder.AF.One
        exponent.isOne() -> return base

        base.isZero() -> return base.builder.AF.Zero
        base.isOne() -> return base.builder.AF.One

        exponent.isScalar() -> return pow(base, exponent.central)
    }

    val ia = powIA(RealRange(base), RealRange(exponent))

    val x0 = max(base.central, base.min.finiteValue.nextUp())
    val y0 = exponent.central

    val f0 = x0.pow(y0)

    val alphaX = y0 * x0.pow(y0 - 1.0)
    val alphaY = f0 * ln(x0)

    val linear = alphaX * base + alphaY * exponent

    fun residual(x: Double, y: Double) =
        x.pow(y) - (
                f0 +
                        alphaX * (x - x0) +
                        alphaY * (y - y0)
                )

    val residuals = listOf(
        residual(base.min.finiteValue, exponent.min.finiteValue),
        residual(base.min.finiteValue, exponent.max.finiteValue),
        residual(base.max.finiteValue, exponent.min.finiteValue),
        residual(base.max.finiteValue, exponent.max.finiteValue)
    )

    val rMin = residuals.min()
    val rMax = residuals.max()

    return create(
        builder = base.builder,
        range = ia,
        central = linear.central - alphaX * x0 - alphaY * y0 + f0 + (rMin + rMax) / 2.0,
        newNoise = (rMax - rMin) / 2.0,
        xi = linear.xi
    )
}
