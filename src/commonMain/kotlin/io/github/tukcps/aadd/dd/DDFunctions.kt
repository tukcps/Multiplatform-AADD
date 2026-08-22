@file:Suppress("UNCHECKED_CAST")
package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.DDBuilder.BoolMath.and
import io.github.tukcps.aadd.DDBuilder.BoolMath.or
import io.github.tukcps.aadd.DDBuilder.IntMath.div
import io.github.tukcps.aadd.DDBuilder.IntMath.minus
import io.github.tukcps.aadd.DDBuilder.IntMath.negate
import io.github.tukcps.aadd.DDBuilder.IntMath.plus
import io.github.tukcps.aadd.DDBuilder.IntMath.times
import io.github.tukcps.aadd.DDBuilder.RealMath.div
import io.github.tukcps.aadd.DDBuilder.RealMath.minus
import io.github.tukcps.aadd.DDBuilder.RealMath.negate
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.times
import io.github.tukcps.aadd.DDException
import io.github.tukcps.aadd.values.real.DoubleBound
import kotlin.math.max

operator fun DD<*>.plus(other: DD<*>): DD<*> = when (this) {
    is AADD if other is AADD -> (this + other)
    is IDD  if other is IDD  -> (this + other)
    is BDD  if other is BDD  -> (this or other)
    else -> throw DDException("Addition of incompatible types.")
}

operator fun DD<*>.minus(other: DD<*>): DD<*> = when (this) {
    is AADD if other is AADD -> builder.realMath { (this@minus) - (other) }
    is IDD if other is IDD   -> this@minus - other
    else -> throw DDException("Subtraction of incompatible types.")
}

operator fun DD<*>.div(other: DD<*>): DD<*> = when(this) {
    is AADD if other is AADD -> (this / other)
    is IDD  if other is IDD  -> (this / other)
    else -> throw DDException("Division of incompatible types.")
}

operator fun DD<*>.times(other: DD<*>): DD<*> = when (this) {
    is AADD if other is AADD -> (this * other)
    is IDD if other is IDD   -> (this * other)
    is BDD if other is BDD   -> (this and other)
    else ->  throw DDException("Multiplication of incompatible types.")
}

operator fun DD<*>.times(other: Double): DD<*>  = when(this) {
    is AADD  -> (this + other)
    is IDD   -> (this + other)
    is StrDD -> (this + other)
    is BDD   -> throw DDException("Comparison of incompatible types.")
}

operator fun DD<*>.div(other: Double): DD<*> = when(this) {
    is AADD  -> (this / other)
    is IDD   -> (this / other)
    else     -> throw DDException("Comparison of incompatible types.")
}

operator fun DD<*>.plus(other: Double): DD<*> = when(this) {
    is AADD  -> (this + other)
    is IDD   -> (this + other)
    is StrDD -> (this + other)
    is BDD   -> throw DDException("Comparison of incompatible types.")
}

operator fun DD<*>.minus(other: Double): DD<*> = when(this) {
    is AADD  -> this - other
    is IDD   -> this - other
    else     -> throw DDException("Comparison of incompatible types.")
}

infix fun DD<*>.lessThan(other: DD<*>): BDD {
    if (this is AADD && other is AADD) return this lessThan other
    if (this is IDD  && other is IDD ) return this lessThan other
    else throw DDException("Comparison of incompatible types.")
}

infix fun DD<*>.lessThanOrEquals(other: DD<*>): BDD {
    if (this is AADD && other is AADD) return this lessThanOrEquals other
    if (this is IDD  && other is IDD ) return this lessThanOrEquals other
    else throw DDException("Comparison of incompatible types.")
}

infix fun DD<*>.greaterThan(other: DD<*>): BDD {
    if (this is AADD && other is AADD) return this greaterThan other
    if (this is IDD  && other is IDD ) return this greaterThan other
    else throw DDException("Comparison of incompatible types.")
}

infix fun DD<*>.greaterThanOrEquals(other: DD<*>): BDD {
    if (this is AADD && other is AADD) return this greaterThanOrEquals other
    if (this is IDD && other is IDD ) return this greaterThanOrEquals other
    else throw DDException("Comparison of incompatible types.")
}

operator fun DD<*>.unaryMinus(): DD<*> = when(this) {
    is AADD -> negate(this)
    is IDD  -> negate(this)
    else    -> throw DDException("Unary minus on incompatible type.")
}

fun DD<*>.ite(t: DD<*>, e: DD<*>): DD<*> = when(this) {
    builder.Bool.Empty if (t::class == e::class) -> t.empty
    builder.Bool.One if (t::class == e::class) -> t.one
    builder.Bool.Zero if (t::class == e::class) -> e.zero
    else -> when (t) {
        is AADD if e is AADD -> (this as BDD).ite(t, e)
        is BDD if e is BDD   -> (this as BDD).ite(t, e)
        is IDD if e is IDD   -> (this as BDD).ite(t, e)
        is StrDD if e is StrDD -> (this as BDD).ite(t, e)
        else     -> throw DDException("ite on incompatible types.")
    }
}

/**
 * Calls the intersect functions of different kind of DD types
 * @param other: second parameter
 */
infix fun DD<*>.intersect(other: DD<*>): DD<*> =
    when (this) {
        is AADD -> this intersect other as AADD
        is BDD -> this intersect other as BDD
        is IDD -> this intersect other as IDD
        is StrDD -> this intersect other as StrDD
    }

operator fun DD<*>.contains(x : Long) = when(this) {
    is AADD -> min <= DoubleBound.Finite(x.toDouble()) && max >= DoubleBound.Finite(x.toDouble())
    is IDD ->  min<= x && max >= x
    else -> false
}

/**
 * Returns number of internal nodes in a BDD.
 */
fun DD<*>.numInternalNodes(node: DD<*> = this): Int = when(node) {
    is DD.Leaf<*> -> 0
    is DD.Internal<*> -> 1 + numInternalNodes(node.T) + numInternalNodes(node.F)
}

/**
 * Returns number of unknown variables;
 * is wrong I believe as max does not consider that T and F can have disjoint conditions.
 */
fun DD<*>.numUnknownVars(node: DD<*> = this): Int = when(node) {
    is DD.Leaf<*> ->  0
    is DD.Internal<*> -> max(node.index, max(numUnknownVars(node.T), numUnknownVars(node.F)))
}



// fun DD<*>.structurallyEquals(dd: DD<*>, other: DD<*>): Boolean =
//     structurallyEquals(dd, other)
