package io.github.tukcps.aadd.values.real

import io.github.tukcps.aadd.values.NumberRange
import kotlin.math.max

fun RealRange.root(other: NumberRange<Double>): NumberRange<Double> =
    TODO("Not yet implemented")


/**
 * overloaded contains-operator to allow "in" range notation
 */
operator fun ClosedFloatingPointRange<Double>.contains(range: ClosedFloatingPointRange<Double>): Boolean {
    if (this.start > range.start) return false
    if (this.endInclusive < range.endInclusive) return false
    return true
}

/**
 * Quick and dirty relu implementation over real valued intervals
 * */
fun RealRange.relu() : RealRange = RealRange(max(0.0, this.min), max(0.0, this.max))


fun ceil(input : RealRange) : RealRange = input.ceil()

fun invCeil(input : RealRange) : RealRange = input.invCeil()

fun floor(input : RealRange) : RealRange = input.floor()

fun invFloor(input : RealRange) : RealRange = input.invFloor()