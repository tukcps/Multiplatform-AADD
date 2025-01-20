@file:Suppress("unused")

package com.github.tukcps.aadd

import com.github.tukcps.aadd.DD.Companion.LEAF_INDEX
import com.github.tukcps.aadd.values.XBool

/**
 * The class BDD implements an ROBDD.
 * It is derived from the superclass DD with subtypes BDD, IDD, AADD, StrDD.
 * It has leaves that are True, False, and two subclasses:
 * - BDD.Internal
 * - BDD.Leaf
 * @author Christoph Grimm, Carna Zivkovic
 */
sealed class BDD: DD<XBool>, XBool
{
    abstract override fun clone(): BDD

    class Internal(
        override var builder: DDBuilder,
        override val index: Int = LEAF_INDEX,
        override val T: BDD,
        override val F: BDD,
        override val status: DD.Status = DD.Status.NotSolved
    ) : BDD(), DD.Internal<XBool>
    {
        override val value: XBool = XBool.X

        /** Clone provides a deep copy of a BDD;
         * reduces, and leaves remain references of ONE and ZERO. */
        override fun clone(): BDD = builder.internal(index, T, F)
        /** Returns short string that gives brief summary of the BDD tree. */
        override fun toString(): String = value.toString()

        /** Unary operation BDD -> BDD */
        override operator fun not() = this.apply(Leaf::not)
    }


    /** A leaf of a BDD has a value of the type Bool, not nullable and not Any? */
    class Leaf (
        override var builder: DDBuilder,
        override val value: XBool,
        override var status: DD.Status = DD.Status.NotSolved
    ) : BDD(), DD.Leaf<XBool> {
        constructor(builder: DDBuilder, value: Boolean, status: DD.Status = DD.Status.NotSolved):
                this(builder, if(value) XBool.True else XBool.False, status)

        override val index: Int get() = LEAF_INDEX

        // No clone for the leaves.
        override fun clone(): Leaf = this
        override fun toIteString(): String = toString()
        override fun toString(): String =
            when {
                this === builder.True   -> "True"
                this === builder.False  -> "False"
                this === builder.NaB    -> "Contradiction"
                this === builder.InfeasibleB -> "Infeasible"
                this === builder.Bool   -> "Unknown"
                else  -> "Error BDD leaf: None of True, False, Nab, Infeasible"
            }


        override fun not(): Leaf = builder.notTable[this]
            ?: throw DDInternalError("Unknown BDD Leaf type")

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
    protected fun apply(function: Leaf.() -> Leaf): BDD = when(this) {
        is Leaf     -> function(this)
        is Internal -> builder.internal(index, T.apply(function), F.apply(function))
    }

    abstract override fun not(): BDD

    /**
     * Applies a binary operator passed as las parameter on the BDD
     * passed as first two parameters and returns result.
     * @param op the operation
     * @param g parameter 2
     * @return result of binary operation on the parameters
     */
    private fun apply(op: (Leaf, Leaf) -> BDD, g: BDD): BDD {
        require(this.builder === g.builder) { "Builder of the operands must be the same." }
        val fT: BDD?
        val fF: BDD?
        val gT: BDD?
        val gF: BDD? // T, F of this and/or g
        val idx: Int

        // An operand path is infeasible; so, we return InfeasibleB to not solve again
        // the unsolvable LP problem.
        if (isInfeasible || g.isInfeasible)
            return builder.InfeasibleB

        // Leaves reached; ends iteration and applies operation.
        if (this is Leaf && g is Leaf)
            return op(this, g)

        // Recursion with child node that has the *largest* index.
        if (index <= g.index && this is Internal) {
            idx = index
            fT = T
            fF = F
        } else {
            idx = g.index
            fF = this
            fT = fF
        }
        if (g.index <= index && g is Internal) {
            gT = g.T
            gF = g.F
        } else {
            gF = g
            gT = gF
        }
        // do the recursion
        val Tr = fT.apply(op, gT)
        val Fr = fF.apply(op, gF)
        // now, the operation is finished.
        return builder.internal(idx, Tr, Fr)
    }

    /** Binary Operations BDD x BDD -> BDD */
    infix fun and(other: BDD): BDD = this.apply( Leaf::and, other )
    override infix fun and(other: XBool): BDD = this.apply( Leaf::and, other.bddLeafOf(this.builder))

    infix fun or(other: BDD): BDD = this.apply( Leaf::or, other )
    override infix fun or(other: XBool): BDD = this.apply(Leaf::or, other.bddLeafOf(this.builder))

    infix fun intersect(other: BDD): BDD = this.apply( Leaf::intersect, other )
    override infix fun intersect(other: XBool): BDD = this.apply(Leaf::intersect, other.bddLeafOf(this.builder))
    override fun contains(other: XBool): Boolean = TODO()

    infix fun xor(other: BDD): BDD = this.apply( Leaf::xor, other)
    infix fun nand(other: BDD): BDD = this.apply( Leaf::nand, other)

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
                is Internal -> if (other is Internal) (T == other.T) && (F == other.F) else false  // OK iff ROBDD!!
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
        this === builder.NaB -> builder.NaB
        this === builder.True -> t
        this === builder.False -> e
        else -> (this and t) or (this.not() and e)
    }

    /**
     * The ITE function merges two AADD by an if-then-else-function.
     * Note that the condition itself that is this BDD, is also a BDD.
     * The parameters are not changed.
     */
    fun ite(t: AADD, e: AADD): AADD = when {
        this === builder.NaB -> builder.Infeasible
        this === builder.True -> t.clone()
        this === builder.False -> e.clone()
        else -> (t * this) + (e * this.not())
    }

    /**
     * The ITE function merges two AADD by an if-then-else-function.
     * Note that the condition itself that is this BDD, is also a BDD.
     * The parameters are not changed.
     */
    fun ite(t: IDD, e: IDD): IDD = when {
        this === builder.NaB -> builder.InfeasibleI
        this === builder.True -> t.clone()
        this === builder.False -> e.clone()
        else -> (t * this) + (e * this.not())
    }

    /**
     * ITE for StrDD
     */
    fun ite(t: StrDD, e: StrDD): StrDD = when {
        this === builder.NaB -> builder.InfeasableS
        this === builder.True -> t.clone()
        this === builder.False -> e.clone()
        else -> (t * this) + (e * this.not())
    }

    /** Returns the number of leaves that hold the value true (SAT). */
    fun numTrue(): Int = when(this) {
        is Leaf -> if (this === builder.True) 1 else 0
        is Internal -> T.numTrue() + F.numTrue()
    }

    /** Returns true if BDD satisfiable (numTrue >= 1). */
    fun satisfiable(): Boolean = numTrue() >= 1

    /** Returns the number of leaves that hold the value false (UnSAT). */
    fun numFalse(): Int = when(this) {
        is Leaf -> if (this === builder.False) 1 else 0
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
            val cond = builder.conds.getVariable(index)

            // IMPORTANT: DO NOT change this to a when(cond)-statement.
            // Due to casts case differentiation will not work properly!
            when {
                cond === builder.True -> T.evaluate()
                cond === builder.False -> F.evaluate()
                cond === builder.Bool -> builder.internal(index, T.evaluate(), F.evaluate())
                else -> builder.internal(index, T.evaluate(), F.evaluate())
            }
        }
    }

    override fun toIteString():String{ return super.toIteString()}

}
