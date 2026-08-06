package io.github.tukcps.aadd.values.real.rounding

import io.github.tukcps.aadd.values.real.DoubleBound

/**
 * Provides directed IEEE-754 floating-point operations.
 *
 * Each operation returns a floating-point result rounded according to the
 * requested [Rounding] mode. These operations form the numerical foundation
 * for interval and affine arithmetic.
 *
 * Implementations must produce conservative results for directed rounding and
 * may use platform-specific optimizations while preserving the specified
 * rounding semantics.
 */
interface RoundingMath {

    /**
     * Returns the directed sum of [a] and [b].
     */
    fun add(a: Double, b: Double, rounding: Rounding): Double

    /**
     * Returns the directed sum of [a] and [b], or null if there is no result (e.g., NaN)
     */
    fun add(a: DoubleBound, b: DoubleBound, rounding: Rounding): DoubleBound?

    /**
     * Returns the directed difference of [a] and [b].
     */
    fun sub(a: Double, b: Double, rounding: Rounding): Double

    /**
     * Returns the directed difference of [a] and [b], or null if there is no result (e.g., NaN)
     */
    fun sub(a: DoubleBound, b: DoubleBound, rounding: Rounding): DoubleBound?

    /**
     * Returns the directed product of [a] and [b].
     */
    fun mul(a: Double, b: Double, rounding: Rounding): Double

    /**
     * Returns the directed quotient of [a] and [b].
     */
    fun div(a: Double, b: Double, rounding: Rounding): Double

    /**
     * Returns the directed square root of [x].
     */
    fun sqrt(x: Double, rounding: Rounding): Double

    /**
     * Returns the directed exponential function of [x].
     */
    fun exp(x: Double, rounding: Rounding): Double
    fun exp(x: DoubleBound, rounding: Rounding): DoubleBound

    /**
     * Returns the directed natural logarithm of [x].
     */
    fun ln(x: Double, rounding: Rounding): Double

    /**
     * Returns `x^exponent`
     */
    fun pow(x: Double, exponent: Double, rounding: Rounding): Double

    /**
     * Returns the directed sine of [x].
     */
    fun sin(x: Double, rounding: Rounding): Double

    /**
     * Returns the directed cosine of [x].
     */
    fun cos(x: Double, rounding: Rounding): Double

    /**
     * Returns the directed tangent of [x].
     */
    fun tan(x: Double, rounding: Rounding): Double

    /**
     * Returns the directed arc sine of [x].
     */
    fun asin(x: Double, rounding: Rounding): Double

    /**
     * Returns the directed arc cosine of [x].
     */
    fun acos(x: Double, rounding: Rounding): Double

    /**
     * Returns the directed arc tangent of [x].
     */
    fun atan(x: Double, rounding: Rounding): Double

    /**
     * Returns the midpoint of two values with minimized rounding error,
     * and considering overflow.
     * @param a first value
     * @param b second value
     * @return (a+b) / 2
     */
    fun midpoint(a: Double, b: Double, rounding: Rounding): Double
    fun midpoint(a: DoubleBound, b: DoubleBound, rounding: Rounding): DoubleBound?
    fun midpoint(a: Double, b: Double): Rounded
    fun midpoint(a: DoubleBound, b: DoubleBound): Rounded
    fun addRounded(a: Double, b: Double): Rounded
    fun subRounded(a: Double, b: Double): Rounded
    fun mulRounded(a: Double, b: Double): Rounded
}