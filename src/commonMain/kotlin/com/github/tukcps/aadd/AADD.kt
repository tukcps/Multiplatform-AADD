@file:Suppress("unused")

package com.github.tukcps.aadd

import com.github.tukcps.aadd.DD.Companion.LEAF_INDEX
import com.github.tukcps.aadd.lpsolver.*
import com.github.tukcps.aadd.DD.Status
import com.github.tukcps.aadd.functions.intersect
import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.NumberRange
import com.github.tukcps.aadd.values.Range
import com.github.tukcps.aadd.functions.constrainTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import com.github.tukcps.aadd.pwl.relu

/**
 * The class AADD implements an Affine Arithmetic Decision Diagram (AADD).
 * An AADD is, in very brief, a decision diagram (class DD) whose leaf nodes
 * take values of type AffineForm. AADD are, like DD, ordered.
 * AADD is a sealed class with the two subclasses
 * - AADD.Leaf, a leaf with a value (an AffineForm).
 * - AADD.Internal, an internal node with two child T and F.
 * AADD objects are immutable; we however do some annotations to leaves to
 * annotate state of solving LP problems.
 */
sealed class AADD: DD<AffineForm>, NumberRange<Double> {

    final override val min: Double get() =  when(this) {
        is Leaf -> value.min
        is Internal -> min(T.min, F.min)
    }

    final override val max: Double get() = when(this) {
        is Leaf -> value.max
        is Internal -> max(T.max, F.max)
    }

    final override fun isZero(): Boolean = this.min == 0.0 && this.max == 0.0
    final override fun isOne(): Boolean = this.min == 1.0 && this.max == 1.0

    override val minIsInf: Boolean get() = min.isInfinite()
    override val maxIsInf: Boolean get() = max.isInfinite()

    /**
     * An internal node that has an index, a true-, and a false edge that lead to an AADD each.
     * @param index The index must be registered in the builder.
     * @param T The true-child
     * @param F the false-child
     */
    class Internal(
        override var builder: DDBuilder,
        override var index: Int,
        override val T: AADD,
        override val F: AADD,
        override var status: Status = Status.NotSolved,
    ) : AADD(), DD.Internal<AffineForm>

    /**
     * A leaf has a value and a status that is of class AffineForm.
     * @param builder the factory used for building this object
     * @param value the value, an affine form
     * @param status the status of solving the LP problem: not solved, feasible, or infeasible.
     */
    class Leaf(
        override var builder: DDBuilder,
        override val value: AffineForm,
        override var status: Status = Status.NotSolved
    ) : AADD(), DD.Leaf<AffineForm> {
        override val index: Int get() = LEAF_INDEX

        val central get() = value.central
        val r get() = value.r
        val radius get()= value.radius

        override fun contains(value: Double) = this.value.contains(value)
    }

    override fun copy(min: Double?, max: Double?): AADD = when(this) {
        is Leaf -> {
            if (min == null || max == null || min <= max)
                builder.leaf(value.copy(min, max))
            else
                builder.Empty
        }
        is Internal -> builder.internal(index, T.copy(min, max), F.copy(min, max))
    }

    fun copy(min: Double?, max: Double?, r: Double?): AADD = when(this) {
        is Leaf -> builder.leaf(value.copy(min, max).also { value.r = r?:value.r })
        is Internal -> builder.internal(index, T.copy(min, max), F.copy(min, max))
    }

    /** Clone method. Makes a deep copy of the tree structure. */
    override fun clone(): AADD = when(this) {
        is Internal -> builder.internal(index, T.clone(), F.clone())
        is Leaf ->  builder.leaf(value.clone(), status)
    }


    fun relu() : AADD = when(this){
        is Internal -> builder.internal(index,T.relu(),F.relu())
        is Leaf -> relu(value,builder)
    }

    /** Method that returns a brief String representation of the NumberRange interface */
    override  fun toString(): String {
        getRange()
        return if (isInfeasible) "Infeasible" else getRange().toString()
    }

    /** Double in AADD. Allows us writing "Double in AADD" */
    override operator fun contains(value: Double): Boolean = when(this) {
        is Internal -> T.contains(value) || F.contains(value)
        is Leaf -> this.value.contains(value)
    }

