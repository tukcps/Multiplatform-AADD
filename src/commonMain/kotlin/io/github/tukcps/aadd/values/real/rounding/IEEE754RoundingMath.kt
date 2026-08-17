package io.github.tukcps.aadd.values.real.rounding

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.rounding.ErrorFreeTransforms.twoSum
import io.github.tukcps.aadd.values.real.toDoubleBound
import io.github.tukcps.aadd.values.real.unaryMinus
import kotlin.math.*

/**
 * Default implementation of [RoundingMath] based on IEEE-754 double precision.
 *
 * Operations are evaluated using the platform's default floating-point
 * arithmetic and then conservatively adjusted to the requested rounding
 * direction.
 */
object IEEE754RoundingMath : RoundingMath {

    override fun add(a: Double, b: Double, rounding: Rounding) =
        round(twoSum(a, b), rounding)

    override fun add(a: DoubleBound, b: DoubleBound, rounding: Rounding) =
        round(twoSum(a, b), rounding).toDoubleBound()

    override fun sub(a: Double, b: Double, rounding: Rounding) =
        round(twoSum(a, -b), rounding)

    override fun sub(a: DoubleBound, b: DoubleBound, rounding: Rounding) =
        round(twoSum(a, (-b)!!), rounding).toDoubleBound()

    override fun mul(a: Double, b: Double, rounding: Rounding) =
        round(ErrorFreeTransforms.twoProd(a, b), rounding)

    override fun div(a: Double, b: Double, rounding: Rounding) = when {
            a == 0.0 && b.isFinite() -> 0.0
            b == 1.0 -> a
            b == -1.0 -> -a
            else -> directedDouble(rounding) { a / b}
        }

    override fun sqrt(x: Double, rounding: Rounding): Double {
        val y = sqrt(x)
        return when (rounding) {
            Rounding.NEAREST -> y
            Rounding.UP      -> if (y * y >= x) y else y.nextUp()
            Rounding.DOWN    -> if (y * y <= x) y else y.nextDown()
            Rounding.TO_ZERO -> if (y >= 0) { if (y * y <= x) y else y.nextDown() } else y
            Rounding.AWAY    -> if (y * y >= x) y else y.nextUp()
        }
    }

    override fun pow(x: Double, exponent: Double, rounding: Rounding): Double {
        return directedBound(rounding) { x.pow(exponent).toDoubleBound() }.toDouble()
    }

    override fun exp(x: Double, rounding: Rounding) =
        directedDouble(rounding) { exp(x) }

    override fun exp(x: DoubleBound, rounding: Rounding): DoubleBound =
        directedBound(rounding) { exp(x.toDouble()).toDoubleBound()!! } !!

    override fun ln(x: Double, rounding: Rounding) =
        directedDouble(rounding) { ln(x) }

    override fun sin(x: Double, rounding: Rounding) =
        directedDouble(rounding) { sin(x) }

    override fun cos(x: Double, rounding: Rounding) =
        directedDouble(rounding) { cos(x) }

    override fun tan(x: Double, rounding: Rounding) =
        directedDouble(rounding) { tan(x) }

    override fun asin(x: Double, rounding: Rounding) =
        directedDouble(rounding) { asin(x) }

    override fun acos(x: Double, rounding: Rounding) =
        directedDouble(rounding) { acos(x) }

    override fun atan(x: Double, rounding: Rounding) =
        directedDouble(rounding) { atan(x) }

    override fun midpoint(a: Double, b: Double, rounding: Rounding): Double  =
        round(midpoint(a,b), rounding)

    override fun midpoint(a: DoubleBound, b: DoubleBound, rounding: Rounding): DoubleBound =
        round(midpoint(a,b), rounding).toDoubleBound()!!

    override fun midpoint(a: DoubleBound, b: DoubleBound): Rounded {
        val s = twoSum(a.finiteValue, b.finiteValue)
        return Rounded(value = s.value * 0.5, error = s.error * 0.5)
    }

    override fun midpoint(a: Double, b: Double): Rounded {
        val s = twoSum(a, b)
        return Rounded(value = s.value * 0.5, error = s.error * 0.5)
    }

    /**
     * Functions with rounding error
     */
    override fun addRounded(a: Double, b: Double) =
        twoSum(a, b)

    override fun subRounded(a: Double, b: Double) =
        twoSum(a, -b)

    override fun mulRounded(a: Double, b: Double) =
        ErrorFreeTransforms.twoProd(a, b)

    /**
     * Applies the requested directed rounding to the computed result.
     */
    private inline fun directedDouble(rounding: Rounding, operation: () -> Double): Double {
        val result = operation()
        return adjust(result, rounding)
    }

    /**
     * Applies the requested directed rounding to the computed result.
     */
    private inline fun directedBound(rounding: Rounding, operation: () -> DoubleBound?): DoubleBound? {
        val result = operation()
        return adjust(result.toDouble(), rounding).toDoubleBound()
    }

    private fun round(r: Rounded, rounding: Rounding): Double =
        adjust(r.value,  rounding,r.error)

    fun Double.nextDown(n: Int): Double {
        var x = this
        repeat(n) { x = x.nextDown() }
        return x
    }

    fun Double.nextUp(n: Int): Double {
        var x = this
        repeat(n) { x = x.nextUp() }
        return x
    }

    /**
     * Adjusts a value according to the requested rounding mode.
     *
     * If [error] is `null`, the exact rounding error is unknown and the result is
     * conservatively adjusted by one ULP in the requested direction.
     *
     * If [error] is specified, the result is only adjusted when the rounding error
     * would otherwise violate the requested rounding mode.
     */
    private fun adjust(value: Double, rounding: Rounding, error: Double? = null): Double {
        if (!value.isFinite() || rounding == Rounding.NEAREST || error == 0.0)
            return value

        return when (rounding) {
            Rounding.DOWN -> when {
                error == null -> value.nextDown()
                error < 0.0 -> value.nextDown()
                else -> value
            }

            Rounding.UP -> when {
                error == null -> value.nextUp()
                error > 0.0 -> value.nextUp()
                else -> value
            }

            Rounding.TO_ZERO -> when {
                value > 0.0 && (error == null || error < 0.0) -> value.nextDown()
                value < 0.0 && (error == null || error > 0.0) -> value.nextUp()
                else -> value
            }

            Rounding.AWAY -> when {
                value > 0.0 && (error == null || error > 0.0) -> value.nextUp()
                value < 0.0 && (error == null || error < 0.0) -> value.nextDown()
                else -> value
            }

            Rounding.NEAREST -> value
        }
    }
}