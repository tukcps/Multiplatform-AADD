@file:Suppress("UNCHECKED_CAST")

package io.github.tukcps.aadd

 import io.github.tukcps.aadd.DDBuilder.IntMath.minus
import io.github.tukcps.aadd.DDBuilder.IntMath.negate
import io.github.tukcps.aadd.DDBuilder.IntMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.negate
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.NumericValue
import io.github.tukcps.aadd.values.ScalarValue
import kotlin.math.max

operator fun <ValueType: Any, DDType: DD<NumericValue>> DDType.plus(other: DDType): DDType = when (this) {
    is AADD if other is AADD -> (this + other) as DDType
    is IDD  if other is IDD  -> (this + other) as DDType
    else -> throw DDException("Addition of incompatible types.")
}

operator fun <ValueType: Any, DDType: DD<NumericValue>> DDType.minus(other: DDType): DDType = when (this) {
    is AADD if other is AADD -> builder.realMath { (this@minus) - (other) }
    is IDD if other is IDD -> this@minus - other
    else -> throw DDException("Subtraction of incompatible types.")
} as DDType

operator fun <T: ScalarValue> DD<T>.div(other: DD<*>): DD<T> = when(this) {
    is AADD if (other is AADD) ->  this / other
    is IDD  if (other is IDD) -> this / other
    else -> throw DDException("Division of incompatible types.")
}

operator fun <ValueType: ScalarValue, DDType: DD<ValueType>> DDType.times(other: DDType): DDType {
    if (this is AADD && other is AADD) return this * other
    if (this is IDD  && other is IDD ) return this * other
    else throw DDException("Multiplication of incompatible types.")
}

operator fun <T: ScalarValue> DD<T>.times(other: Double): DD<T> {
    if (this is AADD) return this * other
    if (this is IDD) return this * other
    else throw DDException("Multiplication of incompatible types.")
}

operator fun <T: ScalarValue> DD<T>.div(other: Double): DD<*> {
    if (this is AADD) return this / other
    if (this is IDD) return this / other
    else throw DDException("Comparison of incompatible types.")
}

operator fun <T: ScalarValue> DD<T>.plus(other: Double): DD<*> {
    if (this is AADD) return this + other
    if (this is IDD) return this + other
    else throw DDException("Comparison of incompatible types.")
}

operator fun <T: ScalarValue> DD<T>.minus(other: Double): DD<*> {
    if (this is AADD) return this - other
    if (this is IDD) return this - other
    else throw DDException("Comparison of incompatible types.")
}

infix fun <T: ScalarValue> DD<T>.lessThan(other: DD<T>): BDD {
    if (this is AADD && other is AADD) return this lessThan other
    if (this is IDD  && other is IDD ) return this lessThan other
    else throw DDException("Comparison of incompatible types.")
}

infix fun <T: ScalarValue>  DD<T>.lessThanOrEquals(other: DD<T>): BDD {
    if (this is AADD && other is AADD) return this lessThanOrEquals other
    if (this is IDD  && other is IDD ) return this lessThanOrEquals other
    else throw DDException("Comparison of incompatible types.")
}

infix fun <T: ScalarValue> DD<T>.greaterThan(other: DD<T>): BDD {
    if (this is AADD && other is AADD) return this greaterThan other
    if (this is IDD  && other is IDD ) return this greaterThan other
    else throw DDException("Comparison of incompatible types.")
}

infix fun <T: ScalarValue> DD<T>.greaterThanOrEquals(other: DD<*>): BDD {
    if (this is AADD && other is AADD) return this greaterThanOrEquals other
    if (this is IDD && other is IDD ) return this greaterThanOrEquals other
    else throw DDException("Comparison of incompatible types.")
}

operator fun DD<NumericValue>.unaryMinus(): DD<*> {
    if (this is AADD) return negate(this)
    if (this is IDD) return negate(this)
    else throw DDException("Unary minus on incompatible type.")
}

fun <T: ScalarValue> BDD.ite(t: DD<T>, e: DD<T>): DD<*> =
    when {
        this === builder.Bool.Empty -> builder.Bool.Infeasible
        this === builder.Bool.True -> t.clone()
        this === builder.Bool.False -> e.clone()
        else -> when (t) {
            is AADD -> ite(t, e as AADD)
            is BDD -> ite(t, e as BDD)
            is IDD  -> ite(t, e as IDD)
            is StrDD -> ite(t, e as StrDD)
        }
    }

/**
 * Calls the intersect functions of different kind of DD types
 * @param other: second parameter
 */
infix fun <T: ScalarValue> DD<T>.intersect(other: DD<T>): DD<*> {
    return when (this) {
        is AADD -> this intersect other
        is BDD -> this intersect other
        is IDD -> this intersect other
        is StrDD -> this intersect other
    } as DD<T>
}

/**
 * Returns number of internal nodes in a BDD.
 */
fun <T: ScalarValue> DD<T>.numInternalNodes(node: DD<T> = this): Int = when(node) {
    is DD.Leaf -> 0
    is DD.Internal -> 1 + numInternalNodes(node.T) + numInternalNodes(node.F)
}

/**
 * Returns number of unknown variables;
 * is wrong I believe as max does not consider that T and F can have disjoint conditions.
 */
fun <T: ScalarValue> DD<T>.numUnknownVars(node: DD<T> = this): Int = when(node) {
    is DD.Leaf ->  0
    is DD.Internal -> max(node.index, max(numUnknownVars(node.T), numUnknownVars(node.F)))
}

fun <T: ScalarValue> structurallyEquals(dd: DD<T>, other: DD<T>): Boolean =
    dd.structurallyEquals(other)
