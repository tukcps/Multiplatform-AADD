@file:Suppress("unused")
package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.RealMath.minus
import io.github.tukcps.aadd.dd.DD.Companion.LEAF_INDEX
import io.github.tukcps.aadd.lpsolver.*
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.max
import io.github.tukcps.aadd.values.real.DoubleBoundMath.min
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.rounding.Rounding
import io.github.tukcps.aadd.values.real.toDoubleBound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.nextDown

/**
 * ## AADD - Affine Arithmetic Decision Diagram
 * AADD represent non-contiguous subsets of the Reals,
 * i.e., Ranges, Sets of Ranges, Enumerations.
 *
 * The class AADD implements an Affine Arithmetic Decision Diagram (AADD).
 * An AADD is, in brief, a decision diagram (class DD) whose leaf nodes
 * take values of type AffineForm. AADD are, like DD, ordered.
 * AADD is a sealed class with the two subclasses
 * - AADD.Leaf, a leaf with a value (an AffineForm).
 * - AADD.Internal, an internal node with two child T and F.
 * AADD objects are immutable; we however do some annotations to leaves to
 * annotate state of solving LP problems.
 */
sealed class AADD: DD<AffineForm>, NumberRange<DoubleBound> {

    override val infeasible get() = builder.Reals.Infeasible
    override val zero get() = builder.Reals.Zero
    override val one get() = builder.Reals.One
    override val empty get() = builder.Reals.Empty
    override val all get() = builder.Reals.All

    /**
     * An internal node that has an index, a true-, and a false edge that lead to an AADD each.
     * @param builder The factory with additional information.
     * @param index The index must be registered in the builder.
     * @param T The true-child
     * @param F The false-child
     * @param status Whether the LP problem is solvable from root to this node.
     */
    class Internal(
        override var builder: DDBuilder,
        override var index: Int,
        override val T: AADD,
        override val F: AADD,
        override var status: DD.Status = DD.Status.NotSolved,
    ) : AADD(), DD.Internal<AffineForm> {
        override val min: DoubleBound get() = min(T.min, F.min)
        override val max: DoubleBound get() = max(T.max, F.max)
    }

    /**
     * A leaf has a value and a status that is of class AffineForm.
     * @param builder the factory used for building this object
     * @param value the value, an affine form
     * @param status the status of solving the LP problem: not solved, feasible, or infeasible.
     */
    class Leaf(
        override var builder: DDBuilder,
        override val value: AffineForm,
        override var status: DD.Status = DD.Status.NotSolved,
        var solverMin: Double = Double.NEGATIVE_INFINITY,
        var solverMax: Double = Double.POSITIVE_INFINITY
    ) : AADD(), DD.Leaf<AffineForm> {
        override val index: Int get() = LEAF_INDEX
        val central get() = value.central
        val radius get()= value.radius
        override val min get() = max(value.min, solverMin.toDoubleBound()?: DoubleBound.NegativeInfinity)
        override val max get() = min(value.max, solverMax.toDoubleBound()?: DoubleBound.PositiveInfinity)
    }

    /** Copy/update method. Creates a clone that has min, max, r updated.  */
    fun copy(min: DoubleBound, max: DoubleBound, r: Double? = null): AADD = when(this) {
        is Leaf -> builder.leaf(value.copy(min, max))
        is Internal -> builder.internal(index, T.copy(min, max), F.copy(min, max))
    }

    /** Clone method. Makes a deep copy of the tree structure. */
    override fun clone(): AADD = when(this) {
        is Internal -> builder.internal(index, T.clone(), F.clone())
        is Leaf ->  builder.leaf(value.clone())
    }

    /** Double in AADD. Allows us writing "Double in AADD" */
    operator fun contains(value: Double): Boolean = when(this) {
        is Internal -> T.contains(value) || F.contains(value)
        is Leaf -> this.value.contains(value)
    }

    /** Overridden operator "in" that allows us to check "Double .. Double in AADD" -> Boolean */
    operator fun contains(x: ClosedFloatingPointRange<Double>): Boolean = when(this) {
        is Leaf -> if (x.start > value.max.toDouble()) false else x.endInclusive >= value.min.toDouble()
        is Internal -> T.contains(x) || F.contains(x)
    }

    override fun join(other: NumberRange<DoubleBound>): AADD = when {
        other is AADD       -> apply(other, AffineForm::join)
        else                -> builder.leaf( (this as NumberRange<DoubleBound>) join other)
    }