    /** Overridden operator "in" that allows us to check "Double .. Double in AADD" -> Boolean */
    open operator fun contains(x: ClosedFloatingPointRange<Double>): Boolean = when(this) {
        is Leaf -> if (x.start > value.max) false else x.endInclusive >= value.min
        is Internal -> T.contains(x) || F.contains(x)
    }

    /**
     * Applies a unary operator on an AADD and returns its AADD result.
     * @param function operator to be applied on this AADD, returning result. This remains unchanged.
     * @return result of operation.
     */
    private fun apply(function: (Leaf) -> AADD): AADD = when(this) {
        is Leaf -> if (isInfeasible) builder.Infeasible else function(this)
        is Internal -> builder.internal(index, T.apply(function), F.apply(function))
    }
    override fun unaryMinus(): AADD = this.apply { x: Leaf -> builder.leaf( -x.value ) }
    fun negate(): AADD = this.apply { x: Leaf -> builder.leaf( -x.value ) }
    override fun exp(): AADD = this.apply  { x: Leaf -> builder.leaf( x.value.exp() ) }
    fun power2(): AADD = this.apply { x: Leaf -> builder.leaf(x.value.power2()) }
    override fun sqrt(): AADD = this.apply { x: Leaf -> builder.leaf(x.value.sqrt()) }
    override fun log(): AADD = this.apply  { x: Leaf -> builder.leaf(x.value.log()) }
    override fun log(other: NumberRange<Double>): AADD = this.apply  { x: Leaf -> builder.leaf(x.value.log(other)) }
    /** piece-wise linear definition of abs() over all nodes */
    fun abs(): AADD = this.apply { x:Leaf-> x.lessThan(0.0).ite(x * -1.0,x) }
    fun ceil() : AADD = this.apply { x : Leaf -> builder.leaf(x.value.ceil()) }
    fun invCeil() : AADD = this.apply { x : Leaf -> builder.leaf(x.value.invCeil()) }
    fun floor() : AADD = this.apply { x : Leaf -> builder.leaf(x.value.floor()) }
    fun invFloor() : AADD = this.apply { x : Leaf -> builder.leaf(x.value.invFloor()) }
    fun inv(): AADD = this.apply  { x: Leaf -> builder.leaf(x.value.inv()) }


    private fun <Type : Any> apply(other: Type, function: (Leaf, Type) -> AADD): AADD = when(this) {
        is Leaf -> if (isInfeasible) builder.Infeasible else function(this, other)
        is Internal -> builder.internal(index, T.apply(other, function), F.apply(other, function))
    }

    /**
     * Applies a function with two parameters on the AADD
     * @param other parameter to be applied on this.
     * @param op the function (Leaf, Leaf) -> AADD
     * @return result of binary operation on this and g.
     */
    private fun apply(other: AADD, op: (Leaf, Leaf) -> AADD): AADD {
        require(other.builder === this.builder)
        val thisT: AADD
        val thisF: AADD
        val otherT: AADD
        val otherF: AADD

        // Check for the terminals. It ends iteration and applies operation.
        if (isInfeasible || other.isInfeasible) return builder.Infeasible
        if (this === builder.Empty || other === builder.Empty) return builder.Empty
        if (this is Leaf && other is Leaf) return op(this, other)

        // Otherwise, recursion following the T/F children with the largest index.
        val idx = min(index, other.index)
        if (index <= other.index && this is Internal) {
            thisT = T
            thisF = F
        } else {
            thisF = this
            thisT = thisF
        }
        if (other.index <= index && other is Internal) {
            otherT = other.T
            otherF = other.F
        } else {
            otherF = other
            otherT = otherF
        }
        val tr = thisT.apply(otherT, op)
        val fr = thisF.apply(otherF, op)
        return builder.internal(idx, tr, fr)
    }

    /**
     * Applies a multiplication of the AADD with a BDD passed as a parameter and returns result. The BDD is
     * interpreted as 1.0 for true and 0.0 for false. The result is an AADD where the 0/1 are replaced with
     * 0/AffineForm of the AADD.
     * @param other parameter to be multiplied with this.
     * @return result of binary operation on this and g.
     */
    operator fun times(other: BDD): AADD {
        if (isInfeasible) return builder.Infeasible
        // ToDo: this prevents intersect() from running properly.
        // if (this.isLeaf && this.value!!.isEmpty()) return AADD.Empty;
        // NOTE, it shall hold: multiplication EMPTY * False = 0.0
        // Check for the terminals of the BDD g. It ends iteration and applies operation.
        return when(other) {
                builder.InfeasibleB -> builder.Infeasible
                builder.False -> builder.real(0.0)
                builder.True  -> clone()
                builder.NaB   -> builder.Empty
                builder.Bool  -> builder.Reals // TODO. Should better be node with proper Range!!
                else -> {
                    val fT: AADD
                    val fF: AADD
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
                    builder.internal(idx, tr, fr)
                }
        }
    }

