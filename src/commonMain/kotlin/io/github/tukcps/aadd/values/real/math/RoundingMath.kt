package io.github.tukcps.aadd.values.real.math

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
     * Returns the directed difference of [a] and [b].
     */
    fun sub(a: Double, b: Double, rounding: Rounding): Double

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

    /**
     * Returns the directed natural logarithm of [x].
     */
    fun log(x: Double, rounding: Rounding): Double

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

    fun addRounded(a: Double, b: Double): Rounded
    fun subRounded(a: Double, b: Double): Rounded
    fun mulRounded(a: Double, b: Double): Rounded
}