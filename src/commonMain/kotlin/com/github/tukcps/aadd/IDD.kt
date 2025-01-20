package com.github.tukcps.aadd

import com.github.tukcps.aadd.DD.Companion.LEAF_INDEX
import com.github.tukcps.aadd.DD.Status
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.NumberRange
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.min


/**
 * The class IDD implements an Integer Decision Diagram (IDD).
 * An IDD is, in very brief, a decision diagram (class DD) whose leaf nodes
 * take values of type Integer. IDD are, like DD, ordered.
 * IDD objects are immutable.
 *
 * @author Christoph Grimm, Jack D. Martin
 */
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@Serializable
sealed class IDD: DD<IntegerRange>, NumberRange<Long>{

    abstract override var builder: DDBuilder
    abstract override val index: Int
    abstract override var status: Status

    abstract override fun clone(): IDD

    // TODO general addition of the json serializers

    override val min: Long get() = getRange().min
    override val max: Long get() = getRange().max
    override val maxIsInf: Boolean get() = getRange().maxIsInf
    override val minIsInf: Boolean get() = getRange().minIsInf

    final override fun isZero(): Boolean = min == 0L && max == 0L
    final override fun isOne(): Boolean = max == 1L && min == 1L

    class Leaf(
        override var builder: DDBuilder,
        override val value: IntegerRange,
        override var status: Status = Status.NotSolved,
    ) : IDD(), DD.Leaf<IntegerRange> {
        override val index: Int get() = LEAF_INDEX
        override fun clone(): Leaf = builder.leaf(value.clone(), status)

        override fun toString(): String {
            getRange(); return value.toString()
        }
    }

    class Internal internal constructor(
        override var builder: DDBuilder,
        override val index: Int,
        override val T: IDD,
        override val F: IDD,
        override var status: Status = Status.NotSolved
    ) : IDD(), DD.Internal<IntegerRange> { // end Internal class
        /** Clone method. Makes a deep copy of the tree structure. */
        override fun clone(): IDD = builder.internal(index, T.clone(), F.clone())
        /** Returns a short string with just the range; to get also the ITE operations, use toIteString() */
        override fun toString(): String = getRange().toString()
    }


    /**
     * Applies a unary operator on an IDD and returns its IDD result.
     * @param f operator to be applied on this IDD, returning the result. This remains unchanged.
     * @return result of operation.
     */
    private fun apply(f: (Leaf) -> IDD): IDD = when (this) {
        is Leaf -> if (isInfeasible) builder.InfeasibleI else f(this)
        is Internal -> builder.internal(index, T.apply(f), F.apply(f))
    }

    fun negate(): IDD = this.apply { x: Leaf -> builder.leaf(-x.value) }
    fun power2(): IDD = this.apply { x: Leaf -> builder.leaf(x.value.power2()) }
    override fun exp(): IDD = this.apply { x: Leaf -> builder.leaf(x.value.exp()) }
    override fun sqr(): IDD = this.apply { x: Leaf -> builder.leaf(x.value.sqr()) }
    override fun sqrt(): IDD = this.apply { x: Leaf -> builder.leaf(x.value.sqrt()) }
    override fun root(other: NumberRange<Long>): IDD = this.apply { x: Leaf -> builder.leaf(x.value.root(other)) }
    override fun log(): IDD = this.apply { x: Leaf -> builder.leaf(x.value.log()) }
    override fun log(other: NumberRange<Long>): IDD = this.apply { x: Leaf -> builder.leaf(x.value.log(other)) }
    fun inv(): IDD = this.apply { x: Leaf -> builder.leaf(x.value.inv()) }