    /** Binary operations AADD, AADD -> AADD */
    infix fun intersect(other: AADD): AADD {
        getRange()
        return this.apply(other, ::intersect)
    }

    infix fun intersect(other: ClosedRange<Double>): AADD {
        getRange()
        return this.apply(builder.leaf(AffineForm(builder, other))) { a: Leaf, b: ClosedRange<Double> ->
            constrainTo(a, Range(b))
        }
    }

    override infix fun intersect(other: NumberRange<Double>): AADD {
        getRange()
        return this.apply(builder.leaf(AffineForm(builder, other))) { a: Leaf, b: NumberRange<Double> ->
            constrainTo(a, Range(b))
        }
    }

    /** Adds constraints that restrict an AADD to a range */
    infix fun constrainTo(other: NumberRange<Double>): AADD  {
        getRange()
        val result = this.apply(other) {
            a: Leaf, b: NumberRange<Double> -> constrainTo(a, Range(b))
        }
        return result
    }

    operator fun plus(other: AADD): AADD =
        apply(other) { x: Leaf, y: Leaf -> builder.leaf( x.value + y.value ) }
    override operator fun plus(other: NumberRange<Double>): AADD =
        apply(other) { x: Leaf, y: NumberRange<Double> -> builder.leaf(x.value + y)}
    override operator fun plus(other: Double) =
        apply(other) { x: Leaf, y: Double -> builder.leaf(x.value + y) }

    operator fun minus(other: AADD): AADD =
        apply(other) { x: Leaf, y: Leaf -> builder.leaf(x.value - y.value) }
    override operator fun minus(other: NumberRange<Double>): AADD =
        apply(other) { x: Leaf, y: NumberRange<Double> -> builder.leaf( x.value - y) }
    override operator fun minus(other: Double): AADD =
        apply(other) { x: Leaf, y: Double -> builder.leaf(x.value - y) }

    operator fun times(other: AADD): AADD =
        apply(other) { x: Leaf, y: Leaf -> builder.leaf(x.value * y.value) }
    override operator fun times(other: NumberRange<Double>): AADD =
        apply(other) { x: Leaf, y: NumberRange<Double> -> builder.leaf(x.value * y) }
    override operator fun times(other: Double): AADD =
        apply(other) { x: Leaf, y: Double -> builder.leaf(x.value * y) }

    operator fun div(other: AADD): AADD =
        apply(other) { x: Leaf, y: Leaf -> builder.leaf(x.value / y.value) }
    override operator fun div(other: NumberRange<Double>): AADD =
        apply(other) { x: Leaf, y: NumberRange<Double> -> builder.leaf(x.value / y) }
    override operator fun div(other: Double): AADD =
        apply(other) { x: Leaf, y: Double -> builder.leaf(x.value / y) }

    infix fun power(other: AADD): AADD = this.apply(other) { x: Leaf, y: Leaf -> builder.leaf( x.value.pow(y.value))}

    /**
     * Calculates pow. That is, it is used for the following function: f(x,y) = x^y. The base is 'this'.
     * @param other : Double = the exponential power that the base is being raised to.
     */
    override fun pow(other : Double): AADD = this.apply { x: Leaf -> builder.leaf(x.value.pow(other)) }


    /**
     * Calculates pow. That is, it is used for the following function: f(x,y) = x^y. The base is 'this'
     * @param exp : Double = the exponential power that the base is being raised to.
     */
    fun pow(exp : AffineForm): AADD = this.apply { x: Leaf -> builder.leaf(x.value.pow(exp)) }
    override fun pow(other: NumberRange<Double>) = this.apply { x: Leaf -> builder.leaf(x.value.pow(builder.real(other))) }
    fun pow(other: AADD): AADD = this.apply(other) { x: Leaf, y: Leaf -> builder.leaf(x.value.pow(y.value)) }

