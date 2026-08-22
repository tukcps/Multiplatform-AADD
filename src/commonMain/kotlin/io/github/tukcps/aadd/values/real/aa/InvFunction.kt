package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.rounding.Rounding
import io.github.tukcps.aadd.values.real.toDoubleBound
import kotlin.math.abs
import kotlin.math.sqrt

internal object InvFunction : UnaryFunction {

    const val EPSILON = 1.0e-3

    override val domain: RealRange = RealRange.Reals
    override val image: RealRange = RealRange.Reals

    override fun value(x: Double, rounding: Rounding): Double =
        math.div(1.0, x, rounding)

    override fun range(x: RealRange): RealRange {
        if (x.isEmpty()) return RealRange.Empty

        val a = x.min.toDouble()
        val b = x.max.toDouble()

        if (a <= 0.0 && b >= 0.0)
            return RealRange.Reals

        return RealRange(
            value(b, Rounding.DOWN).toDoubleBound()
                ?: DoubleBound.NegativeInfinity,
            value(a, Rounding.UP).toDoubleBound()
                ?: DoubleBound.PositiveInfinity
        )
    }

    override fun derivative(
        x: Double,
        rounding: Rounding
    ): Double =
        math.div(
            -1.0,
            math.mul(x, x, rounding),
            rounding
        )

    override fun secondDerivativeBound(x: RealRange): Double {
        val minAbs = minOf(
            abs(x.min.toDouble()),
            abs(x.max.toDouble())
        )

        return math.div(
            2.0,
            math.mul(
                math.mul(minAbs, minAbs, Rounding.UP),
                minAbs,
                Rounding.UP
            ),
            Rounding.UP
        )
    }

    override fun curvature(x: RealRange): Curvature =
        when {
            x.max.toDouble() < 0.0 -> Curvature.CONCAVE
            x.min.toDouble() > 0.0 -> Curvature.CONVEX
            else -> Curvature.MIXED
        }

    override fun inverseDerivative(
        slope: Double,
        interval: RealRange,
        rounding: Rounding
    ): Double {
        require(slope < 0.0)

        val root = math.div(
            1.0,
            sqrt(-slope),
            rounding
        )

        return when {
            root in interval -> root
            -root in interval -> -root
            else -> throw IllegalArgumentException(
                "No solution for slope $slope in $interval"
            )
        }
    }

    override fun approximationScheme(
        x: AffineForm
    ): ApproximationScheme = MinimaxApproximation
}

internal fun InvFunction.linearize(
    x: AffineForm
): LinearApproximation? =
    MinimaxApproximation.linearize(this, x)

internal fun invSplit(x: AffineForm): AADD {
    val builder = x.builder
    val xAsAADD = builder.real(x)

    val result = xAsAADD.lessThan(-InvFunction.EPSILON).ite(
        t = builder.leaf(InvFunction
            .approximate(x constrainToRange RealRange(x.min.toDouble()..-InvFunction.EPSILON))),
        e = xAsAADD.lessThan(InvFunction.EPSILON).ite(
            t = when {
                (x.isScalar() && x.central == 0.0)
                    -> builder.Reals.Empty
                x.max <= DoubleBound.Finite(0.0)
                    -> builder.real(DoubleBound.NegativeInfinity .. DoubleBound.Finite(0.0))
                x.min >= DoubleBound.Finite(0.0)
                    -> builder.real(DoubleBound.Finite(0.0) ..DoubleBound.PositiveInfinity)
                else
                    -> builder.Reals.All
            },
            e = builder.leaf(InvFunction
                .approximate(x constrainToRange RealRange(InvFunction.EPSILON..x.max.toDouble())))
        )
    )
    return result
}
