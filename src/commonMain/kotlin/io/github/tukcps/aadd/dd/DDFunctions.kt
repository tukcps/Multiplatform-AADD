@file:Suppress("UNCHECKED_CAST")
package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.DDBuilder.BoolMath.and
import io.github.tukcps.aadd.DDBuilder.BoolMath.or
import io.github.tukcps.aadd.DDBuilder.IntMath.minus
import io.github.tukcps.aadd.DDBuilder.IntMath.negate
import io.github.tukcps.aadd.DDBuilder.IntMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.div
import io.github.tukcps.aadd.DDBuilder.RealMath.minus
import io.github.tukcps.aadd.DDBuilder.RealMath.negate
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.times
import io.github.tukcps.aadd.DDException
import kotlin.math.max


operator fun <DDType: DD<*>> DDType.plus(other: DDType): DDType = when (this) {
    is AADD if other is AADD -> (this + other)
    is IDD  if other is IDD  -> (this + other)
    is BDD  if other is BDD  -> (this or other)
    else -> throw DDException("Addition of incompatible types.")
} as DDType

operator fun <DDType: DD<*>> DDType.minus(other: DDType): DDType = when (this) {
    is AADD if other is AADD -> builder.realMath { (this@minus) - (other) }
    is IDD if other is IDD   -> this@minus - other
    else -> throw DDException("Subtraction of incompatible types.")
} as DDType

operator fun <DDType: DD<*>> DDType.div(other: DDType): DDType = when(this) {
    is AADD if other is AADD -> (this / other) as DDType
    is IDD  if other is IDD  -> (this / other) as DDType
    else -> throw DDException("Division of incompatible types.")
}

operator fun <DDType: DD<*>> DDType.times(other: DDType): DDType = when (this) {
    is AADD if other is AADD -> (this * other)
    is IDD if other is IDD   -> (this * other)
    is BDD if other is BDD   -> (this and other)
    else ->  throw DDException("Multiplication of incompatible types.")
} as DDType

operator fun <DDType: DD<*>> DDType.times(other: Double): DDType  = when(this) {
    is AADD  -> (this + other)
    is IDD   -> (this + other)
    is StrDD -> (this + other)
    is BDD   -> throw DDException("Comparison of incompatible types.")
} as DDType

operator fun <DDType: DD<*>> DDType.div(other: Double): DDType = when(this) {
    is AADD  -> (this / other)
    is IDD   -> (this / other)
    else     -> throw DDException("Comparison of incompatible types.")
} as DDType

operator fun <DDType: DD<*>> DDType.plus(other: Double): DDType = when(this) {
    is AADD  -> (this + other)
    is IDD   -> (this + other)
    is StrDD -> (this + other)
    is BDD   -> throw DDException("Comparison of incompatible types.")
} as DDType

operator fun <DDType: DD<*>> DDType.minus(other: Double): DDType = when(this) {
    is AADD  -> this - other
    is IDD   -> this - other
    else     -> throw DDException("Comparison of incompatible types.")
} as DDType

infix fun <DDType: DD<*>> DDType.lessThan(other: DDType): BDD {
    if (this is AADD && other is AADD) return this lessThan other
    if (this is IDD  && other is IDD ) return this lessThan other
    else throw DDException("Comparison of incompatible types.")
}

infix fun <DDtype: DD<*>> DDtype.lessThanOrEquals(other: DDtype): BDD {
    if (this is AADD && other is AADD) return this lessThanOrEquals other
    if (this is IDD  && other is IDD ) return this lessThanOrEquals other
    else throw DDException("Comparison of incompatible types.")
}

infix fun <DDType: DD<*>> DDType.greaterThan(other: DDType): BDD {
    if (this is AADD && other is AADD) return this greaterThan other
    if (this is IDD  && other is IDD ) return this greaterThan other
    else throw DDException("Comparison of incompatible types.")
}

infix fun <T: DD<*>> T.greaterThanOrEquals(other: T): BDD {
    if (this is AADD && other is AADD) return this greaterThanOrEquals other
    if (this is IDD && other is IDD ) return this greaterThanOrEquals other
    else throw DDException("Comparison of incompatible types.")
}

operator fun <DDType: DD<*>> DDType.unaryMinus(): DDType = when(this) {
    is AADD -> negate(this)
    is IDD  -> negate(this)
    else    -> throw DDException("Unary minus on incompatible type.")
} as DDType

fun <DDType: DD<*>> BDD.ite(t: DDType, e: DDType): DDType = when(this) {
    builder.Bool.Empty -> t.empty
    builder.Bool.One -> t.one
    builder.Bool.Zero -> e.zero
    else -> when (t) {
        is AADD -> ite(t, e as AADD)
        is BDD -> ite(t, e as BDD)
        is IDD  -> ite(t, e as IDD)
        is StrDD -> ite(t, e as StrDD)
    }
} as DDType

/**
 * Calls the intersect functions of different kind of DD types
 * @param other: second parameter
 */
infix fun <DDType: DD<*>> DDType.intersect(other: DDType): DDType {
    return when (this) {
        is AADD -> this intersect other as AADD
        is BDD -> this intersect other as BDD
        is IDD -> this intersect other as IDD
        is StrDD -> this intersect other as StrDD
    } as DDType
}

/**
 * Returns number of internal nodes in a BDD.
 */
fun <DDType: DD<*>> DDType.numInternalNodes(node: DDType = this): Int = when(node) {
    is DD.Leaf<*> -> 0
    is DD.Internal<*> -> 1 + numInternalNodes(node.T) + numInternalNodes(node.F)
}

/**
 * Returns number of unknown variables;
 * is wrong I believe as max does not consider that T and F can have disjoint conditions.
 */
fun <DDType: DD<*>> DDType.numUnknownVars(node: DDType = this): Int = when(node) {
    is DD.Leaf<*> ->  0
    is DD.Internal<*> -> max(node.index, max(numUnknownVars(node.T), numUnknownVars(node.F)))
}

fun <DDType: DD<*>> structurallyEquals(dd: DDType, other: DDType): Boolean =
    structurallyEquals(dd, other)
