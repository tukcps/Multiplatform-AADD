package examples.aaddtests

import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.IDD
import com.github.tukcps.aadd.values.IntegerRange
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class IDDTests {

    @Test
    fun integerRangeTests() {
        DDBuilder {
            val a = IntegerRange(1, 1)
            val b = IntegerRange(2, 2)
            var c: IntegerRange = a + b
            val d = IntegerRange(3, 3)
            assertTrue(c == d)
            c = d - a
            assertTrue(b == c)
            var e: IntegerRange = b * d
            val f = IntegerRange(6, 6)
            assertTrue(e == f)
            e = f / b
            val g = IntegerRange(-6, -6)
            val h: IntegerRange = f.unaryMinus()
            assertTrue(h == g)
            val i: IntegerRange = -f
            assertTrue(i == g)
            val j = IntegerRange(2, 3)
            val k = IntegerRange(6, 7)
            val l: IntegerRange = k / j
            val m = IntegerRange(2, 4)
            assertTrue(l == m)
        }
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
            assertTrue(c2.index == e.index)
            assertTrue(e.height() == c2.height())
            val f = e.plus(d)
            assertEquals(2, f.height())
        }
    }

    @Test
    @Ignore
    fun testLogicalComparisonOperators() {
        DDBuilder {
            val a = this.integer(1L..1)
            val b = this.integer(2L..2)
            val c = a.lessThan(b)
            val d = b.greaterThan(a)
            assertEquals(True, c)
            assertEquals(True, d)
            val e = a.lessThanOrEquals(b)
            val f = b.greaterThanOrEquals(a)
            assertEquals(True, c)
            assertEquals(True, d)
        }
    }


    @Test
    @Ignore
    fun testIDDAssignment() {
        DDBuilder {
            val a = integer(10)
            val c = a
            assertEquals(c.getRange().min, 10)
            assertEquals(c.getRange().max, 10)
        }
    }

    @Test
    fun testIDDAddition() {
        DDBuilder {
            val a = integer(10)
            val b = integer(1)
            val r = a + b
            assertEquals(r.getRange().min, 11)
            assertEquals(r.getRange().max, 11)
        }
    }

    @Test
    fun testIDDAddition2() {
        DDBuilder {
            val a = integer(1L..2)
            val b = integer(2L..3)
            val c  = a + b
            assertEquals(c.min, 3)
            assertEquals(c.max, 5)
        }
    }

    @Test
    fun testIDDSubtraction() {
        DDBuilder {
            val a = integer(10)
            val b = integer(1)
            val r = a - b
            assertEquals(r.min, 9)
            assertEquals(r.max, 9)
        }
    }

    @Test
    fun testIDDMultiplication() {
        DDBuilder {
            val a = integer(10)
            val b = integer(2)
            val r = a * b
            assertEquals(r.min, 20)
            assertEquals(r.max, 20)
        }
    }

    @Test
    fun testIDDDivision() {
        DDBuilder {
            val a = integer(10)
            val b = integer(5)
            val r = a / b
            assertEquals(r.min, 2)
            assertEquals(r.max, 2)
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
    // Check if negation works for Scalar: value negates is - value.
    fun negate() {
        DDBuilder {
            val a = integer(10)
            //println("a = " + a.toString())
            val b = a.negate()
            //println("b = " + b.toString())
            assertEquals(-10, b.getRange().min)
            // Check if negation works for IDD: value + negated value = 0
            // val cond = conds.newConstraint(builder.AF(1, 2, 3), "")
            // val t = internal(cond, a, b)
            // val tn = t.negate()
            // val s = tn.plus(t)
            // assertEquals(0, s.getRange().min)
            // assertEquals(0, s.getRange().max)
            // This assertion or above assertions fail if merging when considering the quantization error prevents merge.
            // assertTrue(s.isLeaf)
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
    fun testInverseIDD1() {
        DDBuilder {
            // Including zero; division by Zero should result in infinity
            val intervalZeroBounding = this.integer(-2L..2)
            val inv = intervalZeroBounding.inv() as IDD.Leaf
            assertTrue(inv.value.isIntegers())

            // Infinity should be preserved
            val inf_node = IntegerRange.Empty
            // val inv2 = inf_node.inv() as IDD.Leaf // warning: This cast can never succeed
            // println("1/inf = $inv2 (shall be inf)")
            // assertTrue(inv2.value.isIntegerRangeNaN())

            // Regular
            val inv3_node = leaf(IntegerRange(1, 1))
            val inv3 = inv3_node.inv()
            assertEquals(1, inv3.min)
            assertEquals(1, inv3.max)

        }
    }

    // Div by zero returns NaN
    // Div considers rounding
    @Test
    fun testDivIDD1() {
        DDBuilder {
            val zero_node = integer(0)
            val affineForm1_node = leaf(IntegerRange(1, 1))
            val div = affineForm1_node.div(zero_node) as IDD.Leaf
            assertTrue(!div.value.isEmpty())

            // Regular division
            val a = integer(10)
            val b = integer(5)
            var result = a.div(b)
            assertEquals(result.min, 2)
            assertEquals(result.max, 2)

            //Division tested by inversion + multiplication
            val bi = b.inv()
            result = a.times(b.inv())
            assertEquals(result.min, 0)
            assertEquals(result.max, 10)
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
            assertTrue(c1.index == c2.index)
            val a = integer(1)
            val b = integer(2)
            val d = c1.ite(a, b)
            assertTrue(c1.index == d.index)
            assertTrue(c1.height() != 1)
            val e = c2.ite(a, b)
            assertTrue(c2.index == e.index)
            assertTrue(e.height() == c2.height())
            val f = e.plus(d)
            assertTrue(f.height() != 2)
        }
    }
}