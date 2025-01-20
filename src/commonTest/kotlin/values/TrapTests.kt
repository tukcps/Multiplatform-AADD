package values

import com.github.tukcps.aadd.*
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.Range
import kotlin.test.*

/**
 * In jAADD, special cases for FP operation results are defined and handled separately:
 * - Empty (Empty range; min > max and no partial deviations; central and r are 0),
 * - Reals (+- Infinity; no partial deviations; central value is 0, and r is infinity),
 * - RealsNaN (NaN; completely invalid result).
 */

class TrapTests {

    @Test
    fun constantsTest() {
        DDBuilder {
            val x = Range.Reals
            assertTrue(x.maxIsInf)
            assertTrue(x.minIsInf)
            assertTrue(x.min < x.max)

            val afReals = AFReals
            assertTrue(afReals.maxIsInf)
            assertTrue(afReals.minIsInf)
            assertTrue(afReals.min < afReals.max)
            assertTrue(!afReals.isFinite())
            assertTrue(!afReals.isFinite())

            val reals = Reals
            assertTrue(reals.maxIsInf)
            assertTrue(reals.minIsInf)
            assertTrue(reals.min < reals.max)
        }
    }

    /** Operations with Empty set return empty set */
    @Test
    fun arithmeticOperationsWithEmpty() {
        DDBuilder {
            // Arithmetic operations with one operand Empty shall return Empty
            val a = Empty
            val b: AADD = real(1.0..2.0)
            var c = a + b
            assertTrue(c === Empty)
            c = a * b
            assertTrue(c === Empty)
            c = a / b
            assertTrue(c === Empty)
            c = b + a
            assertTrue(c === Empty)
        }
    }


    @Test
    fun divisionByZeroInRange() {
        DDBuilder {
            val b: AADD = real(1.0..2.0)
            val c = b/Reals
            assertFalse(c.isEmpty())
            assertEquals(Reals.min, c.min)
            assertEquals(Reals.max, c.max)
        }
    }

    /** Operations with Empty set return InfeasibleB for comparisons */
    @Test
    fun relationOperationsWithEmpty() {
        DDBuilder {
            // Relations with BDD shall be marked as "InfeasibleB"; the best match for a result.
            val b = real(1.0..2.0)
            val c = Empty greaterThan b
            assertSame(InfeasibleB, c)
        }
    }

    /** Reals are represented as +-Infinity; maintained by addition */
    @Test
    fun overflowTest() {
        DDBuilder{
            val a = Reals
            val b = Reals
            val c = a + b
            assertTrue(a.maxIsInf)
            assertTrue(a.minIsInf)
            assertTrue(c.minIsInf)
            assertTrue(c.maxIsInf)
        }
    }

    @Test
    fun emptyConstrainToTest() {
        DDBuilder {
            val a = Empty
            val b = real(1.0 .. 2.0)
            // val c = a constrainTo b
            val d = b constrainTo a
            assertTrue( !(d as AADD.Leaf).radius.isNaN() )
        }
    }

    /**
     * 'Reals' is correctly cloned.
     */
    @Test
    fun realsCloned() {
        DDBuilder {
            val af = AFReals.clone()
            assertTrue(af.maxIsInf)
            assertTrue(af.minIsInf)

            val a = Reals
            val b = a.clone()
            assertTrue(b.maxIsInf)
            assertTrue(b.minIsInf)
            assertEquals(Double.NEGATIVE_INFINITY, b.min)
            assertEquals(Double.POSITIVE_INFINITY, b.max)
            assertTrue(b is AADD.Leaf)
        }
    }

    @Test
    fun realsConstrainToScalar() {
        DDBuilder {
            val r = Reals constrainTo real(2.0)
            assertEquals(r.min, 2.0, 0.00001)
            assertEquals(0.0, (r as AADD.Leaf).r, 0.000001)
            assertEquals(r.max, 2.0, 0.00001)
            val z = real(2.0) constrainTo Reals
            assertEquals(r.min, 2.0, 0.00001)
            assertEquals(0.0, (z as AADD.Leaf).r, 0.000001)
            assertEquals(r.max, 2.0, 0.00001)
        }
    }

    @Test
    fun realsConstrainToRange() {
        DDBuilder {
            val a = Reals
            val b = real(1.0..2.0)
            val c = a constrainTo b
            assertEquals(1.0, c.min)
            assertEquals(2.0, c.max)
            val d = b constrainTo a
            assertEquals(1.0, d.min)
            assertEquals(2.0, d.max)
            val e = a intersect b
            assertEquals(1.0, e.min)
            assertEquals(2.0, e.max)
            val f = b intersect a
            assertEquals(1.0, f.min)
            assertEquals(2.0, f.max)
            val g = Reals intersect Reals
            assertEquals(Double.NEGATIVE_INFINITY, g.min)
            assertTrue( (g as AADD.Leaf).r == Double.POSITIVE_INFINITY)
            assertEquals(Double.POSITIVE_INFINITY, g.max)
        }
    }

    @Test
    fun timesRealsTest() {
        DDBuilder {
            val c = AFReals * AffineForm(this, 2.0..3.0,)
            assertTrue(c.maxIsInf)
            assertTrue(c.minIsInf)

            val d = AFReals * AFReals
            assertTrue(d.maxIsInf)
            assertTrue(d.r == Double.POSITIVE_INFINITY)
            assertTrue(d.minIsInf)

            val f = Reals * real(2.0..3.0)
            assertTrue(f.maxIsInf)
            assertTrue( (f as AADD.Leaf).value.r == Double.POSITIVE_INFINITY)
            assertTrue(f.minIsInf)

            val i = Reals * Reals
            assertTrue(i.maxIsInf)
            assertTrue( (i as AADD.Leaf).value.r == Double.POSITIVE_INFINITY)
            assertTrue(i.minIsInf)
        }
    }

    @Test
    fun invRealTest() {
        DDBuilder {
            val d = real(-1.0..1.0)
            val result = d.inv()
            assertEquals(Double.NEGATIVE_INFINITY,result.min)
            assertEquals(Double.POSITIVE_INFINITY,result.max)
        }
    }
}