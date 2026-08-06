package io.github.tukcps.aadd

import io.github.tukcps.aadd.values.bool.XBool

/**
 * ## DDApi
 * Common interface for all domains in the AADD library.
 * It is leaning towards a bounded lattice and provides a basic, shared
 * infrastructure for all domains of DD:
 * Booleans (BDD), Reals (AADD), Integers (IDD), and maybe more.
 */
interface DDApi<DDType> {

    fun DDType.isEmpty(): Boolean
    fun DDType.isZero():  Boolean
    fun DDType.isOne():   Boolean
    fun DDType.isAll():   Boolean

    // ------------ Set operations -----------
    infix fun DDType.join(other: DDType): DDType
    infix fun DDType.intersect(other: DDType): DDType
    infix fun DDType.contains(other: DDType): Boolean

    // ------------ Comparison ---------------
    infix fun DDType.greaterThan(other: DDType): XBool
    infix fun DDType.greaterThanOrEquals(other: DDType): XBool
    infix fun DDType.lessThan(other: DDType): XBool
    infix fun DDType.lessThanOrEquals(other: DDType): XBool
    infix fun DDType.equals(other: Any?): XBool

    // ------------- Min, Max ----------------
    fun min(vararg values: DDType): DDType
    fun max(vararg values: DDType): DDType
}
