@file:Suppress("unused", "UnusedVariable")

package dd.bddtests

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.BoolMath.and
import io.github.tukcps.aadd.DDBuilder.BoolMath.nand
import io.github.tukcps.aadd.DDBuilder.BoolMath.not
import io.github.tukcps.aadd.DDBuilder.BoolMath.or
import io.github.tukcps.aadd.DDBuilder.BoolMath.xor
import io.github.tukcps.aadd.DDBuilder.RealMath.multiply
import io.github.tukcps.aadd.dd.BDD
import io.github.tukcps.aadd.dd.numInternalNodes
import io.github.tukcps.aadd.values.bool.XBool
import kotlin.test.*


class BDDTests {

    @Test
    // there are constant leaves with value true, false, nab ...
    fun bddConstantsTest() {
        DDBuilder {
            val tru = Bool.True
            val fal = Bool.False
            val infB = Bool.Infeasible

            assertEquals(XBool.False, fal.value)
            assertEquals(XBool.True, tru.value)
            assertTrue(fal.isFeasible())
            assertTrue(tru.isFeasible())
            assertTrue(infB.isInfeasible())
        }
    }

    @Test
    // The leaves are unique instances and represented by references to
    // exactly only one instance
    fun cloneLeavesTest() {
        DDBuilder {
            // ONE should create a shallow copy as a clone, not a new object.
            val a = Bool.True
            val b = Bool.False
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
            val a = cond.ite(Bool.True, Bool.False) // ITE(1,True, False)
            val b = cond.ite(Bool.False, Bool.True) // ITE(1, False, True)
            val c = a.not() // ITE (1, Boolean.False, Boolean.True) // , shall just flip values at leaves
            assertEquals(b, c)
            assertNotEquals(a, b)
        }
    }


    @Test
    fun complementBoolCondTest() {
        DDBuilder {
            val cond = boolean("a")
            val a = cond.ite(Bool.True, Bool.False) // ITE(1,True, False)
            val b = cond.ite(Bool.False, Bool.True) // ITE(1, False, True)
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
            val c=( multiply( a, b ) greaterThan real(1.0))
            val ite = c.ite(Bool.True, res)
            // println(res.toIteString() )
        }
    }



    @Test
    // BDD shall merge similar leaves
    fun mergeTest() {
        DDBuilder {
            val c = real(1.0..2.0) greaterThanOrEquals real(1.5)
            var a = c.ite(Bool.True, Bool.False)
            var b = c.ite(Bool.False, Bool.True)
            val expected = Bool.False // via reduction of BDD.
            var r = a and b
            assertEquals(expected, r)
            a = c.ite(Bool.True, Bool.False)
            b = c.ite(Bool.True, Bool.False)
            r = a and b
            val expected2 = c.ite(Bool.True, Bool.False)
            assertEquals(expected2, r)
        }
    }

    @Test
    fun bddVars() {
        DDBuilder {
            val tru = Bool.True
            val fal = Bool.False
            val c = boolean("c")
            val d = fal and c and tru
            val e = fal or boolean("e")
            val f = fal nand c
            assertEquals(Bool.False, d)
            assertEquals(Bool.False, fal)
            assertEquals(Bool.True, tru)
            assertEquals(1, e.height())
        }
    }

    @Test
    fun ite() {
        DDBuilder {
            val cond = real(1.0..2.0) greaterThanOrEquals real(1.5)
            val b = cond.ite(Bool.False, Bool.True)
            var r = b.ite(Bool.True, b)
            assertEquals(r, b)
            r = b.ite(Bool.False, b)
            assertEquals(r, Bool.False)
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
            c2 = c1 and c2
            assertEquals(2, c2.height())
            c2 = c2 or c2
            assertEquals(2, c2.height())
            c1 = ai.greaterThan(tr3)
            c1 = c1 xor c2
            assertEquals(3, c1.height())
        }
    }

    @Test
    //Tests BDD helper functions
    fun checkHelpers()  {
        DDBuilder {
            val a = Bool.True
            var freeNodes = a.numInternalNodes()
            assertEquals(0, freeNodes)

            val b = Bool.False or a
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
            val b2 = b1.ite(Bool.False, Bool.True)
            val b3 = (r2 greaterThanOrEquals 1.0).ite(b2, Bool.False)
            assertEquals("Unknown", b3.toString() )
        }
    }


    @Test
    fun toStringTest2() {
        DDBuilder {
            val r1 = Bool.True
            val r2 = Bool.False
            assertEquals("True", r1.toString())
            assertEquals("False", r2.toString())
        }
    }

    @Test
    fun pathConditionTest() {
        DDBuilder {
            val a = Bool.False
            val b = Bool.True
            var c: BDD = Bool.True
            IF(b)
                IF(a)
                    c = assign(c, Bool.False)
                END()
            END()
            assertEquals(Bool.True, c)
        }
    }

    @Test
    fun testITEReal(){
        DDBuilder {
            val l = real(0.0..50.0)
            val r = real(30.0..30.0)
            val c1 = (l lessThanOrEquals r)
            val c = c1.ite(l, r)            // 0..30 (T) or 30 (F)
            c.getRange()
            assertEquals(0.0, c.min.finiteValue, 0.00001)
            assertEquals(30.0, c.max.finiteValue, 0.000001)
        }
    }

    @Test //Does not work on Integers as there is not yet an ILP solver :-(
    @Ignore
    fun testITEInt(){
        DDBuilder {
            val l = integer(0L..50)
            val r = integer(30L..30)
            val c= (l lessThanOrEquals r).ite(l, r)
            assertEquals(0L,(l lessThanOrEquals r).ite(l, r).min.finiteValue)
            assertEquals(30L,(l lessThanOrEquals r).ite(l, r).max.finiteValue)
        }
    }
}
