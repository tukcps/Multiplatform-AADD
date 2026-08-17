package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.rounding.Rounding

/**
 * A real-valued unary mathematical function.
 *
 * Besides the function value, implementations may provide information useful
 * for affine approximation, such as derivative bounds and preferred split
 * points.
 */
interface UnaryFunction {

    /**
     * Domain of the function.
     */
    val domain: RealRange

    /**
     * Range resp. image of the function.
     */
    val image: RealRange

    /**
     * Curvature of the function.
     */
    fun curvature(x: RealRange): Curvature

    /**
     * Returns x such that f'(x)=slope.
     */
    fun inverseDerivative(slope: Double, interval: RealRange, rounding: Rounding = Rounding.NEAREST): Double =
        throw UnsupportedOperationException()

    /**
     * Computes the function value.
     * @param x argument
     * @return f(x)
     */
    fun value(x: Double, rounding: Rounding): Double
    fun range(x: RealRange): RealRange

    /**
     * Computes the first derivative.
     *
     * The argument is guaranteed not to be one of the values returned by
     * [splitPoints]. Implementations may therefore assume differentiability.
     *
     * @param x argument
     * @return f'(x)
     */
    fun derivative(x: Double, rounding: Rounding): Double

    /**
     * Returns an upper bound for |f''(x)| on the given interval.
     *
     * The interval is guaranteed not to contain any of the values returned by
     * [splitPoints].
     *
     * @param x interval of interest
     * @return upper bound of |f''|
     */
    fun secondDerivativeBound(x: RealRange): Double

    /**
     * Preferred points at which an approximation algorithm should split the
     * interval before approximating.
     *
     * Typical examples are:
     *  - non-differentiable points (abs)
     *  - domain boundaries (sqrt, ln, asin, acos)
     *  - singularities (tan)
     *  - inflection points (optional)
     *
     * Only points contained in the supplied interval shall be returned.
     *
     * @param x interval to inspect
     * @return split points in ascending order
     */
    fun splitPoints(x: RealRange): DoubleArray = DoubleArray(0)

    fun approximationScheme(x: AffineForm): ApproximationScheme = TaylorApproximation
}

internal fun UnaryFunction.linearize(x: AffineForm): LinearApproximation? =
    approximationScheme(x).linearize(this, x)

/**
 * Global curvature of a function.
 */
enum class Curvature {

    /** f''(x) ≥ 0 on the complete domain. */
    CONVEX,

    /** f''(x) ≤ 0 on the complete domain. */
    CONCAVE,

    /** Curvature changes over the domain. */
    MIXED
}