    override fun intersect(other: NumberRange<DoubleBound>): AADD = when {
        other is AADD       -> apply(other, AffineForm::intersect)
        else                -> builder.leaf( (this as NumberRange<DoubleBound>) intersect other)
    }

    /**
     * Helper function for equalities; computes this-g < 0 using AADD, AA, or IA.
     */
    private fun difference(other: NumberRange<DoubleBound>): AADD? {
        if (this.isEmpty() || other.isEmpty()) return null
        val difference = if (other !is AADD) this - builder.leaf(other) else this - other
        difference.getRange() // triggers LP solver
        return difference
    }

    /**
     * Implements the relational operator less than `<`.
     * It compares an AADD with AADD passed as a parameter and calls the LP solver to compute min and max.
     * @param other - AADD, AffineForm, RealRange, or NumberRange to be compared with this
     * @return BDD
     */
    override infix fun lessThan(other: NumberRange<DoubleBound>): BDD =
         difference(other)?.checkObjective("<")?: builder.Bool.Empty
    override fun lessThan(other: DoubleBound): BDD = lessThan(RealRange(other, other))
    infix fun lessThan(other: Double): BDD = lessThan(builder.real(other))

    /**
     * Implements relational operator less or equal than `<=`
     * @param other - AADD, AffineForm, RealRange, or NumberRange to be compared with this
     * @return BDD
     */
    override infix fun lessThanOrEquals(other: NumberRange<DoubleBound>): BDD =
        difference(other)?.checkObjective("<=") ?: builder.Bool.Empty
    override fun lessThanOrEquals(other: DoubleBound): BDD = lessThanOrEquals(RealRange(other, other))
    infix fun lessThanOrEquals(other: Double): BDD = lessThanOrEquals(builder.real(other))

    /**
     * Computes the relational operator greater than `>`
     * @param other - AADD, AffineForm, RealRange, or NumberRange to be compared with this
     * @return A BDD that represents the comparison of the leaves.
     */
    override infix fun greaterThan(other: NumberRange<DoubleBound>): BDD =
        difference(other)?.checkObjective(">") ?: builder.Bool.Infeasible
    override fun greaterThan(other: DoubleBound): BDD = greaterThan(RealRange(other, other))
    infix fun greaterThan(other: Double): BDD = greaterThan(builder.real(other))

    /**
     * Implements relational operator greater or equal than `>=`
     * @param other - AADD to be compared with this
     * @return A BDD that represents the comparison of the leaves.
     */
    override infix fun greaterThanOrEquals(other: NumberRange<DoubleBound>): BDD =
        difference(other)?.checkObjective(">=")?: builder.Bool.Empty
    override fun greaterThanOrEquals(other: DoubleBound): BDD =
        greaterThanOrEquals(RealRange(other, other))
    infix fun greaterThanOrEquals(other: Double): BDD =
        greaterThanOrEquals(builder.real(other))

    override fun union(other: NumberRange<DoubleBound>): NumberRange<DoubleBound> {
        TODO("Not yet implemented")
    }

    /**
     * This method computes the Range of an AADD considering
     *  *  the conditions as linear constraints.
     *  *  the noise symbol's limitations to -1 to 1.
     *  *  The affine forms at the leaves as objective functions to be min/max.
     *  It is the main entry point for solving the LP problem and not only returns the overall range of
     *  all leaves, but also keeps the min/max values in each leaf and sets the status of the leaf.
     *  This is done recursively and in a concurrent way by calling the function computeBounds.
     */
    fun getRange(): RealRange {
        val height = height()
        val indexes = IntArray(height)
        val signs = BooleanArray(height)
        runBlocking { computeBounds(indexes, signs, 0) }
        return RealRange(min, max)
    }

