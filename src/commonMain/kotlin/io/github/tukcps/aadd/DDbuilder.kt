@file:Suppress("LocalVariableName")

package io.github.tukcps.aadd

import io.github.tukcps.aadd.DD.Status
import io.github.tukcps.aadd.values.*
import io.github.tukcps.aadd.values.real.AffineForm
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.RealRange
import kotlinx.serialization.json.Json

/**
 * ### DDBuilder
 *
 * The class DDBuilder implements a factory for creating
 * instances of the classes AADD, IDD, BDD.
 *
 * The  _Public API:_ hides the implementation and its classes behind an API
 * that addresses its semantics:
 *
 * - `number (...)` creates a representation of a maybe unknown number.
 * - `real (...)` creates a representation of a maybe unknown Real number.
 * - `integer (...)` creates a representation of a maybe unknown Integer number.
 * - `string (...)` creates a representation of a maybe unknown String.
 * - `boolean (...)` creates a representation of a maybe unknown Boolean value.
 *
 * where the parameters (...) can be literals or value ranges of suitable base types (Double, Long).
 *
 * Furthermore, it can be given a lambda parameter that permits its
 * use in a 'builder' pattern.
 *
 * DDBuilder holds all the shared information for the instances:
 *  - Noise symbols
 *  - Different kind of conditions
 *  - Settings
 *
 * @param noiseVars the noise variables of affine arithmetic
 * @param config the settings
 */
