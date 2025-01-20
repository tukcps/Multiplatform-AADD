package com.github.tukcps.aadd.values

/**
 * NumberRange is the common interface for different kind of range arithmetics
 * on different kind of value types: Long, Double, using IA and AA.
 * The implementations shall handle
 * - overflows
 * - rounding errors such that a safe inclusion is guaranteed
 * - exceptions, e.g., division by zero.
 * The implementation shall provide immutable objects.
 */
interface NumberRange <T: Comparable<T> >: ClosedRange<T> {
    /** The interface property of ClosedFloatingPointRange is mapped to min/max */
    override val start: T get() = min
    override val endInclusive: T get() = max
    val min: T
    val max: T

    fun copy(min: T?, max: T?): NumberRange<T>

    override fun isEmpty(): Boolean = (min > max) || !(min == min) || !(max == max)
    val maxIsInf: Boolean
    val minIsInf: Boolean

    fun isScalar() = (min == max)
    fun isRange() = max > min
    fun isZero(): Boolean
    fun isOne(): Boolean

    override operator fun contains(value: T): Boolean = value in min..max
    operator fun contains(value: NumberRange<T>) = this.min <= value.min && this.max >= value.max

    infix fun join(other: NumberRange<T>): NumberRange<T>
    infix fun union(other: NumberRange<T>): NumberRange<T>
    infix fun intersect(other: NumberRange<T>): NumberRange<T>

    fun greaterThan(other: NumberRange<T>): XBool
    fun greaterThan(other: T): XBool
    fun greaterThanOrEquals(other: NumberRange<T>): XBool
    fun greaterThanOrEquals(other: T): XBool
    fun lessThan(other: NumberRange<T>): XBool
    fun lessThan(other: T): XBool
    fun lessThanOrEquals(other: NumberRange<T>): XBool
    fun lessThanOrEquals(other: T): XBool

    operator fun div(other: NumberRange<T>): NumberRange<T>
    operator fun div(other: T): NumberRange<T>
    operator fun minus(other: NumberRange<T>): NumberRange<T>
    operator fun minus(other: T): NumberRange<T>
    operator fun plus(other: NumberRange<T>): NumberRange<T>
    operator fun plus(other: T): NumberRange<T>
    operator fun times(other: NumberRange<T>): NumberRange<T>
    operator fun times(other: T): NumberRange<T>
    operator fun unaryMinus(): NumberRange<T>

    fun pow(other: T): NumberRange<T>
    fun pow(other: NumberRange<T>): NumberRange<T>
    fun sqr() : NumberRange<T>
    fun sqrt() : NumberRange<T>
    fun root(other: NumberRange<T>): NumberRange<T> //nth root (other=n)
    fun exp(): NumberRange<T>
    fun log(): NumberRange<T>
    fun log(other: NumberRange<T>): NumberRange<T>
}