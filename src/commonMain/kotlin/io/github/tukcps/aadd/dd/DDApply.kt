@file:Suppress("UNCHECKED_CAST")

package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.values.ScalarValue
import kotlin.jvm.JvmName
import kotlin.math.min


/**
 * Applies a unary function to a DD.
 */
internal fun <ValueType: ScalarValue, DDType: DD<ValueType>>
        DDType.applyUnaryGeneric(function: (ValueType) -> DDType): DDType =
    when(this) {
        is DD.Leaf<*> -> if (isInfeasible()) infeasible else function(this.value as ValueType)
        is DD.Internal<*> -> builder.internal(index,
            (T as DDType).applyUnaryGeneric(function),
            (F as DDType).applyUnaryGeneric(function)
        )
    } as DDType

/**
 * Applies a unary function to a DD.
 * The function returns a DD, and hence can "split" ranges.
 */
internal fun <ValueType: ScalarValue, DDType: DD<ValueType>>
    DDType.applyUnarySplitGeneric(function: (ValueType) -> DDType): DDType =
    when(this) {
        is DD.Leaf<*> -> if (isInfeasible()) infeasible else function(this.value as ValueType)
        is DD.Internal<*> -> builder.internal(index,
            (T as DDType).applyUnarySplitGeneric(function),
            (F as DDType).applyUnarySplitGeneric(function)
        )
    } as DDType



/**
 * Applies a binary function op on a DD (receiver) and a second, non-DD parameter.
 * As the return value of the function is a DD, the function's result must be mapped to a DD.
 * @receiver 1st parameter of the function, a DD
 * @param other 2nd parameter of the function, not a DD.
 * @param op the function (ValueType, OtherValueType) -> ResultType.
 * @param creator a lambda that creates a leaf from the op's result.
 * @return result of binary operation on this and g.
 */
internal fun <ValueType: ScalarValue, OtherValueType: Any, ResultType: ScalarValue, DDType: DD<ValueType>>
        DDType.applyDDOtherGeneric(
            other: OtherValueType,
            op: (ValueType, OtherValueType) -> ResultType,
            creator: (ResultType) -> DDType
): DDType = when(this) {
    is DD.Leaf<*> -> if (isInfeasible()) infeasible else creator(op(this.value as ValueType, other))
    is DD.Internal<*> -> builder.internal(index,
        (T as DDType).applyDDOtherGeneric(other, op, creator),
        (F as DDType).applyDDOtherGeneric(other, op, creator)
    )
} as DDType


/**
 * Applies a binary function op on two DD: receiver, parameter.
 * As the return value of the function is a DD, the op may split the result in two or more leaves.
 * @receiver 1st parameter of the function
 * @param other 2nd parameter of the function
 * @param op the function (Leaf, Leaf) -> DDType
 * @return result of binary operation on this and g.
 */
internal fun <ValueType: ScalarValue, DDType: DD<ValueType>> DDType.applySplitGeneric(
    other: DDType,
    op: (ValueType, ValueType) -> DDType): DDType
{
    require(other.builder === this.builder)
    val thisT: DDType
    val thisF: DDType
    val otherT: DDType
    val otherF: DDType

    // Check for the terminals. It ends iteration and applies operation.
    if (isInfeasible() || other.isInfeasible()) return infeasible as DDType
    if (this === empty || other === empty) return empty as DDType
    if (this is DD.Leaf<*> && other is DD.Leaf<*>) return op(this.value as ValueType, other.value as ValueType)

    // Otherwise, recursion following the T/F children with the largest index.
    val idx = min(index, other.index)
    if (index <= other.index && this is DD.Internal<*>) {
        thisT = T as DDType
        thisF = F as DDType
    } else {
        thisF = this
        thisT = thisF
    }
    if (other.index <= index && other is DD.Internal<*>) {
        otherT = other.T as DDType
        otherF = other.F as DDType
    } else {
        otherF = other
        otherT = otherF
    }
    val tr = thisT.applySplitGeneric(otherT, op)
    val fr = thisF.applySplitGeneric(otherF, op)
    return builder.internal(idx, tr, fr)
}


/**
 * Applies a binary function `op` on two parameters `receiver` and `other`.
 * @param other parameter to be applied on this.
 * @param op the function (ValueType, ValueType) -> ValueType
 * @return result of binary operation on this and other.
 */