    /**
     * Calculates nth square root
     */
    fun sqrt(exp : AffineForm): AADD = this.apply { x: Leaf -> builder.leaf(x.value.root(exp)) }
    override fun root(other: NumberRange<Double>) = this.apply { x: Leaf -> builder.leaf(x.value.root(builder.real(other))) }
    fun root(other: AADD): AADD = this.apply(other) { x: Leaf, y: Leaf -> builder.leaf(x.value.root(y.value)) }

    /** Logarithm for non-e base */
    fun log(base : Double): AADD = this.apply  { x: Leaf -> builder.leaf(x.value.log(base)) }

    /** sine on each leaf, not piecewise */
    fun sin(): AADD = this.apply {x : Leaf->builder.leaf(x.value.sin())}
    /** cosine on each leaf, no piecewise */
    fun cos(): AADD = this.apply {x : Leaf->builder.leaf(x.value.cos())}
    /** arcsin on each leaf, no piecewise */
    fun arcsin(): AADD = this.apply {x : Leaf->builder.leaf(x.value.arcsin())}
    /** arccos on each leaf, no piecewise */
    fun arccos(): AADD = this.apply {x : Leaf->builder.leaf(x.value.arccos())}

    /**
     * ceiling function for AADDs
     */
    open fun ceilAsLong() : Long  = kotlin.math.ceil(this.getRange().max).toLong()

    /**
     * ceiling function for AADDs, also converts to IntegerRange
     * @return IntegerRange
     */
    open fun ceiltoIntRange() : IntegerRange = IntegerRange(kotlin.math.ceil(getRange().min).toLong(), kotlin.math.ceil(getRange().max).toLong())

    /** floor function for AADDs */
    open fun floorAsLong() : Long  = kotlin.math.floor(this.getRange().min).toLong()

    /** floor function for AADDs, also converts to IntegerRange @return IntegerRange */
    open fun floorToIntRange() : IntegerRange = IntegerRange(kotlin.math.floor(getRange().min).toLong(), kotlin.math.floor(getRange().max).toLong())

    /**
     * Implements the relational operator less than `<`.
     * It compares an AADD with AADD passed as a parameter and calls the LP solver to compute min and max.
     * @param other - AADD to be compared with this
     * @return BDD
     */
    infix fun lessThan(other: AADD): BDD {
        if (this.isEmpty()) return builder.NaB
        if (other.isEmpty()) return builder.NaB
        val temp = (this - other)
        temp.getRange()
        return temp.checkObjective("<") // this-g < 0
    }

    override infix fun lessThan(other: Double): BDD = lessThan(builder.real(other))

    /**
     * Implements relational operator less or equal than `<=`
     * @param other - AADD to be compared with this
     * @return BDD
     */
    infix fun lessThanOrEquals(other: AADD): BDD {
        if (this.isEmpty() || other.isEmpty()) return builder.InfeasibleB
        val temp = (this - other)
        temp.getRange()
        return temp.checkObjective("<=") // this-g <=0
    }

    override infix fun lessThanOrEquals(other: Double): BDD = lessThanOrEquals(builder.real(other))
    override infix fun lessThanOrEquals(other: NumberRange<Double>): BDD = TODO("Not yet implemented")

    /**
     * computes the relational operator greater than `>`
     * @param other An AADD that is compared with this.
     * @return A BDD that represents the comparison of the leaves.
     */
    infix fun greaterThan(other: AADD): BDD {
        if (this.isEmpty() || other.isEmpty()) return builder.InfeasibleB
        val temp = this - other
        temp.getRange()
        return temp.checkObjective(">") // this-other > 0
    }

    override infix fun greaterThan(other: Double): BDD = greaterThan(builder.real(other))

    /**
     * Implements relational operator greater or equal than `>=`
     * @param other - AADD to be compared with this
     * @return A BDD that represents the comparison of the leaves.
     */
    infix fun greaterThanOrEquals(other: AADD): BDD {
        if (this.isEmpty() || other.isEmpty()) return builder.InfeasibleB
        val temp = this - other
        temp.getRange() // Triggers solver to compute LP problem
        return temp.checkObjective(">=") // this-other >= 0
    }

    override infix fun greaterThanOrEquals(other: Double): BDD =
        greaterThanOrEquals(builder.real(other))

