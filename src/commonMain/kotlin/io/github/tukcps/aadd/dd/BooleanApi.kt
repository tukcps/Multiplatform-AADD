package io.github.tukcps.aadd.values

import io.github.tukcps.aadd.DDApi

/**
 * Operators for Boolean Values
 */
interface BoolApi<DDType>: DDApi<DDType> {
    fun not(a: DDType): DDType
    fun and(a: DDType, b: DDType): DDType
    fun nand(a: DDType): DDType
    fun or(a: DDType, b: DDType): DDType
    fun nor(a: DDType): DDType
    fun xor(a: DDType, b: DDType): DDType
    fun nxor(a: DDType, b: DDType): DDType
}