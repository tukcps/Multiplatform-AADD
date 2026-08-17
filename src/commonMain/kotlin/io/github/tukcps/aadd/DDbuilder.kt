@file:Suppress("LocalVariableName", "PropertyName", "FunctionName")

package io.github.tukcps.aadd

import io.github.tukcps.aadd.DDBuilder.BoolMath.and
import io.github.tukcps.aadd.DDBuilder.BoolMath.not
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.dd.DD.Status
import io.github.tukcps.aadd.dd.Str
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.ScalarValue
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.NoiseVariables
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.toDoubleBound
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmName

typealias Real = AADD
typealias Integer = IDD
typealias Bool = BDD
typealias Str = StrDD

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
 * Other constants are (for any DD), addressing the use in constraint propagation techniques:
 * - Empty (no elements of a type; bottom set)
 * - Zero - a single element with domain-specific meaning (False, 0.0, 0.0)
 * - One - a single element with domain-specific meaning (True, 1.0, 1)
 * - All (all elements of a type interpreted as a set)
 *
 * DDBuilder furthermore holds (internally) all the shared information for the instances:
 *  - Noise symbols of DD-constrained affine arithmetic
 *  - Conditions (path constraints) of internal leaves of DDs
 *  - Settings
 *
 * @param settings the settings
 */