    override fun join(other: NumberRange<Double>): NumberRange<Double> {
        TODO("Not yet implemented")
    }

    override fun union(other: NumberRange<Double>): NumberRange<Double> {
        TODO("Not yet implemented")
    }

    override fun greaterThan(other: NumberRange<Double>): BDD {
        TODO("Not yet implemented")
    }

    override fun greaterThanOrEquals(other: NumberRange<Double>): BDD {
        TODO("Not yet implemented")
    }

    override fun lessThan(other: NumberRange<Double>): BDD {
        TODO("Not yet implemented")
    }


    override fun sqr(): NumberRange<Double> = times(this)


    /**
     * This method computes the Range of an AADD considering
     *  *  the conditions as linear constraints.
     *  *  the noise symbol's limitations to -1 to 1.
     *  *  The affine forms at the leaves as objective functions to be min/max.
     *  It is the main entry point for solving the LP problem and not only returns the overall range of
     *  all leaves, but also keeps the min/max values in each leaf and sets the status of the leaf.
     *  This is done recursively and in a concurrent way by calling the function computeBounds.
     */
    fun getRange(): Range {
        val height = height()
        val indexes = IntArray(height)
        val signs = BooleanArray(height)
        val r = runBlocking {
            computeBounds(indexes, signs, 0)
        }
        return r
    }

