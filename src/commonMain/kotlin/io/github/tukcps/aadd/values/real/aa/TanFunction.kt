package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.containsPeriodic
import io.github.tukcps.aadd.values.real.ia.tan
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.math.*

/**
 * Tangent function for affine approximation.
 */
internal object TanFunction : UnaryFunction {

    override val domain = RealRange.Reals
    override val image = RealRange.Reals

    /**
     * Curvature of the function.
     */
    override fun curvature(x: RealRange): Curvature {
        if (x.containsPeriodic(PI / 2.0, PI))
            return Curvature.MIXED

        if (x.containsPeriodic(0.0, PI))
            return Curvature.MIXED

        return if (tan(x).min.toDouble() > 0.0)
            Curvature.CONVEX
        else
            Curvature.CONCAVE
    }

    override fun approximationScheme(x: AffineForm) =
        TaylorApproximation

    override fun value(x: Double, rounding: Rounding) =
        math.tan(x, rounding)

    override fun range(x: RealRange) =
        tan(x)

    override fun derivative(x: Double, rounding: Rounding) =
        math.div(
            1.0,
            math.mul(
                math.cos(x, rounding),
                math.cos(x, rounding),
                rounding
            ),
            rounding
        )

    /**
     * Returns an upper bound for |f''(x)| on the given interval.
     *
     * The interval is guaranteed not to contain any of the values returned by
     * [splitPoints].
     *
     * @param x interval of interest
     * @return upper bound of |f''|
     */
    override fun secondDerivativeBound(x: RealRange): Double {
        if (x.containsPeriodic(PI / 2.0, PI))
            return Double.POSITIVE_INFINITY

        fun bound(v: Double): Double {
            val sin = math.sin(v, Rounding.UP)
            val cos = math.cos(v, Rounding.DOWN)

            val cos2 = math.mul(cos, cos, Rounding.UP)
            val cos3 = math.mul(cos2, cos, Rounding.UP)

            val numerator = math.mul(2.0, sin, Rounding.UP)
            val value = math.div(numerator, cos3, Rounding.UP)

            return abs(value)
        }

        return max(
            bound(x.min.toDouble()),
            bound(x.max.toDouble())
        )
    }
}

fun tan(x: AffineForm): AffineForm = TanFunction.approximate(x)
