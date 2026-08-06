package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.dd.IDD.Internal
import io.github.tukcps.aadd.dd.IDD.Leaf
import io.github.tukcps.aadd.values.integer.IntegerRange
import kotlin.math.min


/**
 * Applies a unary operator on an IDD and returns its IDD result.
 * @param f operator to be applied on this IDD, returning the result. This remains unchanged.
 * @return result of operation.
 */
fun IDD.apply(f: (IntegerRange) -> IntegerRange): IDD = when (this) {
    is Leaf -> if (isInfeasible()) infeasible else builder.leaf(f(this.value))
    is Internal -> builder.internal(index, T.apply(f), F.apply(f))
}

/**
 * Applies a function with two parameters on the IDD
 * @param function the function
 * @param other parameter to be applied on this.
 * @return result of binary operation on this and g.
 */
internal fun IDD.applySplit(other: IDD, function: (IntegerRange, IntegerRange) -> IDD): IDD =
    applySplitGeneric(other, function)

internal fun IDD.apply(other: IDD, function: (IntegerRange, IntegerRange) -> IntegerRange): IDD =
    applyGeneric(other, function)

internal fun IDD.applyOther(other: Long, op: (IntegerRange, Long) -> IntegerRange): IDD =
    applyDDOtherGeneric(other, op, { x: IntegerRange -> this.builder.leaf(x)} )

/**
 * Applies a multiplication of the IDD with a BDD
 * passed as a parameter and returns the result. The BDD is
 * interpreted as 1.0 for true and 0.0 for false.
 * The result is an IDD where the 0/1 are replaced with 0/AffineForm of the IDD.
 * @param other parameter to be multiplied with this.
 * @return result of binary operation on this and g.
 */
fun IDD.timesBDD(other: BDD): IDD {
    val fT: IDD
    val fF: IDD
    val gT: BDD
    val gF: BDD
    // val idx: Int

    // Check for the terminals of the BDD g. It ends iteration and applies operation.
    if (isInfeasible() || other.isInfeasible()) return infeasible
    return  when (other) {
        builder.Bool.Empty     -> builder.Integers.Empty
        builder.Bool.False   -> builder.leaf(IntegerRange(0))
        builder.Bool.True    -> clone()
        else -> {
            val idx = min(index, other.index)
            if (index <= other.index && this is Internal) {
                fT = T
                fF = F
            } else {
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
            val Tr = fT.timesBDD(gT)
            val Fr = fF.timesBDD(gF)
            builder.internal(idx, Tr, Fr)
        }
    }
}