    /**
     * Applies a function with two parameters on the IDD
     * @param function the function
     * @param other parameter to be applied on this.
     * @return result of binary operation on this and g.
     */
    private fun apply(other: IDD, function: (Leaf, Leaf) -> IDD): IDD {
        require(other.builder === this.builder)
        val fT: IDD
        val fF: IDD
        val gT: IDD
        val gF: IDD

        // Check for the terminals. It ends iteration and applies operation.
        if (isInfeasible || other.isInfeasible) return builder.InfeasibleI
        if (this === builder.EmptyIntegerRange || other === builder.EmptyIntegerRange) return builder.EmptyIntegerRange
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

    /**
     * Applies a multiplication of the IDD with a BDD
     * passed as a parameter and returns the result. The BDD is
     * interpreted as 1.0 for true and 0.0 for false.
     * The result is an IDD where the 0/1 are replaced with 0/AffineForm of the IDD.
     * @param other parameter to be multiplied with this.
     * @return result of binary operation on this and g.
     */
    operator fun times(other: BDD): IDD {
        val fT: IDD
        val fF: IDD
        val gT: BDD
        val gF: BDD
        // val idx: Int

        // Check for the terminals of the BDD g. It ends iteration and applies operation.
        if (isInfeasible || other.isInfeasible) return builder.InfeasibleI
        return  when (other) {
            builder.NaB -> builder.EmptyIntegerRange //Integers//EmptyIntegerRange//builder.InfeasibleI
            builder.False -> builder.leaf(IntegerRange(0))
            builder.True -> return clone()
            else -> {
                //if (g === builder.NaB) return builder.InfeasibleI
                // Guess Axel's fix to jacks code fixing the recursion issue
                // TODO compare with commit
                // Recursion, with new node that has
                // the *largest* indices.
                val idx = min(index, other.index)
                if (index <= other.index && this is Internal) {
                    // idx = index
                    fT = T
                    fF = F
                } else {
                    // idx = g.index
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
                val Tr = fT.times(gT)
                val Fr = fF.times(gF)
                builder.internal(idx, Tr, Fr)
            }
        }
    }

    /** Binary operations IDD, IDD -> IDD */
    operator fun plus(other: IDD): IDD = this.apply(other){ a: Leaf, b: Leaf -> builder.leaf(a.value+b.value) }
    override operator fun plus(other: NumberRange<Long>) = this.apply(builder.leaf(other)) { a: Leaf, b: Leaf -> builder.leaf( a.value+b.value) }
    override operator fun plus(other: Long): IDD = this.apply(builder.leaf(other..other) ) { a: Leaf, b: Leaf -> builder.leaf(a.value+b.value) }
    operator fun minus(other: IDD): IDD = this.apply(other){ a: Leaf, b: Leaf -> builder.leaf(a.value-b.value) }
    override operator fun minus(other: NumberRange<Long>) = this.apply(builder.leaf(other)) { a: Leaf, b: Leaf -> builder.leaf( a.value-b.value) }
    override operator fun minus(other: Long): IDD = this.apply(builder.leaf(other..other) ) { a: Leaf, b: Leaf -> builder.leaf(a.value-b.value) }
    operator fun times(other: IDD): IDD = this.apply(other){ a: Leaf, b: Leaf -> builder.leaf(a.value*b.value) }
    override operator fun times(other: NumberRange<Long>) = this.apply(builder.leaf(other)) { a: Leaf, b: Leaf -> builder.leaf( a.value*b.value) }
    operator fun div(other: IDD): IDD = this.apply(other){ a: Leaf, b: Leaf -> builder.leaf(a.value/b.value) }
    override operator fun div(other: NumberRange<Long>) = this.apply(builder.leaf(other)) { a: Leaf, b: Leaf -> builder.leaf( a.value/b.value) }
    override operator fun div(other: Long): IDD = this.apply(builder.leaf(other..other) ) { a: Leaf, b: Leaf -> builder.leaf(a.value/b.value) }

    infix fun intersect(other: IDD): IDD = this.apply(other){ a: Leaf, b: Leaf -> builder.leaf(a.value intersect b.value) }
    override fun intersect(other: NumberRange<Long>): IDD = this.apply(builder.integer(other)) { a: Leaf, b: Leaf -> builder.leaf(a.value  intersect b.value) }
    /** Computes x^y */
    override fun pow(other: Long): IDD = this.apply { x: Leaf -> builder.leaf(x.value.pow(other)) }

    /** Computes x^y */
    override fun pow(other: NumberRange<Long>): IDD = this.apply { x: Leaf -> builder.leaf(x.value.pow(other)) }

    /** Computes x^y */
    fun pow(exp: IDD): IDD = this.apply(exp) { a: Leaf, b: Leaf -> builder.leaf(a.value.pow(b.value)) }
    override fun unaryMinus(): IDD = this.apply { x: Leaf -> builder.leaf( - x.value) }
    operator fun plus(other: Double): IDD = this.apply(builder.integer(other.toLong())) { x: Leaf, y: Leaf -> builder.integer(x.value+y.value) }
    override operator fun times(other: Long): IDD = this.apply(builder.integer(other)) { x: Leaf, y: Leaf -> builder.integer(x.value*y.value) }
    operator fun times(other: Double): IDD = this.apply(builder.integer(other.toLong())) { x: Leaf, y: Leaf -> builder.integer(x.value*y.value) }


    /**
     * Implements the relational operator less than `<`.
     * It compares an IDD with IDD passed as a parameter and calls the LP solver to compute min and max.
     * @param other - IDD to be compared with this
     * @return BDD
     */
    infix fun lessThan(other: IDD): BDD = (this - other).checkObjective("<") // this-g < 0
    override infix fun lessThan(other: Long): BDD = lessThan(builder.leaf(other..other))
    override infix fun lessThan(other: NumberRange<Long>): BDD = lessThan(builder.leaf(other))

    /**
     * Implements relational operator less or equal than `<=`
     * @param other - IDD to be compared with this
     * @return IDD
     */
    infix fun lessThanOrEquals(other: IDD): BDD = (this - other).checkObjective("<=") // this-g <=0
    override infix fun lessThanOrEquals(other: NumberRange<Long>): BDD = lessThanOrEquals(builder.leaf(other))
    override infix fun lessThanOrEquals(other: Long): BDD = lessThanOrEquals(builder.leaf(other..other))

    /**
     * computes the relational operator greater than `>`
     * @param other An IDD that is compared with this.
     * @return A IDD that represents the comparison of the leaves.
     */
    infix fun greaterThan(other: IDD): BDD = (this - other).checkObjective(">") // this-other > 0
    override infix fun greaterThan(other: Long): BDD = greaterThan(builder.leaf(other..other))
    override infix fun greaterThan(other: NumberRange<Long>): BDD = greaterThan(builder.leaf(other))


    /**
     * Implements relational operator greater or equal than `>=`
     * @param other - IDD to be compared with this
     * @return A IDD that represents the comparison of the leaves.
     */
    infix fun greaterThanOrEquals(other: IDD): BDD {
        val temp = this - other
        // temp.Leaf.getRange()
        return temp.checkObjective(">=") // this-other >= 0
    }

    override infix fun greaterThanOrEquals(other: Long): BDD = greaterThanOrEquals(builder.leaf(other..other))
    override infix fun greaterThanOrEquals(other: NumberRange<Long>): BDD = greaterThanOrEquals(builder.leaf(other))


    infix fun constrainTo(other: ClosedRange<Long>): IDD {
        getRange()
        return this.apply(builder.leaf(IntegerRange(other), Status.NotSolved)) {
                a: Leaf, b: Leaf -> builder.leaf(a.value.intersect(IntegerRange(b)))
        }
    }

    /**
     * Applies a function with a parameter where 2nd is a ClosedRange on the IDD
     */
    protected fun apply(f: (l: Leaf, r: ClosedRange<Long>) -> IDD, g: ClosedRange<Long>): IDD = when (this) {
        is Leaf -> if (isInfeasible) builder.InfeasibleI else f(this, g)
        is Internal -> builder.internal(index, T.apply(f, g), F.apply(f, g))
    }

    /**
     * This method computes the Range of an IDD considering
     *  *  the conditions as linear constraints.
     *  *  the noise symbol's limitations to -1 to 1.
     *  *  The affine forms at the leaves as objective functions to be min/max.
     * NOTE: Not working!
     * */
    fun getRange(): IntegerRange = when (this) {
        is Internal-> T.getRange().union(F.getRange())
        is Leaf -> value
    }


    /**
     * Creates an IDD, depending on the result of a comparison.
     * The result can either be True, False, or unknown, ich which case we add a new level to the BDD.
     * @param op
     * @return A BDD, set up recursively.
     */
    private fun checkObjective(op: String): BDD {
        when (this) {
            is Leaf -> {
                // Stop of recursion, comparison of IntegerRange with 0.
                if (isInfeasible || value.isEmpty())
                    return builder.InfeasibleB
                when (op) {
                    ">=" -> {
                        if (value.min >= 0 || abs(value.min) < Long.MIN_VALUE) return builder.True
                        if (value.max < 0) return builder.False
                    }
                    ">" -> {
                        if (value.min > 0) return builder.True
                        if (value.max < 0 || abs(value.max) < Long.MIN_VALUE) return builder.False
                    }
                    "<=" -> {
                        if (value.min > 0) return builder.False
                        if (value.max <= 0 || abs(value.max) < Long.MIN_VALUE) return builder.True
                    }
                    "<" -> {
                        if (value.min > 0 || abs(value.min) < Long.MIN_VALUE) return builder.False
                        if (value.max < 0) return builder.True
                    }
                }
                // We cannot clearly decide whether larger or smaller, hence we create a new Integer-Constraint.
                // return if (op === ">=" || op === ">") builder.internal(builder.conds.newConstraint(value), builder.True, builder.False)
                // else builder.internal(builder.conds.newConstraint(value), builder.False, builder.True)
                // as long as we do not have an ILP solver, we can just keep it as an unknown boolean variable. Hence, no constraint, just a variable.
                return if (op === ">=" || op === ">") builder.internal(
                    builder.conds.newVariable("", builder),
                    builder.True,
                    builder.False
                )
                else builder.internal(builder.conds.newVariable("", builder), builder.False, builder.True)
            }
            is Internal -> {
                /* Recursion step. */
                val Tr: BDD = T.checkObjective(op)
                val Fr: BDD = F.checkObjective(op)
                return builder.internal(index, Tr, Fr)
            }
        }
    }

    /** Long in IDD. Allows us writing "Long in IDD" */
    override operator fun contains(value: Long): Boolean = when (this) {
        is Leaf -> if (value > this.value.max) false else value >= this.value.min
        is Internal -> T.contains(value) || F.contains(value)
    }

    /** Overridden operator "in" that allows us to check "Long .. Long in IDD" -> Boolean */
    operator fun contains(x: ClosedRange<Long>): Boolean = when(this) {
        is Leaf -> {
            if (x.start > value.max) false
            else x.endInclusive >= value.min
        }
        is Internal -> T.contains(x) || F.contains(x)
    }

    /** Overloaded contains operation for allowing "IDD in range" notation */
    operator fun ClosedRange<Long>.contains(r: IDD): Boolean = when(r) {
        is Leaf -> {
            when {
                r.value.max < endInclusive -> true
                r.value.min > start -> false
                else -> true
            }
        }
        is Internal -> r.T.contains(this) || r.F.contains(this)
    }

    override fun evaluate(): IDD = when(this) {
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

    override fun copy(min: Long?, max: Long?): NumberRange<Long> {
        TODO("Not yet implemented")
    }

    override fun join(other: NumberRange<Long>): NumberRange<Long> {
        TODO("Not yet implemented")
    }

    override fun union(other: NumberRange<Long>): NumberRange<Long> {
        TODO("Not yet implemented")
    }

} // end class IDD
