package io.github.tukcps.aadd

import io.github.tukcps.aadd.dd.DD
import io.github.tukcps.aadd.values.NumericValue

/**
 * Operators and functions for Numeric types.
 */
interface NumericApi<DDType: DD<ValueType>, ValueType: NumericValue, Number: Any> { //, Number: Any>: DDApi<DDType> {

    // ------------ Arithmetic operators --------------
    operator fun DDType.plus(other: DDType): DDType = add(this, other)
    operator fun DDType.plus(other: Number): DDType = add(this, other)
    operator fun DDType.minus(other: DDType): DDType = subtract(this, other)
    operator fun DDType.minus(other: Number): DDType = subtract(this, other)
    operator fun DDType.times(other: DDType): DDType = multiply(this, other)
    operator fun DDType.times(other: Number): DDType = multiply(this, other)
    operator fun DDType.div(other: DDType): DDType = divide(this, other)
    operator fun DDType.div(other: Number): DDType = divide(this, other)
    operator fun DDType.unaryMinus(): DDType = negate(this)

    // ------------ Arithmetic functions ---------------
    fun add(a: DDType, b: DDType): DDType
    fun add(a: DDType, b: Number): DDType
    fun subtract(a: DDType, b: DDType): DDType
    fun subtract(a: DDType, b: Number): DDType
    fun multiply(a: DDType, b: DDType): DDType
    fun multiply(a: DDType, b: Number): DDType
    fun divide(a: DDType, b: DDType): DDType
    fun divide(a: DDType, b: Number): DDType
    fun inv(a: DDType): DDType
    fun negate(value: DDType): DDType
    fun abs(value: DDType): DDType

    fun exp(value: DDType): DDType
    fun ln(value: DDType): DDType
    fun log(value: DDType, base: DDType): DDType
    fun log(value: DDType, base: Number): DDType
    fun pow(value: DDType, exponent: DDType): DDType
    fun pow(value: DDType, exponent: Number): DDType
    fun root(value: DDType, degree: DDType): DDType
    fun root(value: DDType, degree: Number): DDType
    fun sqr(value: DDType): DDType
    fun sqrt(value: DDType): DDType

    // -------- Trigonometric functions (fix: nonsense for Int) ------------
    fun sin(value: DDType): DDType
    fun asin(value: DDType): DDType
    fun cos(value: DDType): DDType
    fun acos(value: DDType): DDType
    fun tan(value: DDType): DDType
    fun atan(value: DDType): DDType
}
