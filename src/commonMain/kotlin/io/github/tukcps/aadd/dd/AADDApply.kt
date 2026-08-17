package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.dd.AADD.Internal
import io.github.tukcps.aadd.dd.AADD.Leaf
import io.github.tukcps.aadd.values.real.aa.AffineForm

/**
 * Applies a unary operator on an AADD and returns its AADD result.
 * @param function operator to be applied on this AADD, returning the result. This remains unchanged.
 * @return result of operation.
 */
fun AADD.applySplit(function: (AffineForm) -> AADD): AADD = when(this) {
    is Leaf -> if (isInfeasible()) infeasible else function(this.value)
    is Internal -> builder.internal(index, T.applySplit(function), F.applySplit(function))
}

fun AADD.apply(function: (AffineForm) -> AffineForm): AADD = when(this) {
    is Leaf -> if (isInfeasible()) infeasible else builder.leaf(function(this.value))
    is Internal -> builder.internal(index, T.apply(function), F.apply(function))
}

fun AADD.apply(other: AADD, op: (AffineForm, AffineForm) -> AffineForm): AADD =
    applyGeneric(other) { a: AffineForm, b: AffineForm -> op(a, b) }

fun AADD.applySplit(other: AADD, op: (AffineForm, AffineForm) -> AADD): AADD =
    applySplitGeneric(other, op)

fun AADD.applyOther(other: Double, op: (AffineForm, Double) -> AffineForm): AADD =
    applyDDOtherGeneric(other, op) { x: AffineForm -> this.builder.leaf(x) }


/**
 * Applies a multiplication of the AADD with a BDD passed as a parameter and returns result.
 * The BDD is interpreted as 1.0 for true and 0.0 for false.
 * The result is an AADD where the 0/1 are replaced with 0/AffineForm of the AADD.
 * @param other parameter to be multiplied with this.
 * @return result of binary operation on this and g.
 */
fun AADD.timesBDD(other: BDD): AADD =
    genericTimesBDD(other)