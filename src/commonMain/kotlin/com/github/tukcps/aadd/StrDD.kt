@file:Suppress("unused")

package com.github.tukcps.aadd

import kotlin.math.min


sealed class StrDD: DD<String> {

    abstract override var builder: DDBuilder
    abstract override val index: Int
    abstract override fun toIteString(): String
    abstract override fun clone(): StrDD

    /**
     * StrDD has exactly two internal subclasses: Leaf or Internal.
     * Leaves have a value of type String, the index DD.LEAF_INDEX and no leaves.
     */
    class Leaf(
        override var builder: DDBuilder,
        override var value: String,
        override val status: DD.Status = DD.Status.NotSolved,
    ): StrDD(), DD.Leaf<String>
    {
        override val index: Int get() = DD.LEAF_INDEX
        override fun toIteString(): String = value
        override fun clone(): StrDD = builder.string(value)
        override fun toString(): String = value
    }

    /**
     * StrDD has exactly two internal subclasses: Leaf or Internal.
     * Internal nodes have an index and to child of type StrDD.
     */
    class Internal(
        override var builder: DDBuilder,
        override val index: Int,
        override val T: StrDD,
        override val F: StrDD,
        override val status: DD.Status = DD.Status.NotSolved
    ): StrDD(), DD.Internal<String> {
        override fun toIteString(): String = TODO("Not yet implemented")
        override fun clone(): StrDD = builder.internal(index, T.clone(), F.clone())
    }


    /**
     * Evaluate computes a StrDD that drops the conditions and internal nodes
     * that are set to True or False.
     */
    override fun evaluate(): StrDD = when(this) {
        is Leaf -> this //Do nothing on leaves
        is Internal -> {
            val cond = builder.conds.getVariable(index)

            //IMPORTANT: DO NOT change this if-statement to a when-statement.
            //Due to casts case differentiation will not work properly!
            when {
                cond === builder.True -> T.evaluate()
                cond === builder.False -> F.evaluate()
                cond === builder.Bool -> builder.internal(index, T.evaluate(), F.evaluate())
                else -> builder.internal(index, T.evaluate(), F.evaluate())
            }
        }
    }

    fun equalValue(strDD: StrDD): BDD{
        return if (this is Leaf && strDD is Leaf){
            if (this.value == strDD.value){
                builder.True
            } else {
                builder.False
            }
        } else {
            builder.False
        }
    }

    /**
     * Applies a multiplication of the StrDD with a BDD passed as a parameter and returns result. The BDD is
     * interpreted as 1.0 for true and 0.0 for false. The result is an StrDD where the 0/1 is replaced with
     * 0/AffineForm of the AADD.
     * @param other parameter to be multiplied with this.
     * @return result of binary operation on this and g.
     */
    operator fun times(other: BDD): StrDD {
        if (isInfeasible || other === builder.InfeasibleB) return builder.InfeasableS
        // ToDo: this prevents intersect() from running properly.
        // if (this.isLeaf && this.value!!.isEmpty()) return AADD.Empty;
        // NOTE, it shall hold: multiplication EMPTY * False = 0.0
        // Check for the terminals of the BDD g. It ends iteration and applies operation.
        if (other is BDD.Leaf) {
            return when(other) {
                builder.False -> builder.string("")
                builder.True  -> clone()
                builder.NaB   -> builder.EmptyStrings
                builder.Bool  -> builder.Strings // Not good. Should better be internal node?
                else -> throw DDException("Unknown Leaf type in BDD - internal error")
            }
        } else {
            val fT: StrDD
            val fF: StrDD
            val gT: BDD
            val gF: BDD
            val idx: Int

            // Recursion, with new node that has
            // the *largest* indices.
            if (index <= other.index && this is Internal) {
                idx = index
                fT = T
                fF = F
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
            val tr = fT * gT
            val fr = fF * gF
            return builder.internal(idx, tr, fr)
        }
    }
    operator fun plus(other: StrDD): StrDD =  this.apply(other){ a: Leaf, b: Leaf -> builder.leaf(a.value + b.value) }
    infix fun intersect(other: StrDD): StrDD = this.apply(other){ a:Leaf, b: Leaf -> builder.leaf(a.value.filter { it in b.value }) }

    /**
     * Applies a function with two parameters on the IDD
     * @param function the function
     * @param other parameter to be applied on this.
     * @return result of binary operation on this and g.
     */
    private fun apply(other: StrDD, function: (Leaf, Leaf) -> StrDD): StrDD {
        require(other.builder === this.builder)
        val fT: StrDD
        val fF: StrDD
        val gT: StrDD
        val gF: StrDD

        // Check for the terminals. It ends iteration and applies operation.
        if (isInfeasible || other.isInfeasible) return builder.InfeasableS
        if (this === builder.EmptyStrings || other === builder.EmptyStrings) return builder.EmptyStrings
        if (this is Leaf && other is Leaf) return function(this, other)
        // Otherwise, recursion following the T/F children with the largest index.
        val idx = min(index, other.index)
        if (index <= other.index && this is Internal) {
            fT = T
            fF = F
        } else {
            fF = this
            fT = fF
        }
        if (other.index <= index && other is Internal) {
            gT = other.T
            gF = other.F
        } else {
            gF = other
            gT = gF
        }
        val Tr = fT.apply(gT, function)
        val Fr = fF.apply(gF, function)
        return builder.internal(idx, Tr, Fr)
    }
}