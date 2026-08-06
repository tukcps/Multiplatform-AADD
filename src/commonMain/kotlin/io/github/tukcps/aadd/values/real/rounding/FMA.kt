package io.github.tukcps.aadd.values.real.rounding

internal expect object FMA {
    /**
     * Computes the fused multiply-add operation {@code a * b + c} with a single
     * final rounding step, minimizing intermediate rounding errors.
     *
     * @return {@code a * b + c}
     */
    fun compute(a: Double, b: Double, c: Double): Double
}