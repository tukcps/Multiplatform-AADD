package io.github.tukcps.aadd.values.integer

/**
 * Returns the absolute value of a long bound.
 * @param x the operand
 * @return the absolute value
 */
fun abs(x: LongBound): LongBound =
    LongMath.abs(x)

/**
 * Returns the arithmetic negation of this bound.
 *
 * @receiver the operand
 * @return the negated bound
 */
operator fun LongBound.unaryMinus(): LongBound =
    LongMath.negate(this)


/**
 * Adds two integer bounds.
 *
 * @receiver the left operand
 * @param other the right operand
 * @return the sum of both operands
 */
operator fun LongBound.plus(other: LongBound): LongBound? =
    LongMath.add(this, other)

/**
 * Adds a Long to an integer bounds.
 * The Long is by nature finite.
 * @receiver the left operand
 * @param other the right operand
 * @return the sum of both operands
 */
operator fun LongBound.plus(other: Long): LongBound? =
    LongMath.add(this, LongBound.Finite(other))

/**
 * Subtracts one integer bound from another.
 *
 * @receiver the left operand
 * @param other the right operand
 * @return the difference
 */
operator fun LongBound.minus(other: LongBound): LongBound? =
    LongMath.subtract(this, other)

/**
 * Subtracts a Long from an integer bound.
 *
 * @receiver the left operand
 * @param other the right operand
 * @return the difference
 */
operator fun LongBound.minus(other: Long): LongBound? =
    LongMath.subtract(this, LongBound.Finite(other))

/**
 * Multiplies an integer bounds with a Long.
 *
 * @receiver the left operand
 * @param other the right operand
 * @return the product
 */
operator fun LongBound.times(other: Long): LongBound? =
    LongMath.multiply(this, LongBound.Finite(other))

/**
 * Multiplies two integer bounds.
 * @receiver the left operand
 * @param other the right operand
 * @return the product
 */
operator fun LongBound.times(other: LongBound): LongBound? =
    LongMath.multiply(this, other)

/**
 * Divides one integer bound by another.
 * @receiver the dividend
 * @param other the divisor
 * @return the quotient
 */
operator fun LongBound.div(other: Long): LongBound? =
    LongMath.divide(this, LongBound.Finite(other))

/**
 * Divides one integer bound by a Long.
 * @receiver the dividend
 * @param other the divisor
 * @return the quotient
 */
operator fun LongBound.div(other: LongBound): LongBound? =
    LongMath.divide(this, other)

/**
 * Returns the smaller of two bounds.
 * @receiver the first bound
 * @param other the second bound
 * @return the smaller bound
 */
fun LongBound.min(other: LongBound): LongBound =
    LongMath.min(this, other)

/**
 * Returns the larger of two bounds.
 * @receiver the first bound
 * @param other the second bound
 * @return the larger bound
 */
fun LongBound.max(other: LongBound): LongBound =
    LongMath.max(this, other)

/**
 * Returns the smallest bound from the given values.
 *
 * @param bounds the bounds to compare
 * @return the smallest bound
 */
fun minOf(vararg bounds: LongBound): LongBound =
    bounds.reduce(LongMath::min)

/**
 * Returns the largest bound from the given values.
 *
 * @param bounds the bounds to compare
 * @return the largest bound
 */
fun maxOf(vararg bounds: LongBound): LongBound =
    bounds.reduce(LongMath::max)