@file:Suppress("unused", "LocalVariableName")
package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.BoolMath.and
import io.github.tukcps.aadd.DDBuilder.BoolMath.not
import io.github.tukcps.aadd.DDBuilder.BoolMath.or
import io.github.tukcps.aadd.DDBuilder.IntMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.DDInternalError
import io.github.tukcps.aadd.dd.DD.Companion.LEAF_INDEX
import io.github.tukcps.aadd.values.bool.XBool

/**
 *
 * ## BDD_A
 *
 * The class BDD implements an ROBDD that specifically models the interaction
 * between discrete and continuous variables.
 *
 * ---
 *      IT IS NOT INTENDED TO BE USED AS A HIGH-PERFORMANCE BDD LIBRARY
 * ---
 *
 * It is derived from the superclass DD that is used for BDD, IDD, AADD, StrDD.
 * BDD is a sealed abstract class and has two internal concrete classes:
 * - BDD.Internal
 * - BDD.Leaf
 * As BDD is sealed, a BDD is either a Leaf or an Internal node.
 * @author Christoph Grimm, Carna Zivkovic
 */
sealed class BDD: DD<XBool>, XBool {

    override val infeasible: Leaf get() = builder.Bool.Infeasible
    override val empty: Leaf get() = builder.Bool.Empty
    override val one: Leaf get() = builder.Bool.True
    override val zero: Leaf get() = builder.Bool.False
    override val all: Leaf get() = builder.Bool.All
    override fun isZero() = value == zero
    override fun isOne() = value == one

    /**
     * Internal node of a BDD.
     * @param builder the builder and factory where the node was created and is used
     * @param index the index of the BDD
     * @param T BDD for true
     * @param F BDD for false
     * @param status
     */
    class Internal(
        override var builder: DDBuilder,
        override val index: Int = LEAF_INDEX,
        override val T: BDD,
        override val F: BDD,
        override val status: DD.Status = DD.Status.NotSolved
    ) : BDD(), DD.Internal<XBool>
    {
        override val value: XBool = XBool.All

        /** Clone provides a deep copy of a BDD;
         * reduces, and leaves remain references of ONE and ZERO. */
        override fun clone(): BDD = builder.internal(index, T, F)
        /** Returns short string that gives brief summary of the BDD tree. */
        override fun toString(): String = value.toString()

        /** Unary operation BDD -> BDD */
    }


    /**
     * A leaf of a BDD has a value of the type Bool, not nullable and not Any?
     * @param builder
     * @param value the value that is true, false, or one of the error cases contradiction, etc.
     */
    class Leaf(
        override var builder: DDBuilder,
        override val value: XBool,
        override var status: DD.Status = DD.Status.NotSolved
    ) : BDD(), DD.Leaf<XBool> {
        constructor(builder: DDBuilder, value: Boolean, status: DD.Status = DD.Status.NotSolved) :
                this(builder, if (value) XBool.True else XBool.False, status)

        override val index: Int get() = LEAF_INDEX

        // No clone for the leaves.
        override fun clone(): Leaf = this
        override fun toIteString(): String = toString()
        override fun toString(): String =
            when {
                this === builder.Bool.True -> "True"
                this === builder.Bool.False -> "False"
                this === builder.Bool.Empty -> "Contradiction"    // --> ~Empty
                this === builder.Bool.Infeasible -> "Infeasible"  // used to map infeasible parameters to infeasible result
                this === builder.Bool.All -> "Unknown"
                else -> "Invalid BDD leaf: None of True, False, Nab, Infeasible"
            }

        fun not(): Leaf =
            builder.notTable[this] ?: throw DDInternalError("Unknown BDD Leaf type")

        infix fun and(other: Leaf): Leaf =
            builder.andTable[Pair(this, other)] ?: throw DDInternalError("Unknown BDD Leaf type")

        infix fun or(other: Leaf): Leaf =
            builder.orTable[Pair(this, other)] ?: throw DDInternalError("Unknown BDD Leaf type")

        infix fun xor(other: Leaf): Leaf =
            builder.xorTable[Pair(this, other)] ?: throw DDInternalError("Unknown BDD Leaf type")

        infix fun nand(other: Leaf): Leaf =
            builder.nandTable[Pair(this, other)] ?: throw DDInternalError("Unknown BDD Leaf type")

        infix fun intersect(other: Leaf): Leaf =
            builder.intersectTable[Pair(this,other)] ?: throw DDInternalError("Unknown BDD Leaf type")

    }


