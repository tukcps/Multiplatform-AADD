package io.github.tukcps.aadd.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.AADD.Internal
import io.github.tukcps.aadd.AADD.Leaf
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.pwl.relu
import io.github.tukcps.aadd.values.real.AffineForm
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.RealRange
import io.github.tukcps.aadd.values.real.root
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


fun power(a: Leaf, b: Leaf): AADD = a.builder.leaf( (b.value*a.value.log()).exp())

/** Intersection of a leaf and an interval returns a leaf.*/
fun constraintTo(leaf: Leaf, range: ClosedFloatingPointRange<Double>) =
    constrainTo(leaf, RealRange(range))

/**
 * Intersection of a leaf and an interval returns a leaf.
 */
fun constrainTo(leaf: Leaf, realRange: RealRange): Leaf {

    // Scalars
    if (leaf.isScalar() && leaf.min in realRange) return leaf.builder.real(leaf.min) as Leaf
    if (realRange.isScalar() && realRange.start in leaf.getRange()) return leaf.builder.real(realRange.start) as Leaf

    // range is in this, so we return range as new AF
    if ((leaf.min < realRange.start) && (realRange.endInclusive < leaf.max)) {
        return if (leaf.value.xi.isEmpty()) leaf.builder.real(realRange.min .. realRange.max)
        else leaf.copy(realRange.start, realRange.endInclusive) as Leaf
    }

    // complete inclusion of this in range; we return this
    if ((realRange.start <= leaf.min) && realRange.endInclusive >= leaf.max)
        return leaf.clone() as Leaf

    // no complete inclusion, so we create an AADD with range constraints
    val newMin = max(leaf.min, realRange.start)
    val newMax = min(leaf.max, realRange.endInclusive)
    val result = if (newMin == newMax)
        leaf.builder.real(newMin) as Leaf
    else
        leaf.copy(newMin, newMax) as Leaf
    return result
}

/**
 * Intersection of two AADDLeaves returns an AADD or a Leaf
 */
fun intersect(a: Leaf, b: Leaf): AADD {
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
            sum += list[index]
    }
    catch(ioobe : IndexOutOfBoundsException) {
        println("i = $i")
        println("N = $N")
        println("message = "+ ioobe.message)
        println("cause = " + ioobe.cause)
        ioobe.printStackTrace()
    }

    return sum
}

fun sum_i(builder: DDBuilder, N : Int, list: MutableList<AADD>) : AADD = sum_i(builder, 0, N, list)

fun AADD.root(other: NumberRange<Double>) =
    this.apply { x: Leaf -> builder.leaf(x.value.root(builder.real(other))) }

fun AADD.relu(splitThreshold : Double = 0.1) : AADD = when(this){
    is Internal -> builder.internal(index,T.relu(),F.relu())
    is Leaf -> relu(value, builder, splitThreshold)
}

/**
 * Calls LP solver and returns the resulting range as IntegerRange.
 */
fun AADD.toIntegerRange(): IntegerRange {
    val range = getRange()
    return IntegerRange(range.min, range.max)
}


/** Overloaded contains operation for allowing "AADD in range" notation */
operator fun ClosedFloatingPointRange<Double>.contains(other: AADD): Boolean =
    when (other) {
        is AADD.Leaf       -> other.max <= endInclusive && other.min >= start
        is AADD.Internal   -> this.contains(other.T) || this.contains(other.F)
    }

