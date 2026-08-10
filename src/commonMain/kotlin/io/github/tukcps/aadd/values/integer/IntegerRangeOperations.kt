package io.github.tukcps.aadd.values.integer

/**
 * ### Arithmetic operators for [IntegerRange].
 *
 * Every operator returns the smallest convex integer subset containing all
 * possible results of the corresponding arithmetic operation.
 */
object DummyToRenderDoc

//
// ------------------- Overloaded operators -------------------------
//
operator fun IntegerRange.unaryMinus(): IntegerRange = negate(this)
operator fun IntegerRange.plus(other: IntegerRange): IntegerRange = add(this, other)
operator fun IntegerRange.minus(other: IntegerRange): IntegerRange = subtract(this, other)
operator fun IntegerRange.times(other: IntegerRange): IntegerRange = multiply(this, other)
operator fun IntegerRange.div(other: IntegerRange): IntegerRange  = divide(this, other)

/**
 * Returns the remainder after division.
 */
operator fun IntegerRange.rem(other: IntegerRange): IntegerRange =
    TODO("Interval remainder")

//--------------------------------------------------
//               Overloaded operators
//--------------------------------------------------
operator fun IntegerRange.plus(value: Long): IntegerRange = this + IntegerRange(value)
operator fun IntegerRange.plus(value: Int): IntegerRange = this + IntegerRange(value.toLong())
operator fun Long.plus(range: IntegerRange): IntegerRange = IntegerRange(this) + range

operator fun IntegerRange.minus(value: Long): IntegerRange = this - IntegerRange(value)
operator fun IntegerRange.minus(value: Int): IntegerRange = this - IntegerRange(value.toLong())
operator fun Long.minus(range: IntegerRange): IntegerRange = IntegerRange(this) - range

operator fun IntegerRange.times(value: Long): IntegerRange = this * IntegerRange(value)
operator fun IntegerRange.times(value: Int): IntegerRange = this * IntegerRange(value.toLong())
operator fun Long.times(range: IntegerRange): IntegerRange = IntegerRange(this) * range

operator fun IntegerRange.div(value: Long): IntegerRange = this / IntegerRange(value)
operator fun IntegerRange.div(value: Int): IntegerRange = this / IntegerRange(value.toLong())
operator fun Long.div(range: IntegerRange): IntegerRange = IntegerRange(this) / range

operator fun IntegerRange.rem(value: Long): IntegerRange = this % IntegerRange(value)
operator fun Long.rem(range: IntegerRange): IntegerRange = IntegerRange(this) % range