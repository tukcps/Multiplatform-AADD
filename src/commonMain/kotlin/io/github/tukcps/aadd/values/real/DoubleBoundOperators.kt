package io.github.tukcps.aadd.values.real

import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble

/**
 * For use with CARE!
 * No dedicated rounding, just convenience, might be dropped if too error-prone.
 */
operator fun DoubleBound?.plus(other: DoubleBound?): DoubleBound? = this?.let { (this.toDouble() + other.toDouble()).toDoubleBound()}
operator fun DoubleBound?.plus(other: Double): DoubleBound? = this?.let { (this.toDouble() + other).toDoubleBound() }
operator fun Double.plus(other: DoubleBound?): DoubleBound? = (this + other.toDouble()).toDoubleBound()

operator fun DoubleBound?.minus(other: DoubleBound?): DoubleBound? = (this.toDouble() + other.toDouble()).toDoubleBound()
operator fun DoubleBound?.minus(other: Double): DoubleBound? = (this.toDouble() - other).toDoubleBound()
operator fun Double.minus(other: DoubleBound?): DoubleBound? = (this + other.toDouble()).toDoubleBound()

operator fun DoubleBound?.times(other: DoubleBound?): DoubleBound? = (this.toDouble() * other.toDouble()).toDoubleBound()
operator fun DoubleBound?.times(other: Double): DoubleBound? = (this.toDouble() * other).toDoubleBound()
operator fun Double.times(other: DoubleBound?): DoubleBound? = (this * other.toDouble()).toDoubleBound()

operator fun DoubleBound?.div(other: DoubleBound?): DoubleBound? = (this.toDouble() / other.toDouble()).toDoubleBound()
operator fun DoubleBound?.div(other: Double): DoubleBound? = (this.toDouble() / other).toDoubleBound()
operator fun Double.div(other: DoubleBound?): DoubleBound? = (this / other.toDouble()).toDoubleBound()

operator fun DoubleBound?.rem(other: DoubleBound): DoubleBound? = (this.toDouble() % other.toDouble()).toDoubleBound()
operator fun DoubleBound?.rem(other: Double): DoubleBound? = (this.toDouble() % other).toDoubleBound()
operator fun DoubleBound?.unaryMinus(): DoubleBound? = (-this.toDouble()).toDoubleBound()
