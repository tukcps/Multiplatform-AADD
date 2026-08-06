package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.dd.BDD.Leaf
import io.github.tukcps.aadd.values.bool.XBool

interface BDDMath {

    operator fun BDD.not() = this.apply(Leaf::not)

    /** Binary Operations BDD x BDD -> BDD */
    infix fun BDD.and(other: BDD): BDD = this.apply(other, Leaf::and)
    infix fun BDD.and(other: XBool): BDD = this.apply(other.bddLeafOf(this.builder), Leaf::and)

    infix fun BDD.or(other: BDD): BDD = this.apply(other, Leaf::or)
    infix fun BDD.or(other: XBool): BDD = this.apply(other.bddLeafOf(this.builder), Leaf::or)

    infix fun BDD.xor(other: BDD): BDD = this.apply(other, Leaf::xor)
    infix fun BDD.xor(other: XBool): BDD = this.apply(other.bddLeafOf(this.builder), Leaf::xor)

    infix fun BDD.nand(other: BDD): BDD = this.apply(other, Leaf::nand)
    infix fun BDD.nand(other: XBool): BDD = this.apply(other.bddLeafOf(this.builder), Leaf::nand)

}