class DDBuilder(
    internal var settings: DDBuilderSettings = DDBuilderSettings()
) {
    /** Parameterless constructor the uses default settins. */
    constructor() : this(DDBuilderSettings())

    /** Constructor with single lambda for DSL-like programming */
    constructor(block: DDBuilder.() -> Unit): this() { this.block() }

    /**
     * Manager for the noise symbols of constrained affine arithmetic.
     */
    internal val noiseVariables = NoiseVariables(this)

    /**
     * Manager for the path conditions (predicates on internal nodes of DDs).
     */
    val conditions = Conditions(this)
    @Deprecated("Replace with conditions", ReplaceWith("conditions"))
    val conds: Conditions get() = conditions

    /**
     * Creates an Integer scalar with given finite Long value.
     * @param scalar the value of the integer constant as Long.
     */
    fun integer(scalar: Long): Integer =
        leaf(IntegerRange(scalar))

    /**
     * Creates an integer scalar with given value that may also be infinite.
     * @param scalar the value of the integer constant
     */
    fun integer(scalar: LongBound): Integer =
        leaf(IntegerRange(scalar))

    /**
     * Crates an integer range that may have infinite bounds.
     * @param range the range
     */
    @JvmName("integerLongBound")
    fun integer(range: ClosedRange<LongBound>): Integer =
        leaf(IntegerRange(range))

    /**
     * Creates an integer variable bounded to a range that may only be finite.
     * @param range bounds of the integer
     */
    fun integer(range: ClosedRange<Long>): Integer =
        leaf(IntegerRange(range.start, range.endInclusive))

    /**
     * Creates a real variable modeld by an affine form
     * @param af affine form
     * @param symbol noise symbol as a string
     */
    internal fun real(af: AffineForm): Real =
        leaf(af)

    /**
     * Creates a real constant with given value
     * @param value the value of the real constant
     */
    fun real(value: Double): Real =
        leaf(RealRange(value, value))

    /**
     * Creates a real variable bounded by a range
     * @param range bounds of the real variable
     * @param id noise symbol as a string
     */
    fun real(range: ClosedRange<DoubleBound>, id: String? = null): Real =
        leaf(AffineForm.range(this, range.start, range.endInclusive, id))

    /**
     * Creates a real variable bounded by a range
     * @param range - bounds of the real variable
     */
    fun real(range: ClosedRange<Double>, id: String? = null) =
        leaf(AffineForm.range(this,
            range.start.toDoubleBound() ?: DoubleBound.NegativeInfinity,
            range.endInclusive.toDoubleBound() ?: DoubleBound.PositiveInfinity,
            id)
        )

    /**
     * Creates a constant of a value typed by Number
     * @param value the value of the constant
     */
    fun number(value: Number): DD<*> =
        when(value) {
            is Double   -> real(value)
            is Float    -> real(value.toDouble())
            is Int      -> integer(value.toLong()..value.toLong())
            is Long     -> integer(value .. value)
            else -> throw DDException("Cannot convert $value to number")
        }

    /**
     * Creates a variable typed by ClosedRange<Number> bounded by a finite range
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
    fun boolean(id: String): BDD = internal(conditions.newVariable(id, this), Bool.True, Bool.False)

    /**
     * Creates a boolean constant with a given value of true or false
     * @param value the value of the boolean constant
     */
    fun boolean(value: Boolean): BDD.Leaf = constant(value)

    /**
     * Creates a string constant with a given value
     * @param value the value of the string
     */
    fun string(value: String): StrDD = StrDD.Leaf(this, Str(value))

    @Deprecated("No longer needed")
    enum class ApproximationScheme{
        Chebyshev,
        MinRange,
        TaylorMiddle,
        LinearRegression
    }

    fun setExternalConfig(configString: String) {
        settings = jsonMapper.decodeFromString(string = configString)
    }

    //
    // -------------- Below here, only for internal use! -----------------
    //

    /** Factory: Creates a new AADD.Leaf with an affine form as value.  */
    internal fun leaf(value: AffineForm, status: Status): AADD.Leaf = when {
        status == Status.Infeasible -> Reals.Infeasible
        value.isEmpty()     -> Reals.Empty
        else                -> AADD.Leaf(this, value.clone(), status)
    }

    /** Creates a new AADD.Leaf with an affine form as value.  */
    internal fun leaf(value: AffineForm): AADD.Leaf = when {
        value.isEmpty() -> Reals.Empty
        value.isReals() -> Reals.All
        else -> AADD.Leaf(this, value)
    }

    internal fun leaf(value: NumberRange<DoubleBound>): AADD.Leaf =
        leaf(AffineForm.range(this, value))

    internal fun leaf(value: NumberRange<LongBound>, status: Status = Status.NotSolved): IDD.Leaf = when {
        status == Status.Infeasible -> Integers.Infeasible
        value.isEmpty()             -> Integers.Empty
        else                        -> IDD.Leaf(this, IntegerRange(value), status)
    }

    internal fun leaf(value: String): StrDD.Leaf =
        StrDD.Leaf(this, Str(value))

    /**
     * Generic creation of internal node.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <DDType: DD<ValueType>, ValueType: ScalarValue> internal(index: Int, T: DDType, F: DDType): DDType =
        if (T is DD.Leaf<*> && F is DD.Leaf<*> && T.value == F.value)
            if (T is BDD) T else leaf(T.value as ValueType)
        else when (T) {
            is AADD -> AADD.Internal(this, index, T, F as AADD) as DDType
            is BDD -> BDD.Internal(this, index, T, F as BDD) as DDType
            is IDD -> IDD.Internal(this, index, T, F as IDD) as DDType
            is StrDD -> StrDD.Internal(this, index, T, F as StrDD) as DDType
        }

    /**
     * Generic creation of leaf node.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <DDType: DD<ScalarValue>, ValueType: Any> leaf(value: ValueType): DDType = when (value) {
        is AffineForm   -> leaf(value as AffineForm) as DDType
        is IntegerRange -> leaf(value as IntegerRange) as DDType
        is String       -> leaf(value as String) as DDType
        is Long         -> integer(IntegerRange(value)) as DDType
        is Double       -> leaf(RealRange(value, value)) as DDType
        is DD.Leaf<*>   -> value as DDType
        else -> TODO()
    }

    /** Creates a new AADD internal node with index 'index' and child nodes T and F. */
    internal fun internal(index: Int, T: AADD, F: AADD): AADD =
        if (T is AADD.Leaf && F is AADD.Leaf && T.value.isSimilar(F.value, this.settings.ddJoinLeavesThreshold))
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

    /*
    internal fun internal(index: Int, T: StrDD, F: StrDD): StrDD =
        if (T is StrDD.Leaf && F is StrDD.Leaf && T.value == F.value)
            StrDD.Leaf(this, T.value)
        else
            StrDD.Internal(this, index, T, F) */

    /** Use this to get a leaf node of a given Boolean value whose path can be infeasible  */
    internal fun constant(value: Boolean, status: Status): BDD =
        when {
            status == Status.Infeasible -> Bool.Empty
            value -> Bool.True
            else -> Bool.False
        }

    /** Returns one of the Boolean constants True or False as BDD */
    fun constant(value: Boolean): BDD.Leaf = if (value) Bool.True else Bool.False

    /** Returns one of the values of the extended Booleans */
    fun constant(value: XBool): BDD =
        when (value) {
            XBool.Empty -> Bool.Empty
            XBool.True -> Bool.True
            XBool.False -> Bool.False
            XBool.All -> Bool.All
            else -> throw Exception("Inconsistent value in BDD")
        }

    fun variable(varname: String, fromExpr: String ="noSourceExpression", isDecVar: Boolean=false): BDD =
        internal(conditions.newVariable(varname, this, fromExpr, isDecVar), Bool.True, Bool.False)

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
        for(i in 1 until pathConds.size)pathConjunction = pathConjunction and pathConds[i]
        return pathConjunction.ite(new,old)
    }

    fun assign(old: AADD, new: AADD): AADD {
        var pathConjunction = pathConds[0]
        for(i in 1 until pathConds.size) pathConjunction = pathConjunction and pathConds[i]
        return pathConjunction.ite(new,old)
    }

    fun assign(old: IDD, new: IDD):IDD{
        var pathConjunction = pathConds[0]
        for(i in 1 until pathConds.size) pathConjunction = pathConjunction and pathConds[i]
        return pathConjunction.ite(new,old)
    }

    override fun toString(): String
            = "Builder: ($conditions, $noiseVariables)"


    /**
     * Helper function that gathers the indices of the root node of the supplied DD list, e.g. AADDs.
     * @param ddli: The list of DDs that we gather the root indices of
     * @return List of integers that represent the root indizes of the given DD list
     * */
    fun <T: ScalarValue> gatherIndices (ddli: MutableMap<String, DD<T>>) : MutableMap<String,Int> {
        val indizes = mutableMapOf<String,Int>()
        for((key, value) in ddli) {
            indizes[key] = value.index
        }
        return indizes
    }

    /** BDD Constants: True */
    val Bool = BDDNamespace()
    @Deprecated("Replaced by Bool", replaceWith = ReplaceWith("Bool"))
    val Boolean: BDDNamespace get() = Bool
    inner class BDDNamespace {
        val Infeasible = BDD.Leaf(this@DDBuilder, XBool.Empty, Status.Infeasible)
        val Empty = BDD.Leaf(this@DDBuilder, XBool.Empty, Status.Feasible)
        val Zero = BDD.Leaf(this@DDBuilder, false)
        val False: BDD.Leaf get() = Zero
        val One = BDD.Leaf(this@DDBuilder, true)
        val True: BDD.Leaf get() = One
        val All = BDD.Leaf(this@DDBuilder, XBool.All, Status.NotSolved)
        @Deprecated("Replace with All", ReplaceWith("All"))
        val Bool: BDD.Leaf get() = All
    }
    object BoolMath: BDDMath
    inline fun <R> boolMath(block: BoolMath.() -> R): R = BoolMath.block()

    /**
     * AffineForm Constants for All, Empty, Zero, One (internal)
     * Don't use independenty - splits could return AADD.
     * Use just Real, Integer, Bool.
     */
    internal val AF = AFNamespace()
    inner class AFNamespace {
        val All   = AffineForm(this@DDBuilder, RealRange.Reals.min, RealRange.Reals.max, Double.NaN, hashMapOf())
        val Empty = AffineForm(this@DDBuilder, RealRange.Empty.min, RealRange.Empty.max, Double.NaN, hashMapOf())
        val Zero  = AffineForm(this@DDBuilder, RealRange.Zero.min, RealRange.Zero.max, 0.0, hashMapOf())
        val One   = AffineForm(this@DDBuilder, RealRange.One.min, RealRange.One.max, 1.0, hashMapOf())
    }

    /** Constants for the Reals */
    val Reals = AADDNamespace()
    inner class AADDNamespace {
        val Infeasible = AADD.Leaf(this@DDBuilder, AF.Empty, Status.Infeasible)
        val Empty = AADD.Leaf(this@DDBuilder, AF.Empty, Status.NotSolved)
        val Zero  = AADD.Leaf(this@DDBuilder, AF.Zero)
        val One   = AADD.Leaf(this@DDBuilder, AF.One)
        val All   = AADD.Leaf(this@DDBuilder, AF.All, Status.NotSolved)
        @Deprecated("Replace with All", ReplaceWith("All"))
        val Reals: AADD.Leaf  get() = All
    }
    /** Operations, Functions on Reals, based on AADD */
    object RealMath: AADDMath
    inline fun <R> realMath(block: RealMath.() -> R): R = RealMath.block()

    /** Constants for the Integers */
    val Integers = IDDNamespace()
    @Deprecated("Replaced by Integers.Empty", replaceWith = ReplaceWith("Integers.Empty"))
    val EmptyIntegerRange get() = Integers.Empty
    inner class IDDNamespace {
        val Infeasible = IDD.Leaf(this@DDBuilder, IntegerRange.Empty, Status.Infeasible)
        val Empty = IDD.Leaf(this@DDBuilder, IntegerRange.Empty, Status.NotSolved)
        val All = IDD.Leaf(this@DDBuilder, IntegerRange())
        val Zero = IDD.Leaf(this@DDBuilder, IntegerRange(LongBound.Finite(0)))
        val One = IDD.Leaf(this@DDBuilder, IntegerRange(LongBound.Finite(1)))
        @Deprecated("Replace with All", ReplaceWith("All"))
        val Integers: IDD.Leaf  get() = All
    }

    /** Operations for Integers, based on IDD */
    object IntMath: IDDMath
    inline fun <R> intMath(block: IntMath.() -> R): R = IntMath.block()

    /** StrDD Constants -- just a try */
    val Strings = StrDDNamespace()
    inner class StrDDNamespace {
        val Infeasible = StrDD.Leaf(this@DDBuilder, Str(""), Status.Infeasible)
        val Empty = StrDD.Leaf(this@DDBuilder, Str(""), Status.NotSolved)
        val All = StrDD.Leaf(this@DDBuilder, Str("*"))
    }

    //
    // ------------------------ Other stuff --------------------------
    //
    var lpCalls = 0

    val jsonMapper = Json {
        prettyPrint = true
        allowSpecialFloatingPointValues = true
    }

    internal val notTable = hashMapOf<BDD, BDD.Leaf>(
        Bool.True to Bool.False,
        Bool.False to Bool.True,
        Bool.Empty to Bool.Empty,
        Bool.Infeasible to Bool.Infeasible,
        Bool.All to Bool.All
    )

    internal val andTable = hashMapOf(
        Pair(Bool.True, Bool.True) to Bool.True,
        Pair(Bool.True, Bool.False) to Bool.False,
        Pair(Bool.True, Bool.All) to Bool.All,
        Pair(Bool.True, Bool.Empty) to Bool.Empty,
        Pair(Bool.True, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.False, Bool.False) to Bool.False,
        Pair(Bool.False, Bool.True) to Bool.False,
        Pair(Bool.False, Bool.All) to Bool.False,
        Pair(Bool.False, Bool.Empty) to Bool.Empty,
        Pair(Bool.False, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.All, Bool.False) to Bool.False,
        Pair(Bool.All, Bool.True) to Bool.All,
        Pair(Bool.All, Bool.All) to Bool.All,
        Pair(Bool.All, Bool.Empty) to Bool.Empty,
        Pair(Bool.All, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Empty, Bool.False) to Bool.Empty,
        Pair(Bool.Empty, Bool.True) to Bool.Empty,
        Pair(Bool.Empty, Bool.Empty) to Bool.Empty,
        Pair(Bool.Empty, Bool.All) to Bool.Empty,
        Pair(Bool.Empty, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Infeasible, Bool.False) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.True) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Empty) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.All) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Infeasible) to Bool.Infeasible,
    )

    internal val orTable = hashMapOf<Pair<BDD, BDD>, BDD.Leaf>(
        Pair(Bool.True, Bool.True) to Bool.True,
        Pair(Bool.True, Bool.False) to Bool.True,
        Pair(Bool.True, Bool.All) to Bool.True,
        Pair(Bool.True, Bool.Empty) to Bool.Empty,
        Pair(Bool.True, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.False, Bool.False) to Bool.False,
        Pair(Bool.False, Bool.True) to Bool.True,
        Pair(Bool.False, Bool.All) to Bool.All,
        Pair(Bool.False, Bool.Empty) to Bool.Empty,
        Pair(Bool.False, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.All, Bool.False) to Bool.All,
        Pair(Bool.All, Bool.True) to Bool.True,
        Pair(Bool.All, Bool.All) to Bool.All,
        Pair(Bool.All, Bool.Empty) to Bool.Empty,
        Pair(Bool.All, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Empty, Bool.False) to Bool.Empty,
        Pair(Bool.Empty, Bool.True) to Bool.Empty,
        Pair(Bool.Empty, Bool.Empty) to Bool.Empty,
        Pair(Bool.Empty, Bool.All) to Bool.Empty,
        Pair(Bool.Empty, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Infeasible, Bool.False) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.True) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Empty) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.All) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Infeasible) to Bool.Infeasible,
    )

    internal val nandTable = hashMapOf<Pair<BDD, BDD>, BDD.Leaf>(
        Pair(Bool.True, Bool.True) to Bool.False,
        Pair(Bool.True, Bool.False) to Bool.True,
        Pair(Bool.True, Bool.All) to Bool.All,
        Pair(Bool.True, Bool.Empty) to Bool.Empty,
        Pair(Bool.True, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.False, Bool.False) to Bool.True,
        Pair(Bool.False, Bool.True) to Bool.True,
        Pair(Bool.False, Bool.All) to Bool.True,
        Pair(Bool.False, Bool.Empty) to Bool.Empty,
        Pair(Bool.False, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.All, Bool.False) to Bool.True,
        Pair(Bool.All, Bool.True) to Bool.All,
        Pair(Bool.All, Bool.All) to Bool.All,
        Pair(Bool.All, Bool.Empty) to Bool.Empty,
        Pair(Bool.All, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Empty, Bool.False) to Bool.Empty,
        Pair(Bool.Empty, Bool.True) to Bool.Empty,
        Pair(Bool.Empty, Bool.Empty) to Bool.Empty,
        Pair(Bool.Empty, Bool.All) to Bool.Empty,
        Pair(Bool.Empty, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Infeasible, Bool.False) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.True) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Empty) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.All) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Infeasible) to Bool.Infeasible,
    )

    internal val xorTable = hashMapOf<Pair<BDD, BDD>, BDD.Leaf>(
        Pair(Bool.True, Bool.True) to Bool.False,
        Pair(Bool.True, Bool.False) to Bool.True,
        Pair(Bool.True, Bool.All) to Bool.All,
        Pair(Bool.True, Bool.Empty) to Bool.Empty,
        Pair(Bool.True, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.False, Bool.False) to Bool.False,
        Pair(Bool.False, Bool.True) to Bool.True,
        Pair(Bool.False, Bool.All) to Bool.All,
        Pair(Bool.False, Bool.Empty) to Bool.Empty,
        Pair(Bool.False, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.All, Bool.False) to Bool.All,
        Pair(Bool.All, Bool.True) to Bool.All,
        Pair(Bool.All, Bool.All) to Bool.All,
        Pair(Bool.All, Bool.Empty) to Bool.Empty,
        Pair(Bool.All, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Empty, Bool.False) to Bool.Empty,
        Pair(Bool.Empty, Bool.True) to Bool.Empty,
        Pair(Bool.Empty, Bool.Empty) to Bool.Empty,
        Pair(Bool.Empty, Bool.All) to Bool.Empty,
        Pair(Bool.Empty, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Infeasible, Bool.False) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.True) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Empty) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.All) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Infeasible) to Bool.Infeasible,
    )

    /** the intersect operation on two XBool checks for the possible equality */
    internal val intersectTable = hashMapOf(
        Pair(Bool.True, Bool.True ) to Bool.True,
        Pair(Bool.True, Bool.False) to Bool.Empty,
        Pair(Bool.True, Bool.All) to Bool.True,
        Pair(Bool.True, Bool.Empty) to Bool.Empty,
        Pair(Bool.True, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.False, Bool.True ) to Bool.Empty,
        Pair(Bool.False, Bool.False) to Bool.False,
        Pair(Bool.False, Bool.All) to Bool.False,
        Pair(Bool.False, Bool.Empty) to Bool.Empty,
        Pair(Bool.False, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.All, Bool.True ) to Bool.True,
        Pair(Bool.All, Bool.False) to Bool.False,
        Pair(Bool.All, Bool.All) to Bool.All,
        Pair(Bool.All, Bool.Empty) to Bool.Empty,
        Pair(Bool.All, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Empty, Bool.True ) to Bool.Empty,
        Pair(Bool.Empty, Bool.False) to Bool.Empty,
        Pair(Bool.Empty, Bool.All) to Bool.Empty,
        Pair(Bool.Empty, Bool.Empty) to Bool.Empty,
        Pair(Bool.Empty, Bool.Infeasible) to Bool.Infeasible,

        Pair(Bool.Infeasible, Bool.True ) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.False) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.All) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Empty) to Bool.Infeasible,
        Pair(Bool.Infeasible, Bool.Infeasible) to Bool.Infeasible,
    )
}
