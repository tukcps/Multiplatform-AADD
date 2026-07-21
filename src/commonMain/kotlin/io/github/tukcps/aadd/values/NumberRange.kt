package io.github.tukcps.aadd.values

enum class BoundKind {
    FINITE,
    NEGATIVE_INFINITY,
    POSITIVE_INFINITY;

    fun isValid(value: Long): Boolean = when (this) {
        FINITE -> true
        NEGATIVE_INFINITY -> value == Long.MIN_VALUE
        POSITIVE_INFINITY -> value == Long.MAX_VALUE
    }
}

val BoundKind.isFinite: Boolean
    get() = this == BoundKind.FINITE

val BoundKind.isInfinite: Boolean
    get() = this == BoundKind.NEGATIVE_INFINITY ||
            this == BoundKind.POSITIVE_INFINITY

val BoundKind.sign: Int
    get() = when (this) {
        BoundKind.NEGATIVE_INFINITY -> -1
        BoundKind.FINITE -> 0
        BoundKind.POSITIVE_INFINITY -> 1
    }

/**
 * NumberRange is the common interface for different kind of range arithmetics
 * on different kind of value types: Long, Double, using IA and AA.
 * The implementations shall handle
 * - overflows
 * - rounding errors such that a safe inclusion is guaranteed
 * - exceptions, e.g., division by zero.
 *
 * Invariants:
 * - The implementation has to provide immutable objects.
 * - min <= max      -> non-empty interval
 * - min > max       -> empty interval; represents also NaN which is never stored in min or max.
 */
interface NumberRange <T: Comparable<T> >: ClosedRange<T> {
    /** The interface property of ClosedRange is mapped to min/max */
    override val start: T get() = min
    override val endInclusive: T get() = max
    val min: T
    val max: T

    override fun isEmpty(): Boolean = min > max
    val maxKind: BoundKind
    val minKind: BoundKind

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
    fun exp(): NumberRange<T>
    fun log(): NumberRange<T>
    fun log(other: NumberRange<T>): NumberRange<T>
}