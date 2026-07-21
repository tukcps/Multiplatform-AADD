package io.github.tukcps.aadd.values.real.math

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
        round(ErrorFreeTransforms.twoSum(a, b), rounding)

    override fun sub(a: Double, b: Double, rounding: Rounding) =
        round(ErrorFreeTransforms.twoSum(a, -b), rounding)

    override fun mul(a: Double, b: Double, rounding: Rounding) =
        round(ErrorFreeTransforms.twoProd(a, b), rounding)

    override fun div(a: Double, b: Double, rounding: Rounding) =
        directed(rounding) { a / b }

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

    override fun exp(x: Double, rounding: Rounding) =
        directed(rounding) { exp(x) }

    override fun log(x: Double, rounding: Rounding) =
        directed(rounding) { ln(x) }

    override fun sin(x: Double, rounding: Rounding) =
        directed(rounding) { sin(x) }

    override fun cos(x: Double, rounding: Rounding) =
        directed(rounding) { cos(x) }

    override fun tan(x: Double, rounding: Rounding) =
        directed(rounding) { tan(x) }

    override fun asin(x: Double, rounding: Rounding) =
        directed(rounding) { asin(x) }

    override fun acos(x: Double, rounding: Rounding) =
        directed(rounding) { acos(x) }

    override fun atan(x: Double, rounding: Rounding) =
        directed(rounding) { atan(x) }

    /**
     * Functions with rounding error
     */
    override fun addRounded(a: Double, b: Double) =
        ErrorFreeTransforms.twoSum(a, b)

    override fun subRounded(a: Double, b: Double) =
        ErrorFreeTransforms.twoSum(a, -b)

    override fun mulRounded(a: Double, b: Double) =
        ErrorFreeTransforms.twoProd(a, b)

    /**
     * Applies the requested directed rounding to the computed result.
     */
    private inline fun directed(rounding: Rounding, operation: () -> Double): Double {
        val result = operation()
        return adjust(result, rounding)
    }

    private fun round(r: Rounded, rounding: Rounding): Double {
        if (!r.value.isFinite() || rounding == Rounding.NEAREST || r.exact)
            return r.value

        return when (rounding) {
            Rounding.DOWN ->
                if (r.error < 0.0) r.value.nextDown() else r.value

            Rounding.UP ->
                if (r.error > 0.0) r.value.nextUp() else r.value

            Rounding.TO_ZERO ->
                when {
                    r.value > 0.0 && r.error < 0.0 -> r.value.nextDown()
                    r.value < 0.0 && r.error > 0.0 -> r.value.nextUp()
                    else -> r.value
                }

            Rounding.AWAY ->
                when {
                    r.value > 0.0 && r.error > 0.0 -> r.value.nextUp()
                    r.value < 0.0 && r.error < 0.0 -> r.value.nextDown()
                    else -> r.value
                }

            Rounding.NEAREST ->
                r.value
        }
    }

    /**
     * Returns the next representable value greater than or equal to this value.
     */
    private fun Double.roundUp(): Double = nextUp()

    /**
     * Returns the next representable value less than or equal to this value.
     */
    private fun Double.roundDown(): Double = nextDown()

    private fun adjust(value: Double, rounding: Rounding): Double {
        if (!value.isFinite()) return value

        return when (rounding) {
            Rounding.DOWN    -> value.nextDown()
            Rounding.NEAREST -> value
            Rounding.UP      -> value.nextUp()
            Rounding.TO_ZERO ->
                if (value > 0.0) value.nextDown()
                else if (value < 0.0) value.nextUp()
                else value
            Rounding.AWAY ->
                if (value > 0.0) value.nextUp()
                else if (value < 0.0) value.nextDown()
                else value
        }
    }
}