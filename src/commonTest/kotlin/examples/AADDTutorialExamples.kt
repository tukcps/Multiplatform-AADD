@file:Suppress("unused")

package examples

import com.github.tukcps.aadd.*
import com.github.tukcps.aadd.values.Range
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AADDTutorialExamples {

    @Test
    fun instantiation() {
        DDBuilder{
            val r = Range(1.0, 2.0)
            val scalar   = real(1.0)
            val range    = real(2.0..3.0, "r")
            val real     = Reals
            val empty    = Empty
            println("scalar   = $scalar")
            println("interval = $range")
            println("real     = $real")
            println("empty    = $empty")
        }
    }

    @Test
    fun computation() {
        DDBuilder{
            val a = real(1.0..2.0, "a")
            val b = real(1.0..2.0, "b")
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
            println("Volume = ${vol.getRange()}")
            config.toStringVerbose = true
            println("Volume = $vol")
        }
    }

    fun piControl() {
        DDBuilder {
            println("\n=== PI controller example with AADD ===")
            val setval      = real(0.4 .. 0.6, "set")
            var isVal: AADD = real(0.9 .. 1.0, "is")
            var piOut: AADD = real(0.5 .. 0.51, "out")
            //val graph = AADDStream("isval")
            var inval: AADD
            for (t in 1..50) {
                inval = setval - isVal  // subtractor block
                piOut += inval * 0.05   // PI Controller
                isVal = isVal * 0.5 + piOut * 0.5
                // Device??? 1???
                println(" At t=$t, isval: ${isVal.getRange()}, " +
                        "setval: ${setval.getRange()}, pi: ${piOut.getRange()}")
                //graph.add(isval, t.toDouble())
            }
            //display(isval, "isval")
            //graph.display()
        }
    }

    /** Instantiation of some BDD  */
    fun bddInstantiation() {
        DDBuilder{
            val a = constant(true) // gets BDD with Boolean value true or false
            val f = False // Constant leaf with value false
            val t = True
            val x = variable("X") // Constant with value true or false
            println("a=$a")
            println("f=$f")
            println("t=$t")
            println("X=$x")
            val d = (f and x) or t
            val e = t and x
            assertSame(True, d)
            assertEquals(e, x)
        }
    }

    fun comparison() {
        DDBuilder{
            val a = real(1.0..3.0, "a")
            val b = real(2.0..4.0, "b")
            val c = a greaterThan b
            println("c = $c")
        }
    }

    fun comparison2() {
        DDBuilder{
            val a = real(1.0 .. 3.0, "a")
            val b = real(2.0 .. 4.0, "a")
            val c = a greaterThan b
            println("c=$c")
        }
    }

    fun comparison3() {
        DDBuilder{
            val a = real(1.0 .. 3.0, "a")
            val b = real(2.0 .. 4.0, "b")
            val c = (a * b) greaterThan (a + b)
            println("c=$c")
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

    fun cavExampleArithmetic() {
        DDBuilder {
            val a = real(1.0..3.0, "a")    // [1, 3]; uses noise symbol w/ index 1
            val b = real(0.0..2.0, "b")   // [0, 2]; uses noise symbol w/ index 1
            val x = a*b
            val y = a-b
            println("a*b=$x and a-b=$y")
        }
    }

    fun cavExampleControlFlow() {
        DDBuilder {
            var x: AADD = real(1.0..3.0, "x")   // [1, 3]; uses noise symbol w/ index 1
            val y: AADD = real(1.0..2.0, "y")   // [0, 2]; uses noise symbol w/ index 1

            IF((x * y) greaterThan x.exp())
            x = assign(x, x - real(1.5))
            END()
            println("x=$x" + " intersect -0.2..2=${x.constrainTo(Range(-0.2 .. 2.0))}")
            //display(x, "AADD x")
        }
    }
}

fun main() {
    val tutorial = AADDTutorialExamples()
    tutorial.bddInstantiation()
    tutorial.cavExampleArithmetic()
    tutorial.cavExampleControlFlow()
    tutorial.piControl()
}