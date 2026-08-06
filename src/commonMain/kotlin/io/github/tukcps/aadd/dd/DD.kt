@file:Suppress("KotlinConstantConditions", "unused", "PropertyName")
package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDInternalError
import io.github.tukcps.aadd.DDTypeCastError
import io.github.tukcps.aadd.values.ScalarValue
import kotlin.math.max

/**
 * ## DD - Decision Diagrams, common interface
 *
 * The interface `DD` specifies a generic decision diagram.
 * It is the base class from which different kinds of DD are inherited:
 * - `AADD` (leaves take `AffineForm` as value)
 * - `IDD` (leaves take `IntegerRange` as value)
 * - `BDD` (leaves take `XBool` as value)
 * It provides the basic framework, but not the leaves.
 * This involves in particular the management of the conditions and
 * index that are common for `AADD` and `BDD`.
 *
 * - The DD is ordered via the index that also identifies a condition.
 * - The index of leaves is Integer.MAXVALUE = LEAF_INDEX
 * - The index of other nodes grows from 0 (the root) with increasing height of the graph.
 */
sealed interface DD<ValueType: ScalarValue> {

    /** Reference to the factory of all DD and AffineForm objects. */
    var builder: DDBuilder

    /**
     * Index via which the condition associated with an internal node can be
     * retrieved.
     */
    val index: Int

    /**
     * A leaf of a decision diagram that has a value of type ValueType.
     */
    sealed interface Leaf<ValueType : ScalarValue>: DD<ValueType> {
        val value: ValueType
    }

    /**
     * An internal node of a decision diagram that has two leaves.
     * The condition is accessed via index.
     */
    sealed interface Internal<ValueType : ScalarValue>: DD<ValueType> {
        val T: DD<ValueType>
        val F: DD<ValueType>
    }

    /**
     * @return a clone of the DD
     */
    fun clone(): DD<ValueType>

    /**
     * The status of a node's path condition.
     * After instantiation of a new DD, it is not solved.
     * After solving the LP problem, paths to leaves are feasible/infeasible.
     */
    enum class Status {NotSolved, Feasible, Infeasible}
    val status: Status

    /**
     * @return [io.github.tukcps.aadd.values.bool.XBool.False] if the path condition is not Empty.
     */
    fun isFeasible():   Boolean = status != Status.Infeasible
    fun isInfeasible(): Boolean = status == Status.Infeasible
    fun isZero(): Boolean
    fun isOne(): Boolean

    val infeasible: Leaf<ValueType>
    val empty: Leaf<ValueType>
    val zero: Leaf<ValueType>
    val one: Leaf<ValueType>
    val all: Leaf<ValueType>

    /** @return the number of leaves.  */
    fun numLeaves(): Int = when(this) {
        is Leaf<ValueType> -> 1
        is Internal<ValueType> ->  T.numLeaves() + F.numLeaves()
    }

    /** @return the number of leaves that are feasible; non-feasible numbers can be reduced */
    fun numInfeasible(): Int = when(this) {
        is Leaf<ValueType> -> if (isInfeasible()) 1 else 0
        is Internal<ValueType> ->  T.numInfeasible() + F.numInfeasible()
    }

    /** @return the number of feasible leaves */
    fun numFeasible(): Int = when(this) {
        is Leaf<ValueType> -> if (!isFeasible()) 0 else 1
        is Internal<ValueType> -> T.numFeasible() + F.numFeasible()
    }

    /** @return the height of the DD tree. */
    fun height(): Int = when(this) {
        is Leaf<*> -> 0
        is Internal<*> -> 1 + max(T.height(), F.height())
    }

    /** @return True if the condition refers to a boolean variable (not to a predicate!). */
    fun isBoolCond(): Boolean =
        index in builder.conditions.x.keys && this.builder.conditions.getVariable(index) is BDD

    /**
     * Each index is associated with a condition that is represented by a DD.
     * @return Condition from builder
     */
    fun getCondition(): DD<*> {
        return builder.conditions.x[index]
            ?:throw DDInternalError("Index without a condition.")
    }

    fun evaluate(): DD<ValueType> {
        return when (this) {
            is BDD -> this.evaluate()
            is IDD  -> this.evaluate()
            is AADD -> this.evaluate()
            is StrDD -> this.evaluate()
        }
    }

    /**
     * Executes a lambda parameter on each node; first, recursion is done
     * via internal nodes; then, the block is run.
     * @param ResultType: the type of the result
     * @param block: a function that, when executed returns the ResultType
     */
    fun <ResultType> runDepthFirst(block: DD<ValueType>.() -> ResultType): ResultType {
        if (this is Internal<ValueType>) {
            T.runDepthFirst(block)
            F.runDepthFirst(block)
        }
        return this.run(block)
    }

    fun structurallyEquals(other: DD<ValueType>): Boolean {
        return when (this) {
            is Leaf<ValueType> if other is Leaf<ValueType> -> {
                (this.value == other.value) && (this.status == other.status)
            }

            !is Leaf<ValueType> if other !is Leaf<ValueType> -> (this is Internal<ValueType> && other is Internal<ValueType>)
                    && (this.index == other.index)
                    && (this.status == other.status)
                    && this.T.structurallyEquals(other.T)
                    && this.F.structurallyEquals(other.F)

            else -> false
        } //one argument is null
    }

    //fun containsSubDD(subDD: DD<ValueType>): Boolean {
    //    return this.runDepthFirst { this.structurallyEquals(subDD) }
    //}
    fun containsSubDD(subDD: DD<ValueType>): Boolean {
        return when (this) {
            is Leaf -> subDD is Leaf && (this.value == subDD.value && this.status == subDD.status)
            is Internal -> this.T.containsSubDD(subDD) || this.F.containsSubDD(subDD)
        }
    }

    companion object {
        internal const val LEAF_INDEX = Int.MAX_VALUE
    }

    fun asAadd(): AADD = when (this) {
        is AADD -> this
        else -> throw DDTypeCastError()
    }
    fun asBdd(): BDD = when(this) {
        is BDD -> this
        else -> throw DDTypeCastError()
    }

    fun asIdd(): IDD = when(this) {
        is IDD -> this
        else -> throw DDTypeCastError()
    }

    fun asStrDD(): StrDD = when(this) {
        is StrDD -> this
        else -> throw DDTypeCastError()
    }

    fun toIteString(): String = when(this) {
        is Leaf -> toString()
        is Internal -> {
            val name : String? = builder.conditions.indexes.firstNotNullOfOrNull { if (it.value == index) it.key else null }
            "ITE(${name?:index}, ${T.toIteString()}, ${F.toIteString()})"
        }
    }
}
