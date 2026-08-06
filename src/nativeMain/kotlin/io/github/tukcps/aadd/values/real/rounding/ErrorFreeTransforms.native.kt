package io.github.tukcps.aadd.values.real.rounding


internal actual fun twoProdImpl(a: Double, b: Double): Rounded {
    val value = a * b
    if (!value.isFinite()) return Rounded(value, 0.0)

    val sa = ErrorFreeTransforms.split(a)
    val sb = ErrorFreeTransforms.split(b)

    val error =
        ((sa.hi * sb.hi - value)
                + sa.hi * sb.lo
                + sa.lo * sb.hi)+ sa.lo * sb.lo

    return Rounded(value, error)
}