    /**
     * Collects bounds of all leaves.
     * When the AADD is an internal node, it collects condition Xp,v on path to leave v.
     * For each leaf, it calls callLPSolver to compute bounds.
     * The method is called by getRange.
     */
    private suspend fun computeBounds(indexes: IntArray, ge: BooleanArray, len: Int): RealRange {
        when (this) {
            is Leaf -> {
                if (value.isEmpty()) return RealRange.Empty
                if (value.isFinite()
                    && indexes.isNotEmpty()
                    && value.radius > builder.settings.lpCallThreshold
                    && status == DD.Status.NotSolved
                )
                    callLPSolver(indexes, ge, len)
                return if (value.isEmpty()) RealRange.Empty
                else RealRange(solverMin.toDoubleBound() ?: DoubleBound.NegativeInfinity,
                    solverMax.toDoubleBound() ?: DoubleBound.PositiveInfinity)
            }
            is Internal -> {
                if (!isBoolCond()) {
                    var result: RealRange = RealRange.Empty
                    indexes[len] = index
                    withContext(Dispatchers.Default) {
                        val resT = async {
                            val ops = ge.copyOf()
                            ops[len] = true
                            T.computeBounds(indexes.copyOf(), ops, len + 1)
                        }
                        ge[len] = false
                        val resF = F.computeBounds(indexes, ge, len + 1)
                        result = resT.await().join(resF)
                    }
                    return result
                }
                val res = T.computeBounds(indexes, ge, len)
                return res.join(F.computeBounds(indexes, ge, len))
            }
        }
    }

    private fun callLPSolver(indexes: IntArray, ge: BooleanArray,len: Int){
        require(len>=0){"len of arrays must be >=1"}
        require(this is Leaf)
        builder.lpCalls+=1
        val symbols = mutableListOf<Long>()
        /* Gathering of all noise symbols used in the constraints as well as the leaf */
        symbols.addAll(value.xi.keys)

        for (i in 0 until len) {
            for(symbol in builder.conditions.getConstraint(indexes[i])!!.value.xi.keys){
                if(!symbols.contains(symbol))symbols.add(symbol)
            }
        }

        /* Create the LP Variable objects used */
        val constraints = mutableListOf<LpConstraint>() // List tracking all LPConstraints
        /* Create an LP Variable for all the symbols found in the 'symbols' list */
        val variables = mutableMapOf<Long, LpVariable>()
        for(symbol in symbols) {
            variables[symbol] = LpVariable("$symbol",canBeNegative = true)
        }

        /* Create noise symbol constraints -1 <= epsilon <= 1*/
        for((_, variable) in variables) {
            val upperNoiseConstraint = LpConstraint(LpExpression(mapOf(variable to 1.0)), LpConstraintSign.LESS_OR_EQUAL ,1.0 ) // x <= 1.0
            val lowerNoiseConstraint = LpConstraint(LpExpression(mapOf(variable to 1.0)), LpConstraintSign.GREATER_OR_EQUAL , -1.0 )// x >= -1.0
            constraints.add(upperNoiseConstraint)
            constraints.add(lowerNoiseConstraint)
        }

        /* Create constraints based on the path set */
        for(i in 0 until len) {
            val condition = builder.conditions.getConstraint(indexes[i])!!
            val coefficientVarMap = mutableMapOf<LpVariable,Double>()
            for((symbolKey, value1) in condition.value.xi) {
                coefficientVarMap[variables[symbolKey]!!] = value1
            }
            // Case none inverted
            if(ge[i]) {
                val pathConstraint = LpConstraint(LpExpression(coefficientVarMap),LpConstraintSign.GREATER_OR_EQUAL,-condition.value.central)
                constraints.add(pathConstraint)
            } // Case inverted
            else {
                val pathConstraint = LpConstraint(LpExpression(coefficientVarMap),LpConstraintSign.LESS_OR_EQUAL,-condition.value.central)
                constraints.add(pathConstraint)
            }
        }
        /* Create the actual LP Problem */

        /* Create the optimization function which is the leaf on which this function is called */
        val coefficientVarMap = mutableMapOf<LpVariable,Double>() // Map of the LpVariable object to its coefficient in the leaf affine form
        for((symbolKey, value1) in value.xi) {
            coefficientVarMap[variables[symbolKey]!!] = value1
        }

        val optfMaximize = LpFunction(LpExpression(coefficientVarMap,value.central),LpFunctionOptimization.MAXIMIZE)
        val optfMinimize = LpFunction(LpExpression(coefficientVarMap,value.central),LpFunctionOptimization.MINIMIZE)

        /* Create the Lp Problem object to solve. Consists out of the path constraints, variable constraitns and the optimization functions */
        val maxProblem = LpProblem(variables.values.toList(),constraints,optfMaximize)
        val minProblem = LpProblem(variables.values.toList(),constraints,optfMinimize)

        try {
            val maxSolution = solve(maxProblem)
            if(maxSolution == NoSolution) throw NoSolutionException()
            if(maxSolution == Unbounded) throw UnboundedException()

            val minSolution = solve(minProblem)
            if(minSolution == NoSolution) throw NoSolutionException()
            if(minSolution == Unbounded) throw UnboundedException()

            status = DD.Status.Feasible
            solverMax = min(value.max.finiteValue, (maxSolution as Solved).functionValue)
            // TODO: Fix LP solver for min!
            var computedMinSolution = value.central.nextDown()
            for((symbolValueKey, symbolValueValue) in (minSolution as Solved).variablesValues) {
                if(value.xi.contains(symbolValueKey.name.toLong())) {
                    computedMinSolution =
                        IEEE754RoundingMath.add(
                            IEEE754RoundingMath.mul(value.xi[symbolValueKey.name.toLong()]!!, symbolValueValue, Rounding.DOWN),
                            computedMinSolution, Rounding.DOWN)
                }
            }
            solverMin = max(value.min.toDouble(), computedMinSolution)

        } catch(e:NoSolutionException) {
            status = DD.Status.Infeasible
            solverMax = builder.AF.Empty.max.toDouble()
            solverMin = builder.AF.Empty.min.toDouble()
        } catch(e:UnboundedException){
            throw RuntimeException("AADD-Error: unbounded solution; maybe numerical issue in Simplex. Check Simplex cutoff & other params.")
        }
    }

