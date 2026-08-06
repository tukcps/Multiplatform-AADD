package io.github.tukcps.aadd.values

/**
 * A bound on an ordered numeric domain.
 *
 * A bound may represent
 *
 * * a finite value,
 * * positive infinity,
 * * negative infinity.
 *
 * Concrete implementations are provided by
 * `LongBound` and `DoubleBound`.
 *
 * The total ordering is
 *
 *     -∞ < finite values < +∞ < NaN
 */
interface Bound : Comparable<Bound> {

    /**
     * Returns whether this bound is finite.
     */
    val isFinite: Boolean

    /**
     * Returns whether this bound is infinite.
     */
    val isInfinite: Boolean

    /**
     * Returns whether this bound represents positive infinity.
     */
    val isPositiveInfinity: Boolean

    /**
     * Returns whether this bound represents negative infinity.
     */
    val isNegativeInfinity: Boolean

    /**
     * Returns whether this bound represents zero.
     */
    val isZero: Boolean

    /**
     * Returns whether this bound represents one.
     */
    val isOne: Boolean

}