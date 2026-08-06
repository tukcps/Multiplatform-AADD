package io.github.tukcps.aadd.values.real.rounding

internal actual fun twoProdImpl(a: Double, b: Double): Rounded {
    val value = a * b
    if (!value.isFinite()) return Rounded(value, 0.0)
    return Rounded(value, FMA.compute(a, b, -value))
}