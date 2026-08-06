package values

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.RealMath.constrainTo
import io.github.tukcps.aadd.DDBuilder.RealMath.div
import io.github.tukcps.aadd.DDBuilder.RealMath.inv
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.times
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.times
import io.github.tukcps.aadd.values.real.ia.RealRange
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
            val x = RealRange.Reals
            assertTrue(x.max.isInfinite)
            assertTrue(x.min.isInfinite)
            assertTrue(x.min < x.max)

            val afReals = AF.All
            assertTrue(afReals.max.isInfinite)
            assertTrue(afReals.min.isInfinite)
            assertTrue(afReals.min < afReals.max)
            assertTrue(!afReals.isFinite())
            assertTrue(!afReals.isFinite())

            val reals = Reals.All
            assertTrue(reals.max.isInfinite)
            assertTrue(reals.min.isInfinite)
            assertTrue(reals.min < reals.max)
        }
    }

    /** Operations with Empty set return empty set */
    @Test
    fun arithmeticOperationsWithEmpty() {
        DDBuilder {
            // Arithmetic operations with one operand Empty shall return Empty
            val a = Reals.Empty
            val b: Real = real(1.0..2.0)
            var c = a + b
            assertSame(c, Reals.Empty)
            c = a * b
            assertSame(c, Reals.Empty)
            c = a / b
            assertSame(c, Reals.Empty)
            c = b + a
            assertSame(c, Reals.Empty)
        }
    }


    @Test
    fun divisionByZeroInRange() {
        DDBuilder {
            val b: AADD = real(1.0..2.0)
            val c = b/Reals.All
            assertFalse(c.isEmpty())
            assertSame(c, Reals.All)
            assertEquals(Reals.All.min, c.min)
            assertEquals(Reals.All.max, c.max)
        }
    }

    /** Operations with Empty set return Infeasible for comparisons */
    @Test
    fun relationOperationsWithEmpty() {
        DDBuilder {
            // Relations with BDD shall be marked as "InfeasibleB"; the best match for a result.
            val b = real(1.0..2.0)
            val c = Reals.Empty greaterThan b
            assertSame(Bool.Infeasible, c)
        }
    }

    /** Reals are represented as +-Infinity; maintained by addition */
    @Test
    fun overflowTest() {
        DDBuilder {
            val a = Reals.All
            val b = Reals.All
            val c = a + b
            assertSame(c, Reals.All)
            assertTrue(a.max.isInfinite)
            assertTrue(a.min.isInfinite)
            assertTrue(c.min.isInfinite)
            assertTrue(c.max.isInfinite)
        }
    }

    @Test
    fun emptyConstrainToTest() {
        DDBuilder {
            val a = Reals.Empty
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
            val af = AF.All.clone()
            assertTrue(af.max.isInfinite)
            assertTrue(af.min.isInfinite)

            val a = Reals.All
            val b = a.clone()
            assertTrue(b.max.isInfinite)
            assertTrue(b.min.isInfinite)
            assertEquals(Double.NEGATIVE_INFINITY, b.min.toDouble())
            assertEquals(Double.POSITIVE_INFINITY, b.max.toDouble())
            assertTrue(b is AADD.Leaf)
        }
    }

    @Test
    fun realsConstrainToScalar() {
        DDBuilder {
            val r = Reals.All constrainTo real(2.0)
            assertEquals(2.0, r.min.toDouble(), 0.00001)
            assertEquals(2.0, r.max.finiteValue, 0.00001)
            val z = real(2.0) constrainTo Reals.All
            assertEquals(2.0, z.min.finiteValue, 0.00001)
            assertEquals(2.0, z.max.finiteValue, 0.00001)
        }
    }

    @Test
    fun realsConstrainToRange() {
        DDBuilder {
            val a = Reals.All
            val b = real(1.0..2.0)
            val c = a constrainTo b
            assertEquals(1.0, c.min.finiteValue)
            assertEquals(2.0, c.max.finiteValue)
            val d = b constrainTo a
            assertEquals(1.0, d.min.finiteValue)
            assertEquals(2.0, d.max.finiteValue)
            val e = a intersect b
            assertEquals(1.0, e.min.finiteValue)
            assertEquals(2.0, e.max.finiteValue)
            val f = b intersect a
            assertEquals(1.0, f.min.finiteValue)
            assertEquals(2.0, f.max.finiteValue)
            val g = Reals.All intersect Reals.All
            assertEquals(Double.NEGATIVE_INFINITY, g.min.toDouble())
            assertEquals(Double.POSITIVE_INFINITY, g.max.toDouble())
        }
    }

    @Test
    fun timesRealsTest() {
        DDBuilder {
            val c = AF.All * AffineForm.range(this, RealRange(2.0..3.0))
            assertTrue(c.max.isInfinite)
            assertTrue(c.min.isInfinite)

            val d = AF.All * AF.All
            assertTrue(d.max.isInfinite)
            assertTrue(d.min.isInfinite)

            val f = Reals.All * real(2.0..3.0)
            assertTrue(f.max.isInfinite)
            assertTrue(f.min.isInfinite)

            val i = Reals.All * Reals.All
            assertTrue(i.max.isInfinite)
            assertTrue(i.min.isInfinite)
        }
    }

    @Test
    fun invRealTest() {
        DDBuilder {
            val d = real(-1.0..1.0)
            val result = inv(d)
            assertEquals(Double.NEGATIVE_INFINITY,result.min.toDouble())
            assertEquals(Double.POSITIVE_INFINITY,result.max.toDouble())
        }
    }
}