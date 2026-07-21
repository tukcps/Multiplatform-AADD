package aaddtests

import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.values.integer.IntegerRange
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class IDDTests {

    @Test
    fun integerRangeTests() {
        val a = IntegerRange(1, 1)
        val b = IntegerRange(2, 2)
        var c: IntegerRange = a + b
        val d = IntegerRange(3, 3)
        assertEquals(c, d)
        c = d - a
        assertEquals(b, c)
        val e: IntegerRange = b * d
        val f = IntegerRange(6, 6)
        assertEquals(e, f)
        val g = IntegerRange(-6, -6)
        val h: IntegerRange = f.unaryMinus()
        assertEquals(h, g)
        val i: IntegerRange = -f
        assertEquals(i, g)
        val j = IntegerRange(2, 3)
        val k = IntegerRange(6, 7)
        val l: IntegerRange = k / j
        assertEquals(l, IntegerRange(2, 3))
    }

    @Test
            /** ITE of an IDD leaf with a BDD of height 1 must result in merged IDD with height 1.
             * However, getting height 0
             */
    @Ignore
    fun testITE1() {
        DDBuilder {
            var a : IDD = integer(1L..1)
            val b : IDD = integer(0L..0)
            val c : BDD = a.greaterThanOrEquals(b)
            assertEquals(0, c.height())
            a = c.ite(a, b)
            assertEquals(0, a.height())
        }
    }

    @Test
    @Ignore
    fun testITE2() {
        DDBuilder {
            var a : IDD = integer(-1L..1)
            val b : IDD = integer(0L..0)
            val c : BDD = a.greaterThanOrEquals(b)
            assertEquals(1, c.height())
            a = c.ite(a, b)
            assertEquals(1, a.height())
        }
    }

    @Test
    @Ignore
    fun reduceTest() {
        val builder = DDBuilder()
        // Two BDD with different indexes.
        with(builder) {
            val ai = integer(-1L..3)
            val tr = integer(1)
            val tr2 = integer(2)
            val c1 = ai.greaterThanOrEquals(tr)
            val c2 = ai.lessThanOrEquals(tr2)
            val a = integer(1L..2)
            val b = integer(2L..3)
            val d = c1.ite(a, b)
            val e = c2.ite(a, b)
            assertEquals(c2.index, e.index)
            assertEquals(e.height(), c2.height())
            val f = e.plus(d)
            assertEquals(2, f.height())
        }
    }

    @Test
    fun testLogicalComparisonOperators() {
        DDBuilder {
            val a = this.integer(1L..1)
            val b = this.integer(2L..2)
            val c = a.lessThan(b)
            val d = b.greaterThan(a)
            assertEquals(True, c)
            assertEquals(True, d)
        }
    }


    @Test
    @Ignore
    fun testIDDAssignment() {
        DDBuilder {
            val a = integer(10)
            assertEquals(10, a.getRange().min)
            assertEquals(10, a.getRange().max)
        }
    }

    @Test
    fun testIDDAddition() {
        DDBuilder {
            val a = integer(10)
            val b = integer(1)
            val r = a + b
            assertEquals(11, r.getRange().min)
            assertEquals(11, r.getRange().max)
        }
    }

    @Test
    fun testIDDAddition2() {
        DDBuilder {
            val a = integer(1L..2)
            val b = integer(2L..3)
            val c  = a + b
            assertEquals(3, c.min)
            assertEquals(5, c.max)
        }
    }

    @Test
    fun testIDDSubtraction() {
        DDBuilder {
            val a = integer(10)
            val b = integer(1)
            val r = a - b
            assertEquals(9, r.min)
            assertEquals(9, r.max)
        }
    }

    @Test
    fun testIDDMultiplication() {
        DDBuilder {
            val a = integer(10)
            val b = integer(2)
            val r = a * b
            assertEquals(20, r.min)
            assertEquals(20, r.max)
        }
    }

    @Test
    fun testIDDDivision() {
        DDBuilder {
            val a = integer(10)
            val b = integer(5)
            val r = a / b
            assertEquals(2, r.min)
            assertEquals(2, r.max)
        }
    }

    @Test
    fun testContains() {
        DDBuilder {
            val a = this.integer(0L..2)
            assertTrue(a.contains(1))
            assertTrue(a.contains(0L))
            assertTrue(a.contains(2L))
        }
    }

    @Test
    @Ignore
    // Check if infeasible paths are detected and leaves are marked.
    fun infeasibilityTest() {
        DDBuilder{
            val a = integer(0L..2)
            val b = integer(3L .. 4)
            val c1 = a greaterThan integer(1)
            //println("c1 = " + c1.toIteString())
            assertEquals(True, (c1 as BDD.Internal).T)
            assertEquals(False, (c1).F)
            // assertEquals("ITE(1, True, False)", c1.toIteString())
            val c2 = a lessThan integer(1)
            //println("c2 = " + c2.toIteString())
            assertEquals(False,(c2 as BDD.Internal).T)
            assertEquals(True,c2.F)
            //assertEquals("ITE(2, False, True)", c2.toIteString())
            val d = c1.ite(a, b)
            val e = c2.ite(a, b)
            val f = d.plus(e)
            f.getRange()
            assertEquals(0, f.numInfeasible())
        }
    }

    @Test
    @Ignore
    fun testMultiplication2() {
        DDBuilder {
            val a = integer(7L..10)
            val b = this.integer(2L..3) as IDD.Leaf
            val c = a.times(b) as IDD.Leaf
            assertEquals(14, c.min)
            assertEquals(30, c.max)
        }
    }

    @Test
    fun checkApply() {
        DDBuilder {
            // Two BDD with different index.
            val ai = this.integer(3L..4)
            val tr = this.integer(1L..1)
            val tr2 = this.integer(2L..2)
            val c1 = ai.greaterThanOrEquals(tr)
            val c2 = ai.lessThanOrEquals(tr2)
            assertEquals(c1.index, c2.index)
            val a = integer(1)
            val b = integer(2)
            val d = c1.ite(a, b)
            assertEquals(c1.index, d.index)
            assertTrue(c1.height() != 1)
            val e = c2.ite(a, b)
            assertEquals(c2.index, e.index)
            assertEquals(e.height(), c2.height())
            val f = e.plus(d)
            assertTrue(f.height() != 2)
        }
    }
}