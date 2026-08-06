package io.github.tukcps.aadd.values.real.rounding

/**
 * Defines the rounding direction for directed floating-point operations.
 *
 * These rounding modes are used by [RoundingMath] to compute conservative
 * bounds for interval and affine arithmetic.
 */
enum class Rounding {
    /** Round to nearest, ties to even. */
    NEAREST,

    /** Round toward zero. */
    TO_ZERO,

    /** Round toward +∞. */
    UP,

    /** Round toward -∞. */
    DOWN,

    /** Round away from zero. */
    AWAY;

    val isDirected: Boolean
        get() = this != NEAREST

    val isMonotone: Boolean
        get() = this == UP || this == DOWN
}