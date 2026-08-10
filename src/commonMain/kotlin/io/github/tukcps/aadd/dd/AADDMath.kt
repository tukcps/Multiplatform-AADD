package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.NumericApi
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.*
import io.github.tukcps.aadd.values.real.ia.RealRange
import kotlin.math.ceil

//
// ----------------- Definition of functions based on apply of AffineForm functions --------------
//
internal interface AADDMath: NumericApi<Real, AffineForm, Double> {
    override fun add(a: AADD, b: AADD): AADD = a.apply(b, ::add)
    override fun add(a: Real, b: Double): Real = a.applyOther(b, ::add)
    override fun subtract(a: AADD, b: AADD): AADD = a.apply(b, ::subtract)
    override fun subtract(a: Real, b: Double): Real = a.applyOther(b, ::subtract)
    override fun multiply(a: AADD, b: AADD): AADD = a.apply(b, ::multiply)
    override fun multiply(a: AADD, b: Double): AADD = a.applyOther(b, ::multiply)
    override fun inv(a: AADD): AADD = a.apply(::inv)
    override fun divide(a: AADD, b: AADD): AADD = a.apply(b, ::divide)
    override fun divide(a: Real, b: Double): Real = a.applyOther(b, ::divide)

    override fun negate(value: AADD): AADD = value.apply(::negate)
    override fun sqrt(value: AADD): AADD = value.apply(::sqrt)
    override fun sqr(value: AADD): AADD = value.apply(::sqr)
    override fun pow(value: AADD, exponent: Double): AADD = value.applyOther(exponent, ::pow)
    override fun pow(value: AADD, exponent: AADD): AADD = value.apply(exponent, ::pow)
    fun pow2(value: AADD): AADD = value.apply(::power2)

    override fun root(value: AADD, degree: AADD): AADD = value.apply(degree, ::root)
    override fun root(value: AADD, degree: Double): AADD = value.applyOther(degree, ::root)
    override fun exp(value: AADD): AADD = value.apply(::exp)
    override fun ln(value: AADD): AADD = value.apply(::ln)
    override fun log(value: AADD, base: AADD) = value.apply(base, ::log)
    override fun log(value: AADD, base: Double) = value.applyOther(base, ::log)
    fun log2(value: AADD): AADD = value.apply(::log2)

    infix fun AADD.constrainTo(other: AADD): AADD = this.apply(other, ::constrainTo)
    infix fun AADD.constrainTo(other: RealRange) = this.apply(builder.real(other), ::constrainTo)

    /** piece-wise linear definition of abs() over all nodes */
    override fun abs(value: AADD): AADD = value.applySplit { x: AffineForm ->
        (value.builder.real(x).lessThan(value.builder.real(0.0))).ite(
            t = value.builder.real(negate(x)),
            e = value.builder.real(x)
        )
    }

    /** Ceil function */
    fun ceil(value: AADD): AADD = value.apply(::ceil)
    /** Inverse function of ceil function */
    fun invCeil(value: AADD): AADD = value.apply(::invCeil)
    fun floor(value: AADD): AADD = value.apply(::floor)
    fun invFloor(value: AADD): AADD = value.apply(::invFloor)

    override fun sin(value: AADD): AADD = value.apply(::sin)
    override fun cos(value: AADD): AADD = value.apply(::cos)
    override fun tan(value: AADD): AADD = value.apply(::tan)
    override fun asin(value: AADD): AADD = value.apply(::asin)
    override fun acos(value: AADD): AADD = value.apply(::acos)
    override fun atan(value: AADD): AADD = value.apply(::atan)

    //
    // --------------------- Overloaded operators -------------------
    //
    override operator fun AADD.unaryMinus(): AADD = apply(::negate)

    override operator fun AADD.plus(other: AADD): AADD = add(this, other)
    operator fun AADD.plus(other: NumberRange<DoubleBound>): AADD = apply(builder.leaf(other), ::add)
    override operator fun AADD.plus(other: Double) = apply(builder.leaf(other), ::add)

    override operator fun AADD.minus(other: AADD): AADD = subtract(this, other)
    operator fun AADD.minus(other: NumberRange<DoubleBound>): AADD = apply(builder.leaf(other), ::subtract)
    override operator fun AADD.minus(other: Double): AADD = applyOther(other, ::subtract)

    override operator fun AADD.times(other: AADD): AADD = multiply(this, other)
    operator fun AADD.times(other: NumberRange<DoubleBound>): AADD = multiply(this, builder.leaf(other))
    override operator fun AADD.times(other: Double): AADD = applyOther(other, ::multiply)

    override operator fun AADD.div(other: AADD): AADD = apply(other, ::divide)
    operator fun AADD.div(other: NumberRange<DoubleBound>): AADD = apply(builder.leaf(other), ::divide)
    override operator fun AADD.div(other: Double): AADD = applyOther(other, ::divide)

    infix fun AADD.power(other: AADD): AADD = apply(other, ::pow)

    /**
     * Calculates pow. That is, it is used for the following function: f(x,y) = x^y. The base is 'this'
     * @param exp : Double = the exponential power that the base is being raised to.
     */
    infix fun AADD.pow(exp: AffineForm): AADD = this.apply(builder.leaf(exp), ::pow)

    /**
     * Calculates nth root
     */
    // fun AADD.root(exp : AffineForm): AADD = this.apply { x: Leaf -> builder.leaf( root(x.value, exp) ) }
    // fun AADD.root(other: AADD): AADD = this.apply(other) { x: Leaf, y: Leaf -> builder.leaf( root(x.value, y.value)) }


    fun AADD.toIntRange(): IntegerRange = IntegerRange(min.toDouble().toLong(), max.toDouble().toLong())

    /**
     * ceiling function for AADDs
     */
    fun ceilAsLong(value: AADD): Long = ceil(value.getRange().max.finiteValue).toLong()

    /**
     * ceiling function for AADDs, also converts to IntegerRange
     * @return IntegerRange
     */
    fun ceilToIntRange(value: AADD): IntegerRange =
        IntegerRange(ceil(value.getRange().min.finiteValue).toLong(), ceil(value.getRange().max.finiteValue).toLong())

    /** floor function for AADDs */
    fun floorAsLong(value: AADD): Long = kotlin.math.floor(value.getRange().min.finiteValue).toLong()

    /** floor function for AADDs, also converts to IntegerRange @return IntegerRange */
    fun floorToIntRange(value: AADD): IntegerRange =
        IntegerRange(
            kotlin.math.floor(value.getRange().min.finiteValue).toLong(),
            kotlin.math.floor(value.getRange().max.finiteValue).toLong()
        )
}