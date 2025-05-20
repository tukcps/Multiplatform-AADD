@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedVariable", "unused")

package aaddtests


import com.github.tukcps.aadd.*
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals


class AADDTutorialKotlinAsTestWithoutRWithOriginalForms {

    @Test
    fun instantiation() {
        DDBuilder {
            config.originalFormsFlag = true
            config.noiseSymbolsFlag = true
            val scalar   = real(1.0)
            val interval = real(2.0 .. 3.0, "id")     // min, max are doubles, index is int
            val real     = Reals
            val empty    = Empty
            assertEquals(1.0, (scalar as AADD.Leaf).central)
            assertEquals((interval).value, AffineForm(this, 2.0..3.0, 1))
        }
    }

    /** TODO: No Assertions so test only checks if it compiles. Can the "test" be deleted ?*/
    @Test
    fun computation() {
        DDBuilder{
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag = true
            val a = real(1.0..2.0, "a")
            val b = real(1.0..2.0, "b")
        }
    }

    @Test
    fun expression() { // Volume of ellipsoid = 4/3 pi a b c
        DDBuilder {
            config.originalFormsFlag = true
            config.noiseSymbolsFlag = true
            val a = real(1.0 .. 10.0, "a")
            val b = real(1.0 .. 10.0, "b")
            val c = real(1.0 .. 10.0, "c")
            val pi = real(3.141 .. 3.142, "pi")
            val vol = (real(4.0/3.0) *pi*a*b*c) as AADD.Leaf

            assertEquals(10 * 10 * 10 * 3.142 * 4 / 3, vol.max, 0.0001)
            assertEquals(3.141 * 4 / 3, vol.min, 0.00001)
        }
    }

    /** TODO: No Assertions so test only checks if it compiles. Can the "test" be deleted ?*/

    @Test
    fun piControlDouble() {
        DDBuilder{
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag = true
            val setVal = 1.0
            var isVal = 1.0
            var piOut = 0.0

            for (t in 1..10) {
                piOut += (setVal - isVal) * 0.5
                isVal = piOut
            }
        }
    }

    /** Simple PI controller model  */
    @Test
    fun piControlAADD() {
        DDBuilder{
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag = true
            val setVal = real(1.0)
            var isVal: AADD = real(0.0..2.0, "Uncertainty isVal")
            var piOut: AADD = real(0.0..2.0, "Uncertainty initial state")
            for (i in 1..10) {
                piOut += (setVal - isVal) * 0.5
                isVal = piOut
            }
            assertTrue(isVal.getRange() in Range(0.99 .. 1.1))
        }
    }

    /** Instantiation of some BDD  */
    @Test
    fun bddInstantiation() {
        DDBuilder{
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag = true
            val a = constant(true) // gets BDD with Boolean value true or false
            val f = False                // Constant leaf with value false
            val t = True
            val x = boolean("X") // Boolean variable with value true or false
            val d = (f and x) or t
            val e = t and x
            assertTrue(a == True)
            assertTrue((d as BDD.Leaf) == True)
            assertTrue(e == x)
        }
    }

    @Test
    fun assumeGuaranteeExercise() {
        DDBuilder{
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag = true
            val a: BDD = False
            val b: BDD = boolean("b")
            val c: BDD = boolean("c")
            val d = a or (b and c.not() )
            assertEquals(2, d.height())
            assertEquals(3, d.numLeaves())
        }
    }

    @Test
    fun comparison() {
        DDBuilder {
            config.originalFormsFlag = true
            config.noiseSymbolsFlag = true
            val a = real(1.0 .. 3.0, "a")
            val b = real(2.0 .. 4.0, "b")
            val c = a greaterThan b
            assertEquals(1, c.height())
        }
    }

    @Test
    fun comparison2() {
        DDBuilder {
            config.originalFormsFlag = true
            config.noiseSymbolsFlag = true
            val a = real(1.0 .. 3.0, "a")
            val b = real(2.0 .. 4.0, "b")
            val c = a greaterThan b
            assertTrue(c.height() == 1)
            assertTrue(c == XBool.X)
        }
    }

    @Test
    fun comparison3() {
        DDBuilder {
            config.originalFormsFlag = true
            config.noiseSymbolsFlag = true
            val a = real(1.0 .. 3.0, 1.toString())
            val b = real(2.0 .. 4.0, 2.toString())
            val c = (a * b) greaterThan (a + b)
            assertEquals(1, c.height())
        }
    }

    /** TODO: No Assertions so test only checks if it compiles. Can the "test" be deleted ?*/

    @Test
    fun jsonExample() {
        DDBuilder{
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag = true
            val a = real(1.0 .. 2.0, 4.toString())
            // TODO: Add DTO
            // val s = a.toDTO.toJson()
        }
    }

    /** TODO: No Assertions so test only checks if it compiles. Can the "test" be deleted ?*/

    /** Example shows the AADD getRange function to call LP solver  */
    @Test fun iteExample1() {
        DDBuilder {
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag = true
            var a: AADD = real(-1.0 .. 1.0, "a")
            a = (a lessThanOrEquals real(0.0)).ite(a + real(2.0), a - real(2.0))
            a.getRange()
        }
    }

    /** TODO: No Assertions so test only checks if it compiles. Can the "test" be deleted ?*/

    @Test
    fun cavExampleArithmetic() {
        DDBuilder {
            config.originalFormsFlag = true
            config.noiseSymbolsFlag = true
            val a = real(1.0 .. 3.0, "a")    // [1, 3]; uses noise symbol w/ index 1
            val b = real(0.0 .. 2.0, "b")   // [0, 2]; uses noise symbol w/ index 1
            var x = a*b
            var y = a-b
        }
    }

    /** TODO: No Assertions so test only checks if it compiles. Can the "test" be deleted ?*/

    @Test
    fun cavExampleControlFlow() {
        DDBuilder {
            config.originalFormsFlag = true
            config.noiseSymbolsFlag = true
            var x: AADD = real(1.0 .. 3.0, "x")   // [1, 3]; uses noise symbol w/ index 1
            val y = real(1.0 .. 2.0, "y")   // [0, 2]; uses noise symbol w/ index 1

            IF ((x * y) greaterThan x.exp() )
            x = assign(x, x- real(1.5))
            END()
        }
    }

}