    /**
     * Applies a unary operator on a BDD and returns its result.
     * It works recursively.
     * @param function a function on the BDD with a parameter.
     * @return a new BDD that is the result of the applied function.
     */
    fun apply(function: Leaf.() -> Leaf): BDD = when(this) {
        is Leaf     -> function(this)
        is Internal -> builder.internal(index, T.apply(function), F.apply(function))
    }

    /** Binary Operations BDD x BDD -> BDD */
    infix fun intersect(other: BDD): BDD = this.apply(other, Leaf::intersect)
    override infix fun intersect(other: XBool): BDD = this.apply(other.bddLeafOf(this.builder), Leaf::intersect)
    override fun contains(other: XBool): Boolean = TODO()

    /**
     * Compares this BDD with other BDD for equality.
     * Two BDD are equal if internal nodes have the same index, and leaves have the same value.
     * @param other
     * @return
     */
    override fun equals(other: Any?): Boolean =
        when {
            other === this  -> true
            other is Boolean -> this is Leaf && this.value == XBool.True
            other is BDD -> when (this) {
                is Leaf -> false // other === this  is always false because of (other === this) above
                is Internal -> other is Internal && (T == other.T) && (F == other.F)  // OK iff ROBDD!!
            }
            other is XBool -> other == this
            else -> false
        }


    /**
     * As we override 'equals', we need also to provide hashCode method that is equal
     * iff two BDD are likely equal.
     */
    override fun hashCode(): Int = when(this) {
        is Internal -> T.hashCode() + F.hashCode()
        is Leaf -> value.hashCode()
    }

    /**
     * The ITE function merges  BDD by an if-then-else-function.
     * Note that the condition itself that is this BDD, is also a BDD.
     * The parameters are not changed.
     */
    fun ite(t: BDD, e: BDD): BDD = when {
        this === builder.Bool.Empty -> builder.Bool.Empty
        this === builder.Bool.True -> t
        this === builder.Bool.False -> e
        else -> (this and t) or (this.not() and e)
    }

    /**
     * The ITE function merges two AADD by an if-then-else-function.
     * Note that the condition itself that is this BDD, is also a BDD.
     * The parameters are not changed.
     */
    fun ite(t: AADD, e: AADD): AADD = when {
        this === empty -> builder.Reals.Infeasible
        this === one   -> t.clone()
        this === zero  -> e.clone()
        else -> t.timesBDD(this) + e.timesBDD(this.not())
    }

    /**
     * The ITE function merges two AADD by an if-then-else-function.
     * Note that the condition itself that is this BDD, is also a BDD.
     * The parameters are not changed.
     */
    fun ite(t: IDD, e: IDD): IDD = when {
        this === builder.Bool.Empty -> builder.Integers.Infeasible
        this === builder.Bool.True -> t.clone()
        this === builder.Bool.False -> e.clone()
        else -> t.timesBDD(this) + e.timesBDD(this.not())
    }

    /**
     * ITE for StrDD
     */
    fun ite(t: StrDD, e: StrDD): StrDD = when {
        this === builder.Bool.Empty -> builder.Strings.Infeasible
        this === builder.Bool.True -> t.clone()
        this === builder.Bool.False -> e.clone()
        else -> (t * this) + (e * this.not())
    }

    /** Returns the number of leaves that hold the value true (SAT). */
    fun numTrue(): Int = when(this) {
        is Leaf -> if (this === builder.Bool.True) 1 else 0
        is Internal -> T.numTrue() + F.numTrue()
    }

    /** Returns true if BDD satisfiable (numTrue >= 1). */
    fun satisfiable(): Boolean = numTrue() >= 1

    /** Returns the number of leaves that hold the value false (UnSAT). */
    fun numFalse(): Int = when(this) {
        is Leaf -> if (this === builder.Bool.False) 1 else 0
        is Internal -> T.numFalse() + F.numFalse()
    }


    /**
     * Computes BDD … considering the currently set values for the decision variables and constraints.
     * (then simply skips a particular internal node, and only follows true or false edge)
     * Checks value of decision variables (Apply does not do this)
     * Does nothing on leaves (Apply applies function as parameter)
     * @return BDD that is structurally equivalent to this, but skips the indices/conditions that are set to True or False.
     */
    override fun evaluate(): BDD = when(this) {
        is Leaf -> this //Do nothing on leaves
        is Internal -> {
            val cond = builder.conditions.getVariable(index)

            // IMPORTANT: DO NOT change this to a when(cond)-statement.
            // Due to casts case differentiation will not work properly!
            when {
                cond === builder.Bool.True -> T.evaluate()
                cond === builder.Bool.False -> F.evaluate()
                cond === builder.Bool.All -> builder.internal(index, T.evaluate(), F.evaluate())
                else -> builder.internal(index, T.evaluate(), F.evaluate())
            }
        }
    }
}
