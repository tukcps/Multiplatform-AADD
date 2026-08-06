package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.IntMath.minus
import io.github.tukcps.aadd.dd.DD.Companion.LEAF_INDEX
import io.github.tukcps.aadd.dd.DD.Status
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.integer.LongBound

/**
 * ## IDD - IntegerRange Decision Diagrams
 * The class IDD implements an Integer Decision Diagram (IDD).
 * An IDD is, in very brief, a decision diagram (class DD) whose leaf nodes
 * take values of type Integer. IDD are, like DD, ordered.
 * IDD objects are immutable.
 *
 * @author Christoph Grimm, Jack D. Martin
 */
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
sealed class IDD: DD<IntegerRange>, NumberRange<LongBound> {

    override val infeasible get() = builder.Integers.Infeasible
    override val empty get() = builder.Integers.Empty
    override val zero get() = builder.Integers.Zero
    override val one get() = builder.Integers.One
    override val all get() = builder.Integers.All

    abstract override fun clone(): IDD
    override val min: LongBound get() = getRange().min
    override val max: LongBound get() = getRange().max

    final override fun isZero(): Boolean = min.isZero && max.isZero
    final override fun isOne(): Boolean = max.isOne && max.isOne

    class Leaf(
        override var builder: DDBuilder,
        override val value: IntegerRange,
        override var status: Status = Status.NotSolved,
    ) : IDD(), DD.Leaf<IntegerRange> {
        override val index: Int get() = LEAF_INDEX
        override fun clone(): Leaf = builder.leaf(value, status)
        override fun toString(): String { getRange(); return value.toString() }
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

    infix fun intersect(other: IDD): IDD = this.apply(other, IntegerRange::intersect)
    override fun intersect(other: NumberRange<LongBound>): IDD = this.apply(builder.integer(other), IntegerRange::intersect)

    /**
     * Implements the relational operator less than `<`.
     * It compares an IDD with IDD passed as a parameter and calls the LP solver to compute min and max.
     * @param other - IDD to be compared with this
     * @return BDD
     */
    fun lessThan(other: IDD): BDD = (this - other).checkObjective("<") // this-g < 0
    infix fun lessThan(other: Long): BDD = lessThan(builder.leaf(IntegerRange(other)))
    override infix fun lessThan(other: NumberRange<LongBound>): BDD = lessThan(builder.leaf(other))

    /**
     * Implements relational operator less or equal than `<=`
     * @param other - IDD to be compared with this
     * @return IDD
     */
    infix fun lessThanOrEquals(other: IDD): BDD = (this - other).checkObjective("<=") // this-g <=0
    override infix fun lessThanOrEquals(other: NumberRange<LongBound>): BDD = lessThanOrEquals(builder.leaf(other))
    override infix fun lessThanOrEquals(other: LongBound): XBool = lessThanOrEquals(builder.leaf(IntegerRange(other)))

    /**
     * computes the relational operator greater than `>`
     * @param other An IDD that is compared with this.
     * @return A IDD that represents the comparison of the leaves.
     */
    infix fun greaterThan(other: IDD): BDD = (this - other).checkObjective(">") // this-other > 0
    override infix fun greaterThan(other: LongBound): BDD = greaterThan(builder.leaf(IntegerRange(other)))
    override infix fun greaterThan(other: NumberRange<LongBound>): BDD = greaterThan(builder.leaf(other))

    /**
     * Implements relational operator greater or equal than `>=`
     * @param other - IDD to be compared with this
     * @return A IDD that represents the comparison of the leaves.
     */
    infix fun greaterThanOrEquals(other: IDD): BDD = (this - other).checkObjective(">=") // this-other >= 0
    infix fun greaterThanOrEquals(other: Long): BDD = greaterThanOrEquals(builder.leaf(IntegerRange(other)))
    override infix fun greaterThanOrEquals(other: NumberRange<LongBound>): BDD = greaterThanOrEquals(builder.leaf(other))


    infix fun constrainTo(other: ClosedRange<LongBound>): IDD {
        getRange()
        return this.apply(builder.leaf(IntegerRange(other), Status.NotSolved), IntegerRange::intersect)
    }

    override fun join(other: NumberRange<LongBound>): NumberRange<LongBound> { TODO("Not yet implemented") }
    override fun greaterThanOrEquals(other: LongBound): XBool { TODO("Not yet implemented") }
    override fun lessThan(other: LongBound): XBool { TODO("Not yet implemented") }

    /**
     * Applies a function with a parameter where 2nd is a ClosedRange on the IDD
     */
    protected fun apply(f: (l: Leaf, r: ClosedRange<Long>) -> IDD, g: ClosedRange<Long>): IDD = when (this) {
        is Leaf -> if (isInfeasible()) infeasible else f(this, g)
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
                if (isInfeasible() || value.isEmpty())
                    return builder.Bool.Infeasible

                if(value.min.isInfinite || value.max.isInfinite)
                    // Overflow
                    return builder.Bool.False

                val cmp = when (op) {
                    ">=" -> value.greaterThanOrEquals(LongBound.Finite(0))
                    ">" -> value.greaterThan(LongBound.Finite(0))
                    "<=" -> value.lessThanOrEquals(LongBound.Finite(0))
                    "<" -> value.lessThan(LongBound.Finite(0))
                    else -> throw IllegalArgumentException()
                }
                when(cmp) {
                    XBool.True -> return builder.Bool.True
                    XBool.False -> return builder.Bool.False
                }
                // We cannot clearly decide whether larger or smaller, hence we create a new Integer-Constraint.
                // return if (op === ">=" || op === ">") builder.internal(builder.conds.newConstraint(value), builder.True, builder.False)
                // else builder.internal(builder.conds.newConstraint(value), builder.False, builder.True)
                // as long as we do not have an ILP solver, we can just keep it as an unknown boolean variable. Hence, no constraint, just a variable.
                return if (op == ">=" || op == ">") builder.internal(
                    builder.conditions.newVariable("", builder),
                    builder.Bool.True,
                    builder.Bool.False
                )
                else builder.internal(builder.conditions.newVariable("", builder), builder.Bool.False, builder.Bool.True)
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
    operator fun contains(value: Long): Boolean = when (this) {
        is Leaf     -> LongBound.Finite(value) <= this.value.max && LongBound.Finite(value) >= this.value.min
        is Internal -> T.contains(value) || F.contains(value)
    }

    /** Overridden operator "in" that allows us to check "Long .. Long in IDD" -> Boolean */
    operator fun contains(x: ClosedRange<Long>): Boolean = when(this) {
        is Leaf -> {
            LongBound.Finite(x.start) <= value.max && LongBound.Finite(x.endInclusive) >= value.min
        }
        is Internal -> T.contains(x) || F.contains(x)
    }

    /** Overloaded contains operation for allowing "IDD in range" notation */
    operator fun ClosedRange<Long>.contains(r: IDD): Boolean = when(r) {
        is Leaf -> {
            when {
                r.value.max.finiteValue < endInclusive -> true
                r.value.min.finiteValue > start -> false
                else -> true
            }
        }
        is Internal -> r.T.contains(this) || r.F.contains(this)
    }

    override fun evaluate(): IDD = when(this) {
        is Leaf -> this //Do nothing on leaves
        is Internal -> {
            val cond = builder.conditions.getVariable(index)

            //IMPORTANT: DO NOT change this if-statement to a when-statement.
            //Due to casts case differentiation will not work properly!
            when {
                cond === builder.Bool.True -> T.evaluate()
                cond === builder.Bool.False -> F.evaluate()
                cond === builder.Bool.All -> builder.internal(index, T.evaluate(), F.evaluate())
                else -> builder.internal(index, T.evaluate(), F.evaluate())
            }
        }
    }
}
