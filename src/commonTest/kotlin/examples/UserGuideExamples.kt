package examples

import com.github.tukcps.aadd.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The examples from the jAADD Users Guide
 */
class HelloBDD {

    @Test
    fun main() {

        val builder = DDBuilder()
        with(builder) {
            val c: BDD = variable("c")
            val b: BDD = variable("b")
            val a: BDD = variable("a")
            val f = (a and b) or c
            println("f = $f")
            assertEquals(3, f.height())
        }
    }
}

class HelloAADD {

    @Test
    fun main() {
        DDBuilder {
            val c = variable("c")
            val x = real(1.1..2.0, "x")
            val y = real(1.1..3.0, "y")
            val f = (x greaterThanOrEquals real(1.5)) and (x lessThanOrEquals y * x) or c
            println(" f = $f")
            // toStringVerbose = true
            println(conds.toString())
            assertEquals(3, f.height())
        }
    }
}

class HelloAADD2 {
    @Test
    fun main() {
        DDBuilder {
            val x = real(-1.0..1.0, "x")
            val f = (x greaterThanOrEquals real(0.0)).ite(x - real(100.0), x + real(100.0))
            f.getRange()
            println(" x = $x")
            println(" f = $f")
            // toStringVerbose = true
            println(conds.toString())
            assertEquals(1, f.height())
        }
    }
}
