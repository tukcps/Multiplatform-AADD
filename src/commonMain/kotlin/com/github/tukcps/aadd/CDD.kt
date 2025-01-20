package com.github.tukcps.aadd

import com.github.tukcps.aadd.DD.Companion.LEAF_INDEX
import com.github.tukcps.aadd.values.StateTuple

sealed class CDD : DD<StateTuple> {
    abstract override var builder: DDBuilder
    abstract override val index: Int
    abstract override var status: DD.Status

    /**
     * An internal node that has an index, a true-, and a false edge that lead to an AADD each.
     * @param index The index must be registered in the builder.
     * @param T The true-child
     * @param F the false-child
     * */
    class Internal(
        override var builder: DDBuilder,
        override var index: Int,
        override val T: CDD,
        override val F: CDD,
        override var status: DD.Status = DD.Status.NotSolved
        ) : CDD(), DD.Internal<StateTuple> {
            override fun clone(): DD<StateTuple> {
            TODO("Not yet implemented")
            }
    }


    /**
     * A leaf has a value and a status that is of class LeafTuple
     * @param builder the factory used for building this object
     * @param value the value, an affine form
     * @param status the status of solving the LP problem: not solved, feasible, or infeasible.
     * */
    class Leaf(
        override var builder: DDBuilder,
        override var value: StateTuple,
        override var status: DD.Status = DD.Status.NotSolved
    ) : CDD(), DD.Leaf<StateTuple>
    {

        override val index: Int get() = LEAF_INDEX

        override fun toString(): String {
            return value.toString()
        }

        override fun clone(): DD<StateTuple> {
            TODO("Not yet implemented")
        }
    }

    override fun toIteString():String
    {
        return super.toIteString()
    }

    fun toSymbolicString(): String = when(this)
    {
        is Leaf -> value.toSymbolicString()
        is Internal -> "ITE($index, ${T.toSymbolicString()}, ${F.toSymbolicString()})"
    }

    /**
     * TODO: corutine implementation
     * */
    fun solve()
    {

    }

    /**
     * TODO: corutine implementation
     * */
    private fun computeBounds(indexes: IntArray, ge:BooleanArray, len: Int)
    {
        when(this)
        {
            is Leaf->
                {
                    callLPSolver(indexes,ge,len)
                }
            is Internal->
                {
                    if(!isBoolCond())
                    {
                        indexes[len] = index

                    }
                }
        }
    }

    /**
     * TODO: Finishe me
     * */
    private fun callLPSolver(indexes: IntArray, ge: BooleanArray,len: Int )
    {

    }

    /** Begin Utility functions */

    /**
     * @return A List containing all the leafs of this cdd together with the indices of the constraints of the internal nodes that lead to the specific leaf
     * */
    fun gatherLeafsWithPaths():MutableList<Pair<Leaf, MutableList<Int>>>
    {
        return leafPathGatherer(mutableListOf<Int>())
    }


    /**
     * Helper function TODO: Fix me up and make it a single function
     * */
    fun leafPathGatherer(path:MutableList<Int>) : MutableList<Pair<Leaf,MutableList<Int>>>
    {
        return if(this is Leaf) mutableListOf(Pair(this,path))
        else {
            ((this as Internal).T.leafPathGatherer((path+this.index).toMutableList()) + this.F.leafPathGatherer((path+(-this.index)).toMutableList())).toMutableList()
        }
    }

    /** End Utility functions */

}