class DDBuilder(
    var noiseVars: NoiseVariables = NoiseVariables(),
    var config: DDBuilderSettings = DDBuilderSettings(),
) {
    var conds: Conditions = Conditions(this)
    constructor() : this(NoiseVariables(), DDBuilderSettings()) {
        conds = Conditions(this)
    }
    /** Parameterless constructor for nice Builder in a DSL-like way */
    constructor(block: DDBuilder.() -> Unit): this() {
        this.block()
    }

    constructor(conditions: Conditions):this(){
        conds = conditions
    }

    /**
     * Creates an integer constant with given value
     * @param value the value of the integer constant
     */
    fun integer(value: Long): IDD =
        leaf(value .. value)

    /**
     * Creates an integer variable bounded to a range
     * @param range bounds of the integer
     */
    fun integer(range: ClosedRange<Long>): IDD =
        leaf(range)

    /**
     * Creates a real constant with given value
     * @param value the value of the real constant
     */
    fun real(value: Double): AADD =
        leaf(value..value)

    /**
     * Creates a real variable bounded by a range
     * @param range bounds of the real variable
     * @param symbol noise symbol as a string
     */
    fun real(range: ClosedRange<Double>, symbol: String) =
        leaf(AffineForm(this, range, symbol))

    /**
     * Creates a real variable bounded by a ranges
     * @param range - bounds of the real variable
     */
    fun real(range: ClosedRange<Double>) =
        leaf(AffineForm(this, range))

    /**
     * Creates a constant of a value typed by Number
     * @param value the value of the constant
     */
    fun number(value: Number): DD<*> =
        when(value) {
            is Double -> real(value)
            is Float -> real(value.toDouble())
            is Int -> integer(value.toLong())
            is Long -> integer(value)
            else -> throw DDException("Cannot convert $value to number")
        }

    /**
     * Creates a variable typed by ClosedRange<Number> bounded by a range
     * @param range range with the bounds
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: Comparable<T>> number(range: ClosedRange<T>): DD<*> =
        when(range.start) {
            is Double -> real(range as ClosedRange<Double>)
            is Float  -> real( (range.start as Float).toDouble() .. (range.endInclusive as Float).toDouble())
            is Int  -> integer( (range.start as Int).toLong() .. (range.endInclusive as Int).toLong())
            is Long -> integer(range as ClosedRange<Long>)
            else -> throw DDException("Cannot convert $range to number range")
        }


    /**
     * Creates a boolean variable with a given id.
     * @param id String that identifies the underlying variable in the decision diagrams
     */
    fun boolean(id: String): BDD = internal(conds.newVariable(id, this), True, False)


    /**
     * Creates a boolean constant with a given value of true or false
     * @param value the value of the boolean constant
     */
    fun boolean(value: Boolean): BDD.Leaf = constant(value)

    /**
     * Creates a string constant with a given value
     * @param value the value of the string
     */
    fun string(value: String): StrDD = StrDD.Leaf(this, value)


    enum class ApproximationScheme{
        Chebyshev,
        MinRange,
        TaylorMiddle,
        LinearRegression
    }

    var scheme = ApproximationScheme.MinRange

    fun setExternalConfig(configString: String) {
        config = jsonMapper.decodeFromString(string = configString)
    }

    /**
     * Below here, only for internal use!
     */

    /** Factory: Creates a new AADD.Leaf with an affine form as value.  */
    internal fun leaf(value: AffineForm, status: Status): AADD.Leaf = when {
        status == Status.Infeasible -> Infeasible
        value.isEmpty()     -> Empty
        else                -> AADD.Leaf(this, value.clone(), status)
    }

    /** Creates a new AADD.Leaf with an affine form as value.  */
    internal fun leaf(value: AffineForm): AADD.Leaf = when {
        value.isEmpty() -> Empty
        value.isReals() -> Reals
        value.isEmpty() -> Empty
        else -> AADD.Leaf(this, value)
    }

    internal fun leaf(value: ClosedRange<Double>): AADD.Leaf =
        leaf(AffineForm(this, value))

    internal fun leaf(value: IntegerRange) : IDD.Leaf =
        IDD.Leaf(this, value)

    internal fun leaf(value: ClosedRange<Long>) : IDD.Leaf =
        if (value.isEmpty()) EmptyIntegerRange
        else IDD.Leaf(this, IntegerRange(value))

    internal fun leaf(value: IntegerRange, status: Status): IDD.Leaf = when {
        status == Status.Infeasible -> InfeasibleI
        value.isEmpty() -> EmptyIntegerRange
        else -> IDD.Leaf(this, value, status)
    }

    internal fun leaf(value: String): StrDD.Leaf =
        StrDD.Leaf(this, value)

    /** Creates a new AADD internal node with index 'index' and child nodes T and F. */
    internal fun internal(index: Int, T: AADD, F: AADD): AADD =
        if (T is AADD.Leaf && F is AADD.Leaf && T.value.isSimilar(F.value, this.config.joinTh))
            leaf(T.value.join(F.value))
        else {
            AADD.Internal(this, index, T, F)
        }

    /** Creates a new IDD internal node with index 'index' and child nodes T and F. */
    internal fun internal(index: Int, T: IDD, F: IDD) : IDD =
        if (T is IDD.Leaf && F is IDD.Leaf && T.value==F.value)
            leaf(T.value.join(F.value))
        else
            IDD.Internal(this, index, T, F)

    internal fun internal(index: Int, T: StrDD, F: StrDD): StrDD =
        if (T is StrDD.Leaf && F is StrDD.Leaf && T.value == F.value)
            StrDD.Leaf(this, T.value)
        else
            StrDD.Internal(this, index, T, F)

    /** Use this to get a leaf node of a given Boolean value whose path can be infeasible  */
    internal fun constant(value: Boolean, status: Status): BDD =
        when {
            status == Status.Infeasible -> NaB
            value -> True
            else -> False
        }

    /** Returns one of the Boolean constants True or False as BDD */
    fun constant(value: Boolean): BDD.Leaf = if (value) True else False

    /** Returns one of the values of the extended Booleans */
    fun constant(value: XBool): BDD =
        when (value) {
            XBool.True -> True
            XBool.False -> False
            XBool.X -> Bool
            XBool.NaB -> NaB
            else -> throw Exception("Inconsistent value in BDD")
        }

    fun variable(varname: String, fromExpr: String ="noSourceExpression", isDecVar: Boolean=false): BDD =
        internal(conds.newVariable(varname, this, fromExpr, isDecVar), True, False)

    /** Creates an internal node with a given index that must refer to an existing condition. */
    internal fun internal(index: Int, T: BDD, F: BDD): BDD =
        if (T === F)  T
        else  BDD.Internal(this, index, T, F)

    private var pathConds: ArrayDeque<BDD> = ArrayDeque()

    /** Functions for modeling control-flow in a human-readable way: IF() .. END(): x = x.assignS(thenval) */
    fun IF(cond: BDD): BDD {
        pathConds.addFirst(cond)
        return  cond
    }

    fun END(): BDD = pathConds.removeFirst()

    fun ELSE(): BDD {
        val cond = END().not()
        pathConds.addFirst(cond)
        return cond
    }

    fun assign(old: BDD, new: BDD): BDD {
        var pathConjunction = pathConds[0]
        for(i in 1 until pathConds.size)pathConjunction = pathConjunction.and(pathConds[i])
        return pathConjunction.ite(new,old)
    }
    fun assign(old: AADD, new: AADD): AADD {
        var pathConjunction = pathConds[0]
        for(i in 1 until pathConds.size)pathConjunction = pathConjunction.and(pathConds[i])
        return pathConjunction.ite(new,old)
    }
    fun assign(old: IDD, new: IDD):IDD{
        var pathConjunction = pathConds[0]
        for(i in 1 until pathConds.size)pathConjunction = pathConjunction.and(pathConds[i])
        return pathConjunction.ite(new,old)
    }

    override fun toString(): String
            = "Builder: ($conds, $noiseVars)"


    /**
     * Helper function that gathers the indices of the root node of the supplied DD list, e.g. AADDs.
     * @param ddli: The list of DDs that we gather the root indices of
     * @return List of integers that represent the root indizes of the given DD list
     * */
    fun gatherIndices(ddli: MutableMap<String,DD<*>>) : MutableMap<String,Int>
    {
        val indizes = mutableMapOf<String,Int>()
        for(dd in ddli) {
            indizes[dd.key] = dd.value.index
        }
        return indizes
    }

    fun generateCDD(variables: MutableMap<String,DD<*>>, currentState: StateTuple, builder: DDBuilder) : CDD
    {
        val finalVars = mutableListOf<String>() // Variables that are already only represented by a single affine form or truth value thus not need to split them further
        for (variable in variables) {
            if(variable.value is AADD.Leaf) {
                currentState.addContinuousVar(variable.key,(variable.value as AADD.Leaf).value)
                finalVars.add(variable.key)
            }
            else if(variable.value is BDD.Leaf)
            {
                currentState.addDiscreteVar(variable.key,(variable.value as BDD.Leaf).value)
                finalVars.add(variable.key)
            }
        }

        for(variable in finalVars)variables.remove(variable)

        val indizes = gatherIndices(variables)
        var lowest = Int.MAX_VALUE

        for(indexTupel in indizes)
        {
            if(indexTupel.value < lowest) lowest = indexTupel.value
        }

        val toSplitVars = mutableListOf<String>()

        for(indexTuple in indizes)
        {
            if(indexTuple.value == lowest)
            {
                toSplitVars.add(indexTuple.key)
            }
        }

        for(variable in toSplitVars) indizes.remove(variable)// remove the entry from the map as it will be handled separately

        val tMap = mutableMapOf<String,DD<*>>()
        val fMap = mutableMapOf<String,DD<*>>()

        for(indexTuple in indizes) {
            tMap[indexTuple.key] = variables[indexTuple.key]!!
            fMap[indexTuple.key] = variables[indexTuple.key]!!
        }

        for(id in toSplitVars) {
            tMap[id] = (variables[id]!! as DD.Internal).T
            fMap[id] = (variables[id]!! as DD.Internal).F
        }

        return if(variables.isEmpty()) {
            CDD.Leaf(builder=builder,value = currentState.clone())
        } else {
            CDD.Internal(builder=builder, index = lowest,T = generateCDD(builder = builder, currentState = currentState.clone(), variables = tMap),F = generateCDD(builder = builder, currentState = currentState.clone(), variables = fMap))
        }

    }

    /** BDD Constants: True */
    val True = BDD.Leaf(this, true)
    val False = BDD.Leaf(this, false)
    val Bool = BDD.Leaf(this, XBool.X, Status.NotSolved)
    val NaB = BDD.Leaf(this, XBool.NaB, Status.Feasible)
    val InfeasibleB = BDD.Leaf(this, XBool.NaB, Status.Infeasible)

    /** AffineForm Constants */
    val AFReals  = AffineForm(this, RealRange.Reals.min, RealRange.Reals.max)
    val AFEmpty = AffineForm(this, RealRange.Empty.min, RealRange.Empty.max)

    /** AADD Constants */
    val Reals = AADD.Leaf(this, AFReals, Status.NotSolved)
    val Empty = AADD.Leaf(this, AFEmpty, Status.NotSolved)
    val Infeasible = AADD.Leaf(this, AFEmpty, Status.Infeasible)
    val RealZero = AADD.Leaf(this, AffineForm(this, 0.0))
    val RealOne = AADD.Leaf(this, AffineForm(this, 1.0))

    /** IDD Constants */
    val EmptyIntegerRange = IDD.Leaf(this, IntegerRange.Empty, Status.NotSolved)
    val Integers = IDD.Leaf(this, IntegerRange())
    val InfeasibleI = IDD.Leaf(this, IntegerRange.Empty, Status.Infeasible)
    val IntegerRangeZero = IDD.Leaf(this, IntegerRange(0))
    val IntegerRangeOne = IDD.Leaf(this, IntegerRange(1))

    /** StrDD Constants */
    val Strings = StrDD.Leaf(this, "")
    val InfeasableS = StrDD.Leaf(this, "", Status.Infeasible)
    val EmptyStrings = StrDD.Leaf(this,"", Status.NotSolved)

    var lpCalls = 0

    val jsonMapper = Json {
        prettyPrint = true
        allowSpecialFloatingPointValues = true
    }

    internal val notTable = hashMapOf<BDD, BDD.Leaf>(
        True to False,
        False to True,
        NaB to NaB,
        InfeasibleB to InfeasibleB,
        Bool to Bool
    )

    internal val andTable = hashMapOf(
        Pair(True, True) to True,
        Pair(True, False) to False,
        Pair(True, Bool) to Bool,
        Pair(True, NaB) to NaB,
        Pair(True, InfeasibleB) to InfeasibleB,

        Pair(False, False) to False,
        Pair(False, True) to False,
        Pair(False, Bool) to False,
        Pair(False, NaB) to NaB,
        Pair(False, InfeasibleB) to InfeasibleB,

        Pair(Bool, False) to False,
        Pair(Bool, True) to Bool,
        Pair(Bool, Bool) to Bool,
        Pair(Bool, NaB) to NaB,
        Pair(Bool, InfeasibleB) to InfeasibleB,

        Pair(NaB, False) to NaB,
        Pair(NaB, True) to NaB,
        Pair(NaB, NaB) to NaB,
        Pair(NaB, Bool) to NaB,
        Pair(NaB, InfeasibleB) to InfeasibleB,

        Pair(InfeasibleB, False) to InfeasibleB,
        Pair(InfeasibleB, True) to InfeasibleB,
        Pair(InfeasibleB, NaB) to InfeasibleB,
        Pair(InfeasibleB, Bool) to InfeasibleB,
        Pair(InfeasibleB, InfeasibleB) to InfeasibleB,
    )


    internal val orTable = hashMapOf<Pair<BDD, BDD>, BDD.Leaf>(
        Pair(True, True) to True,
        Pair(True, False) to True,
        Pair(True, Bool) to True,
        Pair(True, NaB) to NaB,
        Pair(True, InfeasibleB) to InfeasibleB,

        Pair(False, False) to False,
        Pair(False, True) to True,
        Pair(False, Bool) to Bool,
        Pair(False, NaB) to NaB,
        Pair(False, InfeasibleB) to InfeasibleB,

        Pair(Bool, False) to Bool,
        Pair(Bool, True) to True,
        Pair(Bool, Bool) to Bool,
        Pair(Bool, NaB) to NaB,
        Pair(Bool, InfeasibleB) to InfeasibleB,

        Pair(NaB, False) to NaB,
        Pair(NaB, True) to NaB,
        Pair(NaB, NaB) to NaB,
        Pair(NaB, Bool) to NaB,
        Pair(NaB, InfeasibleB) to InfeasibleB,

        Pair(InfeasibleB, False) to InfeasibleB,
        Pair(InfeasibleB, True) to InfeasibleB,
        Pair(InfeasibleB, NaB) to InfeasibleB,
        Pair(InfeasibleB, Bool) to InfeasibleB,
        Pair(InfeasibleB, InfeasibleB) to InfeasibleB,
    )


    internal val nandTable = hashMapOf<Pair<BDD, BDD>, BDD.Leaf>(
        Pair(True, True) to False,
        Pair(True, False) to True,
        Pair(True, Bool) to Bool,
        Pair(True, NaB) to NaB,
        Pair(True, InfeasibleB) to InfeasibleB,

        Pair(False, False) to True,
        Pair(False, True) to True,
        Pair(False, Bool) to True,
        Pair(False, NaB) to NaB,
        Pair(False, InfeasibleB) to InfeasibleB,

        Pair(Bool, False) to True,
        Pair(Bool, True) to Bool,
        Pair(Bool, Bool) to Bool,
        Pair(Bool, NaB) to NaB,
        Pair(Bool, InfeasibleB) to InfeasibleB,

        Pair(NaB, False) to NaB,
        Pair(NaB, True) to NaB,
        Pair(NaB, NaB) to NaB,
        Pair(NaB, Bool) to NaB,
        Pair(NaB, InfeasibleB) to InfeasibleB,

        Pair(InfeasibleB, False) to InfeasibleB,
        Pair(InfeasibleB, True) to InfeasibleB,
        Pair(InfeasibleB, NaB) to InfeasibleB,
        Pair(InfeasibleB, Bool) to InfeasibleB,
        Pair(InfeasibleB, InfeasibleB) to InfeasibleB,
    )


    internal val xorTable = hashMapOf<Pair<BDD, BDD>, BDD.Leaf>(
        Pair(True, True) to False,
        Pair(True, False) to True,
        Pair(True, Bool) to Bool,
        Pair(True, NaB) to NaB,
        Pair(True, InfeasibleB) to InfeasibleB,

        Pair(False, False) to False,
        Pair(False, True) to True,
        Pair(False, Bool) to Bool,
        Pair(False, NaB) to NaB,
        Pair(False, InfeasibleB) to InfeasibleB,

        Pair(Bool, False) to Bool,
        Pair(Bool, True) to Bool,
        Pair(Bool, Bool) to Bool,
        Pair(Bool, NaB) to NaB,
        Pair(Bool, InfeasibleB) to InfeasibleB,

        Pair(NaB, False) to NaB,
        Pair(NaB, True) to NaB,
        Pair(NaB, NaB) to NaB,
        Pair(NaB, Bool) to NaB,
        Pair(NaB, InfeasibleB) to InfeasibleB,

        Pair(InfeasibleB, False) to InfeasibleB,
        Pair(InfeasibleB, True) to InfeasibleB,
        Pair(InfeasibleB, NaB) to InfeasibleB,
        Pair(InfeasibleB, Bool) to InfeasibleB,
        Pair(InfeasibleB, InfeasibleB) to InfeasibleB,
    )


    /** the intersect operation on two XBool checks for the possible equality */
    internal val intersectTable = hashMapOf(
        Pair(True, True ) to True,
        Pair(True, False) to NaB,
        Pair(True, Bool) to True,
        Pair(True, NaB) to NaB,
        Pair(True, InfeasibleB) to InfeasibleB,

        Pair(False, True ) to NaB,
        Pair(False, False) to False,
        Pair(False, Bool) to False,
        Pair(False, NaB) to NaB,
        Pair(False, InfeasibleB) to InfeasibleB,

        Pair(Bool, True ) to True,
        Pair(Bool, False) to False,
        Pair(Bool, Bool) to Bool,
        Pair(Bool, NaB) to NaB,
        Pair(Bool, InfeasibleB) to InfeasibleB,

        Pair(NaB, True ) to NaB,
        Pair(NaB, False) to NaB,
        Pair(NaB, Bool) to NaB,
        Pair(NaB, NaB) to NaB,
        Pair(NaB, InfeasibleB) to InfeasibleB,

        Pair(InfeasibleB, True ) to InfeasibleB,
        Pair(InfeasibleB, False) to InfeasibleB,
        Pair(InfeasibleB, Bool) to InfeasibleB,
        Pair(InfeasibleB, NaB) to InfeasibleB,
        Pair(InfeasibleB, InfeasibleB) to InfeasibleB,
    )
}
