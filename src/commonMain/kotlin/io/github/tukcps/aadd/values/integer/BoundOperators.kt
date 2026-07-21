package io.github.tukcps.aadd.values.integer


/**
 * Unary minus.
 */
operator fun Bound.unaryMinus(): Bound = when (this) {
    is Bound.Finite -> IntegerMath.neg(value)
    Bound.PositiveInfinity -> Bound.NegativeInfinity
    Bound.NegativeInfinity -> Bound.PositiveInfinity
    Bound.NaN -> Bound.NaN
}

/**
 * Absolute value.
 */
fun Bound.abs(): Bound = when (this) {
    is Bound.Finite -> IntegerMath.abs(value)
    Bound.PositiveInfinity,
    Bound.NegativeInfinity -> Bound.PositiveInfinity
    Bound.NaN -> Bound.NaN
}

/**
 * Addition.
 */
operator fun Bound.plus(other: Bound): Bound = when {

    this is Bound.NaN || other is Bound.NaN ->
        Bound.NaN

    this is Bound.Finite && other is Bound.Finite ->
        IntegerMath.add(value, other.value)

    this === Bound.PositiveInfinity && other === Bound.PositiveInfinity ->
        Bound.PositiveInfinity

    this === Bound.NegativeInfinity && other === Bound.NegativeInfinity ->
        Bound.NegativeInfinity

    this === Bound.PositiveInfinity && other === Bound.NegativeInfinity ->
        Bound.NaN

    this === Bound.NegativeInfinity && other === Bound.PositiveInfinity ->
        Bound.NaN

    this === Bound.PositiveInfinity || other === Bound.PositiveInfinity ->
        Bound.PositiveInfinity

    this === Bound.NegativeInfinity || other === Bound.NegativeInfinity ->
        Bound.NegativeInfinity

    else ->
        Bound.NaN
}

/**
 * Subtraction.
 */
operator fun Bound.minus(other: Bound): Bound =
    this + (-other)