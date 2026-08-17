package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.Integer
import io.github.tukcps.aadd.NumericApi
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.integer.*
import io.github.tukcps.aadd.values.real.aa.power2


interface IDDMath: NumericApi<Integer, IntegerRange, Long> {

    //
    // ------------------ Overloaded operators --------------------
    //
    override operator fun IDD.unaryMinus(): IDD = this.apply(::negate)

    override operator fun IDD.plus(other: IDD): IDD = add(this, other)
    operator fun IDD.plus(other: NumberRange<LongBound>): IDD = this.apply(builder.leaf(other), ::add )
    override operator fun IDD.plus(other: Long): IDD = add(this, other)

    override operator fun IDD.minus(other: IDD): IDD = subtract(this, other)
    operator fun IDD.minus(other: NumberRange<LongBound>) = this.apply(builder.leaf(other), ::subtract)
    override operator fun IDD.minus(other: Long): IDD = subtract(this, other)

    override operator fun IDD.times(other: IDD): IDD = this.apply(builder.leaf(other), ::multiply)
    operator fun IDD.times(other: NumberRange<LongBound>) = this.apply(builder.leaf(other), ::multiply)
    override operator fun IDD.times(other: Long): IDD = this.apply(builder.leaf(other), ::multiply)

    override operator fun IDD.div(other: IDD): IDD = this.apply(other, ::divide)
    operator fun IDD.div(other: NumberRange<LongBound>) = this.apply(builder.leaf(other), ::divide)
    override operator fun IDD.div(other: Long): IDD = this.apply(builder.leaf(other), ::divide)

    override fun negate(value: IDD): Integer = value.apply(::negate)
    override fun abs(value: Integer): Integer = value.apply(::abs)

    // --------- To be fixed, makes no sense  ---------
    override fun add(a: Integer, b: Integer): Integer = a.apply(b, ::add)
    override fun add(a: Integer, b: Long): Integer= a.applyOther(b, ::add)
    override fun subtract(a: Integer, b: Integer): Integer = a.apply(b, ::subtract)
    override fun subtract(a: Integer, b: Long): Integer = a.applyOther(b, ::subtract)
    override fun multiply(a: Integer, b: Integer): Integer = a.apply(b, ::multiply)
    override fun multiply(a: Integer, b: Long): Integer { TODO("Not yet implemented") }
    override fun divide(a: Integer, b: Integer): Integer = a.apply(b, ::divide)
    override fun divide(a: Integer, b: Long): Integer { TODO("Not yet implemented") }

    override fun inv(a: Integer): Integer = a.apply(::inv)
    override fun exp(value: IDD): IDD = value.apply(::exp)
    override fun sqr(value: IDD): IDD = value.apply(::sqr)
    override fun sqrt(value: IDD): IDD = value.apply(::sqrt)
    override fun ln(value: IDD): IDD = value.apply(::ln)
    override fun log(value: Integer, base: Integer): Integer = value.apply(base, ::log)
    override fun log(value: Integer, base: Long): Integer = value.applyOther(base, ::log)
    override fun pow(value: Integer, exponent: Integer): Integer = value.apply(exponent, ::pow)
    override fun pow(value: Integer, exponent: Long): Integer = value.applyOther(exponent, ::pow)
    fun pow2(value: IDD): IDD = value.apply(::pow2)
    fun pow10(value: Integer): IDD = value.apply(::pow10)

    override fun root(value: Integer, degree: Integer): Integer = value.apply(degree, ::root)
    override fun root(value: Integer, degree: Long): Integer = value.applyOther(degree, ::root)

    // --------- Trigonometric functions to be moved to separate API ----------
    override fun sin(value: Integer): Integer { TODO("Not implemented") }
    override fun asin(value: Integer): Integer { TODO("Not implemented") }
    override fun cos(value: Integer): Integer { TODO("Not implemented") }
    override fun acos(value: Integer): Integer { TODO("Not implemented") }
    override fun tan(value: Integer): Integer { TODO("Not implemented") }
    override fun atan(value: Integer): Integer { TODO("Not implemented") }
    // fun log(value: IDD, other: NumberRange<LongBound>): IDD = value.apply(value.builder.integer(other), ::log)

    /*
    /** Computes x^y */
    fun pow(other: Long): IDD = this.apply { x: Leaf -> builder.leaf(x.value.pow(other)) }

    /** Computes x^y */
    fun pow(other: NumberRange<Long>): IDD = this.apply { x: Leaf -> builder.leaf(x.value.pow(other)) }

    /** Computes x^y */
    fun pow(exp: IDD): IDD = this.apply(exp) { a: Leaf, b: Leaf -> builder.leaf(a.value.pow(b.value)) }



    fun IDD.root(other: NumberRange<LongBound>): IDD =
        this.apply { x: IDD.Leaf -> builder.leaf(x.value.root(other)) }


    /** Intersection of a leaf and an Interval returns a leaf */
    fun constrainTo(idd: IDD.Leaf, range: ClosedRange<Long>) =
        idd.builder.leaf(idd.value intersect IntegerRange(range.start, range.endInclusive))

    /**
     * Calculates pow for IDD
     * That is, it is used for the following function: f(x,y) = x^y
     * @param base the base, or number, which is being raised to an exponent,
     * i.e., multiplied by itself that number of times
     * @param exponent = the exponential power that the base is being raised to.
     */
    fun pow(base : IDD, exp : Long) : IDD = base.pow(exp)

    /**
     * Calculates pow for IDD
     * That is, it is used for the following function: f(x,y) = x^y
     * @param base the base, or number, which is being raised to an exponent,
     * i.e., multiplied by itself that number of times
     * @param exponent = the exponential power that the base is being raised to.
     */
    fun pow(base : IDD, exp : IDD) : IDD = base.pow(exp)
    */
}