    /**
     * Creates a BDD, depending on the result of a comparison.
     * The result can either be True, False, or unknown, in which case we add a new level to the BDD.
     * @param op
     * @return A BDD, set up recursively.
     */
    private fun checkObjective(op: String): BDD {
        when(this) {
            is Leaf -> {
                // Stop of recursion, comparison of Range/AF with 0.
                if (isInfeasible() || value.isEmpty())
                    return builder.Bool.Infeasible

                when (op) {
                    ">=" -> {
                        if (value.min.toDouble() > 0.0 || abs(value.min.toDouble()) < 2 * Double.MIN_VALUE) return builder.Bool.True
                        if (value.max.toDouble() < 0.0) return builder.Bool.False
                    }
                    ">" -> {
                        if (value.min.toDouble() > 0.0) return builder.Bool.True
                        if (value.max.toDouble() < 0.0 || abs(value.max.toDouble()) < 2 * Double.MIN_VALUE) return builder.Bool.False
                    }
                    "<=" -> {
                        if (value.min.toDouble() > 0.0) return builder.Bool.False
                        if (value.max.toDouble() < 0.0 || abs(value.max.toDouble()) < 2 * Double.MIN_VALUE) return builder.Bool.True
                    }
                    "<" -> {
                        if (value.min.toDouble() > 0.0 || abs(value.min.toDouble()) < 2 * Double.MIN_VALUE) return builder.Bool.False
                        if (value.max.toDouble() < 0.0) return builder.Bool.True
                    }
                }
                return if (op == ">=" || op == ">")
                    builder.internal(builder.conditions.newConstraint(value),
                        builder.Bool.True, builder.Bool.False
                )
                else
                    builder.internal(builder.conditions.newConstraint(value),
                        builder.Bool.False, builder.Bool.True)
            }
            is Internal -> {
                /* Recursion step. */
                val tr: BDD = T.checkObjective(op)
                val fr: BDD = F.checkObjective(op)
                return builder.internal(index, tr, fr)
            }
        }
    }

    /** Evaluates AADD with current assignment to decision variables/path conditions */
    override fun evaluate(): AADD = when (this) {
        is Leaf -> this
        is Internal -> {
            val cond = builder.conditions.getVariable(index)
            when {
                cond === builder.Bool.True -> T.evaluate()
                cond === builder.Bool.False -> F.evaluate()
                cond === builder.Bool.All -> builder.internal(index, T.evaluate(), F.evaluate())
                else -> builder.internal(index, T.evaluate(), F.evaluate())
            }
        }
    }

    /** Method that returns a brief String representation of the NumberRange interface */
    override fun toString(): String {
        getRange()
        return if (isInfeasible()) "Infeasible" else getRange().toString()
    }

    /**
     * Do Not Delete this function its required to utilize the super toIteFunction as the native objects don't know
     * the super functions!
     * */
    @Suppress("RedundantOverride")
    override fun toIteString() : String { return super.toIteString() }
}