package com.github.tukcps.aadd.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.Range
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


fun power(a: AADD.Leaf, b: AADD.Leaf): AADD = a.builder.leaf( (b.value*a.value.log()).exp())

/** Intersection of a leaf and an interval returns a leaf.*/
fun constraintTo(leaf: AADD.Leaf, range: ClosedFloatingPointRange<Double>) =
    constrainTo(leaf, Range(range))

/**
 * Intersection of a leaf and an interval returns a leaf.
 */
fun constrainTo(leaf: AADD.Leaf, range: Range): AADD.Leaf {

    // Scalars
    if (leaf.isScalar() && leaf.min in range) return leaf.builder.real(leaf.min) as AADD.Leaf
    if (range.isScalar() && range.start in leaf.getRange()) return leaf.builder.real(range.start) as AADD.Leaf

    // range is in this, so we return range as new AF
    if ((leaf.min < range.start) && (range.endInclusive < leaf.max)) {
        return if (leaf.value.xi.isEmpty()) leaf.builder.real(range.min .. range.max)
        else leaf.copy(range.start, range.endInclusive) as AADD.Leaf
    }

    // complete inclusion of this in range; we return this
    if ((range.start <= leaf.min) && range.endInclusive >= leaf.max)
        return leaf.clone() as AADD.Leaf

    // no complete inclusion, so we create an AADD with range constraints
    var newMin = max(leaf.min, range.start)
    var newMax = min(leaf.max, range.endInclusive)
    val result = if (newMin == newMax)
        leaf.builder.real(newMin) as AADD.Leaf
    else
        leaf.copy(newMin, newMax) as AADD.Leaf
    return result
}



/**
 * Intersection of two AADDLeaves returns an AADD or a Leaf
 */
fun intersect(a: AADD.Leaf, b: AADD.Leaf): AADD {
    // If this is just a range ...
    val result: AADD
    if (a.value.xi.isEmpty()) result = b constrainTo a.value
    else if (b.value.xi.isEmpty()) result = a constrainTo b.value
    else if (a.minIsInf && a.maxIsInf)  result = b
    else if (b.minIsInf && b.maxIsInf)  result = a
    else {
        // avoid creation of unnecessary precise ITE in case of nearly similar AADD
        if (a.value.isSimilar(b.value, 0.00000001)) {
            result = a.copy(min = min(a.min, b.min), max = max(a.max, b.max), a.value.r+0.000001)
        } else {
            val smaller = if (abs(a.min - a.max) < abs(b.min - b.max)) a else b
            result =
                if (smaller === a)
                    ((a greaterThanOrEquals b) and (a lessThanOrEquals b)).ite(a, a.builder.Empty)
                else
                    ((b greaterThanOrEquals a) and (b lessThanOrEquals a)).ite(b, a.builder.Empty)
            result.getRange()
        }
    }
    return result
}



/**  Extension Functions developed by Jack **/
fun ceil(input : AADD) : AADD = input.ceil()
fun invCeil(input : AADD) : AADD = input.invCeil()
fun floor(input : AADD) : AADD = input.floor()
fun invFloor(input : AADD) : AADD = input.invFloor()

/**
 * Calculates  f(x,y) = x^y
 * @param base : AADD  = the base, or number, which is being raised to an exponent, i.e., multiplied by itself that number of times
 * @param exponent : Double = the exponential power that the base is being raised to.
 */
fun pow(base : AADD, exponent : Double) : AADD = base.pow(exponent)

/**
 * Calculates  f(x,y) = x^y
 * @param base : AADD = the base, or number, which is being raised to an exponent, i.e., multiplied by itself that number of times
 * @param exponent : AffineForm = the exponential power that the base is being raised to.
 */
fun pow(base : AADD, exponent : AffineForm) : AADD = base.pow(exponent)

/**
 * Calculates  f(x,y) = x^y
 * @param base : AADD = the base, or number, which is being raised to an exponent, i.e., multiplied by itself that number of times
 * @param exponent : AffineForm = the exponential power that the base is being raised to.
 */
fun pow(base : AADD, exponent : AADD) : AADD = base.pow(exponent)

/**
 * Calculates log_base(arg)
 * @param base: A double number of the base
 * @param arg: An AADD
 */
fun log(base : Double, arg : AADD) : AADD = arg.log(base)

/**
 * Calculates the sum over i.
 */
fun sum_i(builder: DDBuilder, i : Int = 0, N : Int, list: MutableList<AADD>) : AADD {
    var sum : AADD = builder.leaf(AffineForm(builder, 0.0, 0.0))

    try {
        for (index in i..N)
            sum = sum + list[index]
    }
    catch(ioobe : IndexOutOfBoundsException) {
        println("i = " + i)
        println("N = " + N)
        println("message = "+ ioobe.message)
        println("cause = " + ioobe.cause)
        ioobe.printStackTrace()
    }

    return sum
}

fun sum_i(builder: DDBuilder, N : Int, list: MutableList<AADD>) : AADD = sum_i(builder, 0, N, list)