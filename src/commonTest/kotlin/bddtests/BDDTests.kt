@file:Suppress("unused", "UnusedVariable")

package bddtests

import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.functions.numInternalNodes
import com.github.tukcps.aadd.values.XBool
import kotlin.test.*


internal class BDDTests {

    @Test
    // there are constant leaves with value true, false, nab ...
    fun bddConstantsTest() {
        DDBuilder {
            val tru = True
            val fal = False
            val infB = InfeasibleB

            assertEquals(XBool.False, fal.value)
            assertEquals(XBool.True, tru.value)
            assertTrue(fal.isFeasible)
            assertTrue(tru.isFeasible)
            assertTrue(infB.isInfeasible)
        }
    }

    @Test
    // The leaves are unique instances and represented by references to
    // exactly only one instance
    fun cloneLeavesTest() {
        DDBuilder {
            // ONE should create a shallow copy as a clone, not a new object.
            val a = True
            val b = False
            val ac = a.clone()
            val bc = b.clone()
            assertSame(a, ac)
            assertSame(b, bc)
        }
    }

    // @Test
    // the index of an internal node refers to a condition.
    // there shall be an assertion error if we access an index
    // that is not referring to a condition
    /*
    fun testBDDIndexException() {
        val fab = AADDContext()
        assertThrows(AADDError::class.java) {
            val a = BDD(fab, fab.True, fab.False, 100)
            val b = a.not()
        }
    } */

    @Test
    fun complementAADDCondTest() {
        DDBuilder {
            val cond = real(1.0..2.0) greaterThanOrEquals real(1.5)
            val a = cond.ite(True, False) // ITE(1,True, False)
            val b = cond.ite(False, True) // ITE(1, False, True)
            val c = a.not() // ITE(1, False, True), shall just flip values at leaves
            assertEquals(b, c)
            assertNotEquals(a, b)
        }
    }


    @Test
    fun complementBoolCondTest() {
        DDBuilder {
            val cond = boolean("a")
            val a = cond.ite(True, False) // ITE(1,True, False)
            val b = cond.ite(False, True) // ITE(1, False, True)
            val c = a.not() // ITE(1, False, True), shall just flip values at leaves
            assertEquals(b, c)
            assertNotEquals(a, b)
        }
    }

    @Test @Ignore
    fun nodesOrderTest() {
        DDBuilder {
            val res= boolean("res")
            val a=real(1.0 .. 4.0)
            val b=real(0.5)
            val c=(a*b greaterThan real(1.0))
            val ite = c.ite(True, res)
            // println(res.toIteString() )
        }
    }



    @Test
    // BDD shall merge similar leaves
    fun and() {
        DDBuilder {
            val c = real(1.0..2.0) greaterThanOrEquals real(1.5)
            var a = c.ite(True, False)
            var b = c.ite(False, True)
            val expected = False // via reduction of BDD.
            var r = a.and(b)
            assertEquals(expected, r)
            a = c.ite(True, False)
            b = c.ite(True, False)
            r = a.and(b)
            val expected2 = c.ite(True, False)
            assertEquals(expected2, r)
        }
    }

    @Test
    fun bddVars() {
        DDBuilder {
            val tru = True
            val fal = False
            val c = boolean("c")
            val d = fal and c and tru
            val e = fal.or(boolean("e"))
            assertEquals(False, d)
            assertEquals(False, fal)
            assertEquals(True, tru)
            assertEquals(1, e.height())
        }
    }

    @Test
    fun ite() {
        DDBuilder {
            val cond = real(1.0..2.0) greaterThanOrEquals real(1.5)
            val b = cond.ite(False, True)
            var r = b.ite(True, b)
            assertEquals(r, b)
            r = b.ite(False, b)
            assertEquals(r, False)
        }
    }

    @Test
    // The apply function merges two BDD with different index
    // and applies the lambda parameter to the leave values
    fun checkApply() {
        DDBuilder {
            val ai = real(-1.0 .. 1.0, "1")
            val tr = real(0.1)
            val tr2 = real(0.2)
            val tr3 = real(0.3)
            var c1 = ai greaterThanOrEquals tr
            assertEquals(1, c1.height())
            var c2 = ai.lessThanOrEquals(tr2)
            assertEquals(1, c2.height())
            // println("c1=$c1, c2=$c2")
            c2 = c1.and(c2)
            assertEquals(2, c2.height())
            c2 = c2.or(c2)
            assertEquals(2, c2.height())
            c1 = ai.greaterThan(tr3)
            c1 = c1.xor(c2)
            assertEquals(3, c1.height())
        }
    }

    @Test
    //Tests BDD helper functions
    fun checkHelpers()  {
        DDBuilder {
            val a = True
            var freeNodes = a.numInternalNodes()
            assertEquals(0, freeNodes)

            val b = False or a
            freeNodes = b.numInternalNodes()
            assertEquals(0, freeNodes)

            val c = boolean("c")
            freeNodes = c.numInternalNodes()
            assertEquals(1, freeNodes)

            val d = c or boolean("d")
            freeNodes = d.numInternalNodes()
            assertEquals(2, freeNodes)

            assertEquals(true, a.satisfiable())
            assertEquals(false, a.not().satisfiable())
            assertEquals(true, c.satisfiable())
        }
    }

    @Test
    fun toStringTest() {
        DDBuilder {
            val r1 = real(0.0..2.0)
            val r2 = real(-1.0..2.0)
            val b1 = r1 greaterThanOrEquals 1.0
            val b2 = b1.ite(False, True)
            val b3 = (r2 greaterThanOrEquals 1.0).ite(b2, False)
            assertEquals("Unknown", b3.toString() )
        }
    }


    @Test
    fun toStringTest2() {
        DDBuilder {
            val r1 = True
            val r2 = False
            assertEquals("True", r1.toString())
            assertEquals("False", r2.toString())
        }
    }

    @Test
    fun pathConditionTest() {
        DDBuilder {
            val a = False
            val b = True
            var c: BDD = True
            IF(b)
                IF(a)
                    c = assign(c, False)
                END()
            END()
            assertEquals(True, c)
        }
    }

    @Test
    fun testITEReal(){
        DDBuilder {
            val l = real(0.0..50.0)
            val r = real(30.0..30.0)
            val c = (l lessThanOrEquals r).ite(l, r)
            c.getRange()
            assertEquals(0.0, c.min, 0.00001)
            assertEquals(30.0, c.max, 0.000001)
        }
    }

    @Test //Does not work on Integers
    @Ignore
    fun testITEInt(){
        DDBuilder {
            val l = integer(0L..50)
            val r = integer(30L..30)
            val c= (l lessThanOrEquals r).ite(l, r)
            assertEquals(0,(l lessThanOrEquals r).ite(l, r).min)
            assertEquals(30,(l lessThanOrEquals r).ite(l, r).max)
        }
    }
}
