package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.dd.BDD.Leaf
import io.github.tukcps.aadd.values.bool.XBool
import kotlin.jvm.JvmName

/**
 * Applies a binary operator passed as parameter on the BDD
 * passed as first two parameters and returns result.
 * @receiver first parameter
 * @param op the operation
 * @param g second parameter
 * @return result of binary operation on the parameters
 */
internal fun BDD.apply(g: BDD, op: (XBool, XBool) -> XBool): BDD =
    applyGeneric(g, op)

@JvmName("applyGenericLeaf")
internal fun BDD.apply(g: BDD, op: (Leaf, Leaf) -> Leaf): BDD =
    applyGeneric(g, op)