    /**
     * Collects bounds of all leaves.
     * When the AADD is an internal node, it collects condition Xp,v on path to leave v.
     * For each leaf, it calls callLPSolver to compute bounds.
     * The method is called by getRange.
     */
    private suspend fun computeBounds(indexes: IntArray, ge: BooleanArray, len: Int): Range {
        when (this) {
            is Leaf -> {
                if (value.isEmpty()) return Range.Empty
                if (value.isFinite()
                    && indexes.isNotEmpty()
                    && value.radius > builder.config.lpCallTh
                    && status == Status.NotSolved
                )
                    callLPSolver(indexes, ge, len)
                return if (value.isEmpty()) Range.Empty
                else Range(value)
            }
            is Internal -> {
                if (!isBoolCond()) {
                    var result: Range = Range.Empty
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
        val symbols = mutableListOf<Int>()
        /* Gathering of all noise symbols used in the constraints as well as the leaf */
        symbols.addAll(value.xi.keys)

        for (i in 0 until len) {
            for(symbol in builder.conds.getConstraint(indexes[i])!!.value.xi.keys){
                if(!symbols.contains(symbol))symbols.add(symbol)
            }
        }

        /* Create the LP Variable objects used */
        val constraints = mutableListOf<LpConstraint>() // List tracking all LPConstraints
        /* Create an LP Variable for all the symbols found in the 'symbols' list */
        val variables = mutableMapOf<Int,LpVariable>()
        for(symbol in symbols) {
            variables[symbol] = LpVariable("$symbol",canBeNegative = true)
        }

        /* Create noise symbol constraints -1 <= epsilon <= 1*/
        for(variable in variables) {
            val upperNoiseConstraint = LpConstraint(LpExpression(mapOf(variable.value to 1.0)), LpConstraintSign.LESS_OR_EQUAL ,1.0 ) // x <= 1.0
            val lowerNoiseConstraint = LpConstraint(LpExpression(mapOf(variable.value to 1.0)), LpConstraintSign.GREATER_OR_EQUAL , -1.0 )// x >= -1.0
            constraints.add(upperNoiseConstraint)
            constraints.add(lowerNoiseConstraint)
        }

        /* Create constraints based on the path set */
        for(i in 0 until len) {
            val condition = builder.conds.getConstraint(indexes[i])!!
            val coefficientVarMap = mutableMapOf<LpVariable,Double>()
            for(symbol in condition.value.xi) {
                val symbolKey = symbol.key
                coefficientVarMap[variables[symbolKey]!!] = symbol.value
            }
            // Case none inverted
            if(ge[i]) {
                val pathConstraint = LpConstraint(LpExpression(coefficientVarMap),LpConstraintSign.GREATER_OR_EQUAL,-condition.value.central - condition.value.r)
                constraints.add(pathConstraint)
            } // Case inverted
            else {
                val pathConstraint = LpConstraint(LpExpression(coefficientVarMap),LpConstraintSign.LESS_OR_EQUAL,-condition.value.central + condition.value.r)
                constraints.add(pathConstraint)
            }
        }
        /* Create the actual LP Problem */

        /* Create the optimization function which is the leaf on which this function is called */
        val coefficientVarMap = mutableMapOf<LpVariable,Double>() // Map of the LpVariable object to its coefficient in the leaf affine form
        for(symbol in value.xi) {
            val symbolKey = symbol.key
            coefficientVarMap[variables[symbolKey]!!] = symbol.value
        }

        val optfMaximize = LpFunction(LpExpression(coefficientVarMap,value.central+value.r),LpFunctionOptimization.MAXIMIZE)
        val optfMinimize = LpFunction(LpExpression(coefficientVarMap,value.central-value.r),LpFunctionOptimization.MINIMIZE)

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

            status = Status.Feasible
            value.max = min(value.max, (maxSolution as Solved).functionValue)


            // TODO fix this crappy lp solver and its min objective function issue
            //value.min = (minSolution as Solved).functionValue
            var computedMinSolution = value.central - value.r

            for(symbolValue in (minSolution as Solved).variablesValues) {
                if(value.xi.contains(symbolValue.key.name.toInt())) {
                    computedMinSolution += value.xi[symbolValue.key.name.toInt()]!! * symbolValue.value
                }
            }
            value.min = max(value.min, computedMinSolution)
            // TODO end

        } catch(e:NoSolutionException) {
            status = Status.Infeasible
            value.max = builder.AFEmpty.max
            value.min = builder.AFEmpty.min
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
                if (isInfeasible || value.isEmpty())
                    return builder.InfeasibleB

                when (op) {
                    ">=" -> {
                        if (value.min > 0.0 || abs(value.min) < 2 * Double.MIN_VALUE) return builder.True
                        if (value.max < 0.0) return builder.False
                    }
                    ">" -> {
                        if (value.min > 0.0) return builder.True
                        if (value.max < 0.0 || abs(value.max) < 2 * Double.MIN_VALUE) return builder.False
                    }
                    "<=" -> {
                        if (value.min > 0.0) return builder.False
                        if (value.max < 0.0 || abs(value.max) < 2 * Double.MIN_VALUE) return builder.True
                    }
                    "<" -> {
                        if (value.min > 0.0 || abs(value.min) < 2 * Double.MIN_VALUE) return builder.False
                        if (value.max < 0.0) return builder.True
                    }
                }
                return if (op === ">=" || op === ">") builder.internal(
                    builder.conds.newConstraint(value),
                    builder.True,
                    builder.False
                )
                else
                    builder.internal(builder.conds.newConstraint(value), builder.False, builder.True)
            }
            is Internal -> {
                /* Recursion step. */
                val tr: BDD = T.checkObjective(op)
                val fr: BDD = F.checkObjective(op)
                return builder.internal(index, tr, fr)
            }
        }
    }


    /** Returns an AADD that is from the context given as parameter. */
    fun fixDeserialized(builder: DDBuilder): AADD =
        when (this) {
            is Internal ->
                builder.internal(this.index, T.fixDeserialized(builder), F.fixDeserialized(builder))
            is Leaf ->
                builder.leaf(this.value)
        }

    /** Evaluates AADD with current assignment to decision variables/path conditions */
    override fun evaluate(): AADD = when (this) {
        is Leaf -> this
        is Internal -> {
            val cond = builder.conds.getVariable(index)
            when {
                cond === builder.True -> T.evaluate()
                cond === builder.False -> F.evaluate()
                cond === builder.Bool -> builder.internal(index, T.evaluate(), F.evaluate())
                else -> builder.internal(index, T.evaluate(), F.evaluate())
            }
        }
    }

    fun toIntRange(): IntegerRange = IntegerRange(min.toLong() .. max.toLong())

    /**
     * Do Not Delete this function its required to utilise the super toIteFunction as the native objects don't know
     * the super functions!
     * */
    override fun toIteString() : String {return super.toIteString()}

}


/** Overloaded contains operation for allowing "AADD in range" notation */
operator fun ClosedFloatingPointRange<Double>.contains(other: AADD): Boolean =
    when (other) {
        is AADD.Leaf       -> other.max <= endInclusive && other.min >= start
        is AADD.Internal   -> this.contains(other.T) || this.contains(other.F)
    }
