package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.ia.RealRange

/**
 * Computes a linear affine approximation of a unary function.
 *
 * Implementations determine the parameters α, δ and ε of an approximation
 *
 *     f(x) ≈ α·x + δ + ε,
 *
 * where |ε| ≤ noise.
 */
interface ApproximationScheme {
    /**
     * The returned parameters are transformed into an affine form by the common
     * affine approximation infrastructure.
     *
     * @param function function to approximate
     * @param argument argument to approximate over
     * @return parameters of the linear affine approximation
     */
    fun linearize(function: UnaryFunction, argument: AffineForm): LinearApproximation?
}

/**
 * Represents a linear affine approximation
 *
 *     f(x) ≈ α·x + δ + ε,
 *
 * where |ε| ≤ noise.
 */
class LinearApproximation(
    val alpha: Double,
    val delta: Double,
    val noise: Double
)


fun UnaryFunction.approximate(x: AffineForm): AffineForm {
    when {
        x.isEmpty() -> return  x.builder.AF.Empty
        x.isReals() -> return  AffineForm.range(x.builder, image)
        x.max.isInfinite -> return AffineForm.range(x.builder, range(x))
        x.min.isInfinite -> return AffineForm.range(x.builder, range(x))
        range(x).isEmpty() -> return x.builder.AF.Empty
        x.isScalar() -> return AffineForm.range(x.builder, range(RealRange(x.min, x.min)))
    }
    val range = this.range(x)
    val l = approximationScheme(x).linearize(this, x)
    return if (l == null)
        AffineForm.range(x.builder, range)
    else
        affine(x, range, l.alpha, l.delta, l.noise)
}