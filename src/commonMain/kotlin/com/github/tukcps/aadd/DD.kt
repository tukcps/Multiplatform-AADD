@file:Suppress("KotlinConstantConditions", "unused")
package com.github.tukcps.aadd

import kotlin.math.max


/**
 * The interface DDcond implements a decision diagram as base class template leaf types.
 * It is the base class from which different kinds of DD are inherited.
 * It provides the basic framework, but not the leaves.
 * This involves in particular the management of the conditions and
 * index that are common for AADD and BDD.
 *
 * The DD is ordered via the index that also identifies a condition.
 * The index of leaves is Integer.MAXVALUE = LEAF_INDEX
 * The index of other nodes grows from 0 (the root) with increasing height of the graph.
 * Modified name to DDc to avoid conflicts with v2.x.
 */
sealed interface DD<ValueType: Any> {

    /** Reference to the factory of all DD and AffineForm objects. */
    var builder: DDBuilder
    val index: Int

    interface Leaf<ValueType : Any>: DD<ValueType> {
        val value: ValueType
    }

    interface Internal<ValueType : Any>: DD<ValueType> {
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

    val isFeasible:   Boolean get() = status != Status.Infeasible
    val isInfeasible: Boolean get() = status == Status.Infeasible

    /** Returns the number of leaves.  */
    fun numLeaves(): Int = when(this) {
        is Leaf<ValueType> -> 1
        is Internal<ValueType> ->  T.numLeaves() + F.numLeaves()
        else -> throw DDInternalError("Internal Error; not reachable.")
    }

    fun numInfeasible(): Int = when(this) {
        is Leaf<ValueType> -> if (isInfeasible) 1 else 0
        is Internal<ValueType> ->  T.numInfeasible() + F.numInfeasible()
        else -> throw DDInternalError("Internal Error; not reachable.")
    }

    fun numFeasible(): Int = when(this) {
        is Leaf<ValueType> -> if (!isFeasible) 0 else 1
        is Internal<ValueType> -> T.numFeasible() + F.numFeasible()
        else -> throw DDInternalError("Internal Error; not reachable.")
    }

    /** Returns the height of the tree.  */
    fun height(): Int = when(this) {
        is Leaf<*> -> 0
        is Internal<*> -> 1 + max(T.height(), F.height())
        // The following must not be reached:
        // all others are subclasses of Leaf, Internal
        else -> throw DDInternalError("Internal Error; not reachable.")
    }

    /** @return True if the condition refers to a boolean variable. */
    fun isBoolCond(): Boolean =
        if (index in builder.conds.x.keys)
            this.builder.conds.getVariable(index) is BDD
        else
            false

    fun getCondition(): DD<*> {
        return builder.conds.x[index]
            ?:throw DDInternalError("Index without a condition.")
    }

    fun evaluate(): DD<*> {
        return when (this) {
            is BDD  -> this.evaluate()
            is IDD  -> this.evaluate()
            is AADD -> this.evaluate()
            is StrDD -> this.evaluate()
            else -> throw Exception("Evaluate $this unknown data type.")
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
        return if (this is Leaf<ValueType> && other is Leaf<ValueType>) {
            (this.value == other.value) && (this.status == other.status)
        } else if (this !is Leaf<ValueType> && other !is Leaf<ValueType>)
            (this is Internal<ValueType> && other is Internal<ValueType>)
                    && (this.index == other.index)
                    && (this.status == other.status)
                    && this.T.structurallyEquals(other.T)
                    && this.F.structurallyEquals(other.F)
        else
            false //one argument is null
    }

    //fun containsSubDD(subDD: DD<ValueType>): Boolean {
    //    return this.runDepthFirst { this.structurallyEquals(subDD) }
    //}
    fun containsSubDD(subDD: DD<ValueType>): Boolean {
        return when (this) {
            is Leaf -> if (subDD is Leaf) (this.value == subDD.value && this.status == subDD.status) else false
            is Internal -> this.T.containsSubDD(subDD) || this.F.containsSubDD(subDD)
            else -> throw DDInternalError("Should never be reached.")
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

    fun asCAadd(): CDD = when(this){
        is CDD -> this
        else -> throw DDTypeCastError()
    }

    fun toIteString(): String = when(this) {
        is Leaf -> toString()
        is Internal -> "ITE($index, ${T.toIteString()}, ${F.toIteString()})"
        else -> throw DDInternalError("Must not be reached")
    }
}
