package io.github.tukcps.aadd.values.integer

import io.github.tukcps.aadd.values.NumberRange
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * Calculate nth root of an integer (nth root of x = x^(1/n))
 */
fun IntegerRange.root(other: NumberRange<Long>): IntegerRange {
    val lb: Long
    val ub: Long
    if (other.min >= 0) {
        lb = floor(asReal(min).pow(1 / asReal(other.max))).toLong()
        ub = ceil(asReal(max).pow(1 / asReal(other.min))).toLong()
    } else if (other.max < 0) {
        lb = floor(asReal(min).pow(1 / asReal(other.min))).toLong()
        ub = ceil(asReal(max).pow(1 / asReal(other.max))).toLong()
    } else { // other.min<0 and other.max>=0
        lb = floor(asReal(min).pow(1 / asReal(other.min))).toLong()
        ub = ceil(asReal(max).pow(1 / asReal(other.min))).toLong()
    }
    return IntegerRange(lb, ub)
}