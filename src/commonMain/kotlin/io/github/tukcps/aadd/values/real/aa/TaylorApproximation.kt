package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.rounding.FMA
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.ulp

/**
 * #### Computes first-order affine Taylor approximations.
 * The function is linearized at the midpoint of the argument. The nonlinear
 * remainder is conservatively enclosed by a new noise symbol using an upper
 * bound on the second derivative. Exact interval bounds are obtained
 * independently by interval arithmetic.
 */
object TaylorApproximation : ApproximationScheme {

    private val math = IEEE754RoundingMath

    /**
     * Computes the parameters of a first-order affine Taylor approximation.
     *
     * The returned linear approximation has the form
     *
     *     f(x) ≈ α·x + δ + ε,
     *
     * where |ε| is bounded by the returned noise term.
     */
    override fun linearize(
        function: UnaryFunction,
        argument: AffineForm
    ): LinearApproximation {

        val center = function.value(argument.central, Rounding.NEAREST)
        val slope = function.derivative(argument.central, Rounding.NEAREST)

        val bound = function.secondDerivativeBound(argument)
        val r2 = math.mul(argument.radius, argument.radius, Rounding.UP)
        val tmp = math.mul(bound, r2, Rounding.UP)
        val noise = math.div(tmp, 2.0, Rounding.UP) + center.ulp
        val delta = FMA.compute(-slope, argument.central, center)

        return LinearApproximation(
            alpha = slope,
            delta = delta,
            noise = noise,
        )
    }
}