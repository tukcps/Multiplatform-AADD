package com.github.tukcps.aadd.functions

import com.github.tukcps.aadd.IDD
import com.github.tukcps.aadd.values.IntegerRange


/** Intersection of a leaf and an Interval returns a leaf */
fun constrainTo(idd: IDD.Leaf, range: ClosedRange<Long>) =
    idd.builder.leaf(idd.value intersect IntegerRange(range.start, range.endInclusive))

fun intersect(a: IDD.Leaf, b: IDD.Leaf): IDD = a.builder.leaf(a.value intersect b.value)
fun plus(a: IDD.Leaf, b: IDD.Leaf): IDD = a.builder.leaf(a.value + b.value)
fun minus(a: IDD.Leaf, b: IDD.Leaf): IDD = a.builder.leaf(a.value - b.value)
fun times(a: IDD.Leaf, b: IDD.Leaf): IDD = a.builder.leaf(a.value * b.value)
fun div(a: IDD.Leaf, b: IDD.Leaf): IDD = a.builder.leaf(a.value / b.value)
fun contains(a: IDD.Leaf, value: Long) = a.value.contains(value)
fun pow(base: IDD.Leaf, exp: IDD.Leaf): IDD = base.pow(exp.value)
fun sqrt(input : IDD.Leaf, n: IDD.Leaf) : IDD = input.root(n.value)


/**  Extension Functions developed by Jack **/
fun sqr(input : IDD) : IDD = input.sqr()

fun sqrt(input : IDD) : IDD = input.sqrt()
fun sqrt(input : IDD, n: IDD) : IDD = input.root(n)

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

// eof