@JvmName("applyGenericValueValue")
internal fun <ValueType: ScalarValue, DDType: DD<ValueType>> DDType.applyGeneric(
    other: DDType,
    op: (ValueType, ValueType) -> ValueType
): DDType {
    check(other.builder === this.builder)
    val thisT: DDType
    val thisF: DDType
    val otherT: DDType
    val otherF: DDType

    // Check for the terminals. It ends iteration and applies operation.
    if (isInfeasible() || other.isInfeasible()) return infeasible as DDType
    if (this === empty || other === empty) return empty as DDType
    if (this is DD.Leaf<*> && other is DD.Leaf<*>) return builder.leaf(op(this.value as ValueType, other.value as ValueType))

    // Otherwise, recursion following the T/F children with the largest index.
    val newIndex = min(index, other.index)
    if (index <= other.index && this is DD.Internal<*>) {
        thisT = T as DDType
        thisF = F as DDType
    } else {
        thisF = this
        thisT = thisF
    }
    if (other.index <= index && other is DD.Internal<*>) {
        otherT = other.T as DDType
        otherF = other.F as DDType
    } else {
        otherF = other
        otherT = otherF
    }
    val tr = thisT.applyGeneric(otherT, op)
    val fr = thisF.applyGeneric(otherF, op)
    return builder.internal(newIndex, tr, fr)
}


/**
 * Applies a function with two parameters on the DD.
 * In this one, the function maps two **leaves** to a DD.
 * @param other parameter to be applied on this.
 * @param op the function (LeafType, LeafType) -> DDType
 * @return result of binary operation on this and other.
 */
internal fun <ValueType: ScalarValue, DDType: DD<ValueType>, LeafType: DDType> DDType.applyGeneric(
    other: DDType,
    op: (LeafType, LeafType) -> DDType
): DDType {
    check(other.builder === this.builder)
    val thisT: DDType
    val thisF: DDType
    val otherT: DDType
    val otherF: DDType

    // Check for the terminals. It ends iteration and applies operation.
    if (isInfeasible() || other.isInfeasible()) return infeasible as DDType
    if (this === empty || other === empty) return empty as DDType
    if (this is DD.Leaf<*> && other is DD.Leaf<*>) return op(this as LeafType, other as LeafType)

    // Otherwise, recursion following the T/F children with the largest index.
    val newIndex = min(index, other.index)
    if (index <= other.index && this is DD.Internal<*>) {
        thisT = T as DDType
        thisF = F as DDType
    } else {
        thisF = this
        thisT = thisF
    }
    if (other.index <= index && other is DD.Internal<*>) {
        otherT = other.T as DDType
        otherF = other.F as DDType
    } else {
        otherF = other
        otherT = otherF
    }
    val tr = thisT.applyGeneric(otherT, op)
    val fr = thisF.applyGeneric(otherF, op)
    return builder.internal(newIndex, tr, fr)
}


/**
 * Applies a multiplication of the AADD with a BDD passed as a parameter and returns result.
 * The BDD is interpreted as 1.0 for true and 0.0 for false.
 * The result is an AADD where the 0/1 are replaced with 0/AffineForm of the AADD.
 * @param other parameter to be multiplied with this.
 * @return result of binary operation on this and g.
 */
fun <DDType: DD<*>> DDType.genericTimesBDD(other: BDD): DDType {
    if (isInfeasible()) return infeasible as DDType
    // ToDo: this prevents intersect() from running properly.
    // if (this.isLeaf && this.value!!.isEmpty()) return AADD.Empty;
    // NOTE, it shall hold: multiplication EMPTY * False = 0.0
    // Check for the terminals of the BDD g. It ends iteration and applies operation.
    return when(other) {
        builder.Bool.Infeasible -> infeasible
        builder.Bool.False -> zero
        builder.Bool.True  -> clone()
        builder.Bool.Empty   -> empty
        builder.Bool.All  -> all
        else -> {
            val fT: DDType
            val fF: DDType
            val gT: BDD
            val gF: BDD
            val idx: Int

            // Recursion, with new node that has
            // the *largest* indices.
            if (index <= other.index && this is DD.Internal<*>) {
                idx = index
                fT = T as DDType
                fF = F as DDType
            } else {
                idx = other.index
                fF = this
                fT = fF
            }
            if (other.index <= index && other is BDD.Internal) {
                gT = other.T
                gF = other.F
            } else {
                gF = other
                gT = gF
            }
            val tr = fT.genericTimesBDD(gT)
            val fr = fF.genericTimesBDD(gF)
            when(tr) {
                is AADD -> builder.internal(idx, tr as AADD, fr as AADD)
                is IDD  -> builder.internal(idx, tr as IDD, fr as IDD)
                is StrDD -> builder.internal(idx, tr as StrDD, fr as StrDD)
                is BDD -> builder.internal(idx, tr as BDD, fr as BDD)
            }
        }
    } as DDType
}

fun <ValueType: ScalarValue, DDType: DD<ValueType>> DDType.applyGeneric(function: (ValueType) -> ValueType): DDType =
    when(this) {
        is DD.Leaf<*> -> if (isInfeasible()) infeasible else builder.leaf(function(this.value as ValueType))
        is DD.Internal<*> -> builder.internal(index, (T as DDType).applyGeneric(function), (F as DDType).applyGeneric(function))
    } as DDType


