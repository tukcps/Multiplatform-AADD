package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.DDBuilder.RealMath.multiply
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble

operator fun AffineForm.unaryMinus() = negate(this)

/**
 * Adds two affine forms.
 */
operator fun AffineForm.plus(other: AffineForm): AffineForm = add(this, other)
operator fun AffineForm.plus(other: Double): AffineForm = add(this, other)
operator fun Double.plus(other: AffineForm): AffineForm = add(other, this)

/**
 * Subtracts two affine forms.
 */
operator fun AffineForm.minus(other: AffineForm): AffineForm = subtract(this, other)

/** Subtracts a (possibly negative) scalar from an affine form. */
operator fun AffineForm.minus(other: Double): AffineForm = add(this, -other)
operator fun Double.minus(other: AffineForm): AffineForm = add(negate(other), this)

/**
 * Operator '*'
 */
operator fun AffineForm.times(other: AffineForm): AffineForm = multiply(this, other)
operator fun AffineForm.times(other: Double): AffineForm = multiply(this, other)
operator fun Double.times(other: AffineForm): AffineForm = multiply(other, this)
operator fun AffineForm.times(other: DoubleBound): AffineForm = multiply(this, other.toDouble())
operator fun DoubleBound.times(other: AffineForm): AffineForm = multiply(other, this.toDouble())

/**
 * We do division by multiplying by inv(other) as suggested by Stolfi.
 * Division by zero returns infinity.
 */
operator fun AffineForm.div(other: AffineForm): AADD = multiply(other.builder.leaf(this), invSplit(other))
operator fun AffineForm.div(other: Double): AffineForm = multiply(this, 1 / other)
operator fun Double.div(other: AffineForm): AADD = multiply(invSplit(other), this)
