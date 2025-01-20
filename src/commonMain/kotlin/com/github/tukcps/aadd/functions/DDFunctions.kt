package com.github.tukcps.aadd.functions

import com.github.tukcps.aadd.*
import kotlin.math.max

operator fun DD<*>.plus(other: DD<*>): DD<*> {
    if (this is AADD && other is AADD) return this + other
    if (this is IDD && other is IDD ) return this + other
    else throw DDException("Addition of incompatible types.")
}

operator fun DD<*>.minus(other: DD<*>): DD<*> {
    if (this is AADD && other is AADD) return this - other
    if (this is IDD  && other is IDD ) return this - other
    else throw DDException("Subtraction of incompatible types.")
}

operator fun DD<*>.div(other: DD<*>): DD<*> {
    if (this is AADD && other is AADD) return this / other
    if (this is IDD  && other is IDD ) return this / other
    else throw DDException("Division of incompatible types.")
}

operator fun DD<*>.times(other: DD<*>): DD<*> {
    if (this is AADD && other is AADD) return this * other
    if (this is IDD  && other is IDD ) return this * other
    else throw DDException("Multiplication of incompatible types.")
}

operator fun DD<*>.times(other: Double): DD<*> {
    if (this is AADD) return this * other
    if (this is IDD) return this * other
    else throw DDException("Multiplication of incompatible types.")
}

operator fun DD<*>.div(other: Double): DD<*> {
    if (this is AADD) return this / other
    if (this is IDD) return this / other
    else throw DDException("Comparison of incompatible types.")
}

operator fun DD<*>.plus(other: Double): DD<*> {
    if (this is AADD) return this + other
    if (this is IDD) return this + other
    else throw DDException("Comparison of incompatible types.")
}

operator fun DD<*>.minus(other: Double): DD<*> {
    if (this is AADD) return this - other
    if (this is IDD) return this - other
    else throw DDException("Comparison of incompatible types.")
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

infix fun <T: Any> DD<T>.greaterThan(other: DD<*>): BDD {
    if (this is AADD && other is AADD) return this greaterThan other
    if (this is IDD  && other is IDD ) return this greaterThan other
    else throw DDException("Comparison of incompatible types.")
}

infix fun <T: Any> DD<T>.greaterThanOrEquals(other: DD<*>): BDD {
    if (this is AADD && other is AADD) return this greaterThanOrEquals other
    if (this is IDD  && other is IDD ) return this greaterThanOrEquals other
    else throw DDException("Comparison of incompatible types.")
}

operator fun DD<*>.unaryMinus(): DD<*> {
    if (this is AADD) return builder.real(0.0).minus(this)
    if (this is IDD) return builder.integer(0).minus(this)
    else throw DDException("Unary minus on incompatible type.")
}

fun BDD.ite(t: DD<*>, e: DD<*>): DD<*> =
    when {
        this === builder.NaB -> builder.Infeasible
        this === builder.True -> t.clone()
        this === builder.False -> e.clone()
        else -> when (t) {
            is AADD -> ite(t, e as AADD)
            is BDD  -> ite(t, e as BDD)
            is IDD  -> ite(t, e as IDD)
            is StrDD -> ite(t, e as StrDD)
            else -> { throw DDInternalError("Internal Error.")}
        }
    }

/**
 * Calls the intersect functions of different kind of DD types
 * @param other: second parameter
 */
infix fun  DD<*>.intersect(other: DD<*>): DD<*> {
    return when (this) {
        is AADD -> this intersect other as AADD
        is BDD -> this intersect other as BDD
        is IDD -> this intersect other as IDD
        is StrDD -> this intersect other as StrDD
        else -> throw DDException("unknown DD type")
    }
}

/**
 * Returns number of internal nodes in a BDD.
 */
fun DD<*>.numInternalNodes(node: DD<*> = this): Int = when(node) {
    is DD.Leaf -> 0
    is DD.Internal -> 1 + numInternalNodes(node.T) + numInternalNodes(node.F)
    else -> throw DDException("Should never be reached.")
}


/**
 * Returns number of unknown variables;
 * is wrong I believe as max does not consider that T and F can have disjoint conditions.
 */
fun DD<*>.numUnknownVars(node: DD<*> = this): Int = when(node) {
    is DD.Leaf ->  0
    is DD.Internal -> max(node.index, max(numUnknownVars(node.T), numUnknownVars(node.F)))
    else -> throw DDException("Should never be reached.")
}

fun <T: Any> structurallyEquals(dd: DD<T>, other: DD<T>): Boolean =
    dd.structurallyEquals(other)

