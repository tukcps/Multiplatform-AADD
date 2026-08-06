package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.rounding.FMA
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.ulp

/**
 * Computes linear minimax (Chebyshev) approximations.
 *
 * The approximation minimizes the maximum deviation from the function over
 * the complete argument interval. It is applicable to monotone convex or
 * concave functions providing an inverse derivative.
 */
internal object MinimaxApproximation : ApproximationScheme {

    private val math = IEEE754RoundingMath

    override fun linearize(
        function: UnaryFunction,
        argument: AffineForm
    ): LinearApproximation? {

        require(function.curvature(argument) != Curvature.MIXED)

        val a = argument.min.toDouble()
        val b = argument.max.toDouble()

        val fa = function.value(a, Rounding.NEAREST)
        val fb = function.value(b, Rounding.NEAREST)

        val alpha = math.div(
            math.sub(fb, fa, Rounding.NEAREST),
            b - a,
            Rounding.NEAREST
        )

        val c = function.inverseDerivative(alpha, argument) // .coerceIn(a, b)
        if (!c.isFinite()) return null
        require(c in a..b)

        val fc = function.value(c, Rounding.NEAREST)

        val tangent = FMA.compute(-alpha, c, fc)
        val secant = FMA.compute(-alpha, a, fa)

        val delta = math.div(math.add(tangent, secant, Rounding.NEAREST), 2.0, Rounding.NEAREST)
        val noise = math.div(kotlin.math.abs(math.sub(tangent, secant, Rounding.UP)) + fc.ulp, 2.0, Rounding.UP)
        return if (alpha.isFinite() || delta.isFinite() || noise.isFinite())
            LinearApproximation(alpha = alpha, delta = delta, noise = noise)
        else
            null
    }
}