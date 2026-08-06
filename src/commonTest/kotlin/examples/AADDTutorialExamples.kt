@file:Suppress("unused", "UnusedVariable")

package examples

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.BoolMath.and
import io.github.tukcps.aadd.DDBuilder.BoolMath.or
import io.github.tukcps.aadd.DDBuilder.RealMath.constrainTo
import io.github.tukcps.aadd.DDBuilder.RealMath.exp
import io.github.tukcps.aadd.DDBuilder.RealMath.minus
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.times
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.values.real.ia.RealRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AADDTutorialExamples {

    @Test
    fun instantiation() {
        DDBuilder{
            val r = RealRange(1.0, 2.0)
            val scalar   = real(1.0)
            val range    = real(2.0..3.0, "r")
            val real     = Reals.All
            val empty    = Reals.Empty
            println("scalar   = $scalar")
            println("interval = $range")
            println("real     = $real")
            println("empty    = $empty")
        }
    }

    @Test
    fun computation() {
        DDBuilder {
            val a = real(1.0..2.0, "a")
            val b = real(1.0..2.0, "b")
            val diff = a - a
            assertTrue( (a-a).isZero() )
            println("    a-a = " + (a-a))
            println("but a-b = " + (a-b))
        }
    }

    @Test
    fun expression() {
        // Volume of ellipsoid = 4/3 pi a b c
        DDBuilder {
            val a = real(1.0..10.0, "a")
            val b = real(1.0..10.0, "b")
            val c = real(1.0..10.0, "c")
            val pi = real(3.141..3.142, "pi")
            val vol = real(4.0 / 3.0) *pi*a*b*c
            // println("Volume = ${vol.getRange()}")
            // config.toStringVerbose = true
            // println("Volume = $vol")
        }
    }

    @Test
    fun piControl() {
        DDBuilder {
            println("\n=== PI controller example with AADD ===")
            val setVal: Real = real(0.4 .. 0.6, "set")
            var isVal: Real  = real(0.9 .. 1.0, "is")
            var piOut: Real  = real(0.5 .. 0.51, "out")
            //val graph = AADDStream("isVal")
            var inVal: Real
            for (t in 1..50) {
                inVal = setVal - isVal  // subtractor block
                piOut += inVal * 0.05   // PI Controller
                isVal = isVal * 0.5 + piOut * 0.5
                // Device??? 1???
                println(" At t=$t, isval: ${isVal.getRange()}, " +
                        "setval: ${setVal.getRange()}, pi: ${piOut.getRange()}")
                //graph.add(isval, t.toDouble())
            }
            //display(isval, "isval")
            //graph.display()
        }
    }

    /** Instantiation of some BDD  */
    @Test
    fun bddInstantiation() {
        DDBuilder{
            val f = Bool.False // Singleton leaf with value false
            val t = Bool.True
            val x = boolean("x")  // Constant with value true or false
            println("f = $f")
            println("t = $t")
            println("x = $x, internally as ${x.toIteString()}")
            val d = (f and x) or t
            val e = t and x
            assertSame(Bool.True, d)
            assertEquals(e, x)
        }
    }

    fun comparison() {
        DDBuilder {
            val a = real(1.0..3.0, "a")
            val b = real(2.0..4.0, "b")
            val c = a greaterThan b
            println("c = $c")
        }
    }

    @Test
    fun comparison2a() {
        DDBuilder{
            val a = real(1.0 .. 3.0, "a") // a
            val b = real(2.0 .. 4.0, "a")  // shares dependency, here complete
            val c = a greaterThan b
            println("c = $c, because a equals b and cannot be greater than b")
        }
    }

    @Test
    fun comparison3() {
        DDBuilder{
            val a = real(1.0 .. 3.0, "a")
            val b = real(2.0 .. 4.0, "b")
            val c = (a * b) greaterThan (a + b)
            println("c = $c, or internally: ${c.toIteString()}\"")
        }
    }

    fun jsonExample() {
        DDBuilder{
            val a = real(1.0..2.0, "a")
            // TODO
            // val s = a.toDTO.toJson()
            // println("s = $s")
        }
    }

    @Test
    fun exampleArithmetic() {
        DDBuilder {
            val a = real(1.0..3.0, "a")   // [1, 3]; uses noise symbol w/ index 1
            val b = real(0.0..2.0, "b")   // [0, 2]; uses noise symbol w/ index 1
            val x = a*b
            val y = a-b
            println("a * b = $x, and a - b = $y")
        }
    }

    @Test
    fun exampleControlFlow() {
        DDBuilder {
            var x: Real = real(1.0..3.0, "x")   // [1, 3]; uses noise symbol w/ index 1
            val y: Real = real(1.0..2.0, "y")   // [0, 2]; uses noise symbol w/ index 1

            IF( (x * y) greaterThan exp(x) )
                x = assign(x, x - real(1.5))
            END()

            val z: Real = x constrainTo RealRange(-0.2 .. 2.0)

            println("x = $x")
            println("z = $z or ${z.toIteString()}")
        }
    }
}

fun main() {
    val tutorial = AADDTutorialExamples()
    tutorial.bddInstantiation()
    tutorial.exampleArithmetic()
    tutorial.exampleControlFlow()
    tutorial.piControl()
}