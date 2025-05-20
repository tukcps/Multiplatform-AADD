package aaddtests

import com.github.tukcps.aadd.*
import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.Range
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class AADDIntersectTests {

    /**
     *  Test the intersection of an AADD with an interval; constrained by a narrower interval.
     *  The result must be the AADD, with additional linear constraints limited to the interval.
     *  This must take effect after calling the LP solver.
     */
    @Test fun intersectNarrowIntervalTest() {
        DDBuilder {
            val a = real(1.0 .. 3.0)
            val b = a.constrainTo(Range(1.2 .. 2.2))
            // println("a=$a intersect [1.2, 2.2] as AADD is: $b")
            // println("a's range computed by LP solver is: " + b)  // getRange calls the LP solver.
            assertEquals(1.2, b.getRange().min, 0.001)
            assertEquals(2.2, b.getRange().max, 0.001)
        }
    }

    /**
     * Test the intersection of an AADD with an interval; constrained by a wider interval
     */
    @Test fun intersectWiderIntervalTest() {
        DDBuilder {
            // println("=== Testing: Intersection of AADD with wider interval (min, max) ===")
            val a = real(1.0 .. 3.0)
            val b = a.constrainTo(Range(0.5 .. 4.0))
            // println("a=$a intersect [0.5, 4] is: $b")
            // println("b's range computed by LP is:" + b.getRange())
            assertEquals(1.0, b.getRange().min, 0.001)
            assertEquals(3.0, b.getRange().max, 0.001)
        }
    }

    /**
     * Test the intersection of an AADD with a Range-type object
     */
    @Test fun intersectRangeTest() {
        DDBuilder {
            // println("=== Testing: Intersection of AADD with Range ===")
            val a = real(1.5..3.0, "a")
            val c = a.constrainTo(Range(0.9..2.2))
            // println("a = $a")
            // println("a intersect b = $c")
            // println("with range:" + c.getRange())
            assertEquals(1.5, c.getRange().min, 0.001)
            assertEquals(2.2, c.getRange().max, 0.001)
        }
    }

    /** Test the intersection of an AADD with an AffineForm */
    @Test
    fun intersectTestAffineForm() {
        DDBuilder{
            val a = real(1.5..3.0, "a")
            val b = AffineForm(this,1.2 .. 2.2, "b")
            val c = a.intersect(AADD.Leaf(this, b))
            assertEquals(1.5, c.getRange().min, 0.001)
            assertEquals(2.2, c.getRange().max, 0.001)
        }
    }


    /** Test the intersection of an AADD with a Range via constraintToRange */
    @Test fun constrainToRange() {
        DDBuilder{
            val a = real(-50.0 .. 50.0)
            val r = Range(0.0 .. 50.0)
            val c = a.constrainTo(r)
            assertEquals(0.0, c.getRange().min, 0.00001)
            assertEquals(50.0, c.getRange().max, 0.00001)
        }
    }


    /**
     * Test the intersection of an AADD with an AADD
     */
    @Test
    fun intersectTestAADD() {
        DDBuilder{
            val a = real(2.2 .. 3.0, "a")
            val b = real(1.2 .. 2.5, "b")
            val c = a intersect b
            assertEquals(2.2, c.getRange().min, 0.001)
            assertEquals(2.5, c.getRange().max, 0.001)
        }
    }

    @Test
    // Intersection of complete ranges.
    fun intersectCompleteTest() {
        DDBuilder{
            var a: AADD = real(0.98..1.05, "a")
            val b: AADD = real(0.99..1.01, "b")
            a = a.intersect(b)
            assertTrue(a in 0.989 .. 1.051)
        }
    }

    @Test
    // Intersection of complete ranges.
    fun intersectCompleteBTest() {
        DDBuilder{
            var a: AADD = real(0.98..1.05, "a")
            val b: AADD = real(0.99..1.01, "b")
            a = b.intersect(a)
            assertTrue(a in 0.989 .. 1.051)
        }
    }

    @Test
    // Intersection with Reals of a finite range shall return leaf with range.
    fun intersectInfTest() {
        DDBuilder {
            val a = Reals
            val b = real(1.0..2.0, "b")
            val y = a intersect b // as AADD.Leaf
            assertEquals(b.min, y.min, 0.00001)
            assertEquals(b.max, y.max, 0.00001)

            val z = (b intersect a) // as AADD.Leaf
            assertEquals(b.min, z.min, 0.00001)
            assertEquals(b.max, z.max, 0.00001)
        }
    }

    @Test
    // Intersection and Clone working together
    fun intersectCloned() {
        DDBuilder {
            //ERROR does not happen
            val a1: AADD = real(1.0 .. 3.0)
            val b1: AADD = AADD.Leaf(this, AFEmpty, DD.Status.NotSolved) //Empty Set
            val c1 = a1.clone()

            c1.intersect(b1)

            //ERROR happens
            val a2: AADD = real(2.0 .. 4.0)
            val b2: AADD = real(0.0 .. 0.0)
            val c2 = a2.clone()

            c2.intersect(b2)

            // println("A1: $a1, B1: $b1, C1: $c1")
            // println("A2: $a2, B2: $b2, C2: $c2")

            // println(a1 == c1)
        }
    }

    /** Intersection with single value and single value gives single value. */
    @Test
    fun intersectPoint() {
        DDBuilder {
            val a = real(1000.0 .. 1000.0)
            val b = real(4.0..4.0)/real(3.0..3.0)*a*a*a //+ range(-0.001, 0.001)
            assertTrue( (b intersect real(1000.0*4.0*1000.0/3.0*1000.0)) in 13.33E8 .. 13.34E8)
            assertTrue( (real(4.0/3.0*1000.0*1000.0*1000.0) intersect b) in 13.33E8 .. 13.34E8)
        }
    }

    /** Intersection of scalar value and Reals gives scalar value. */
    @Test
    fun intersectPointReals() {
        DDBuilder {
            val a = real(1000.0)
            val res = a intersect Reals
            assertTrue( a in res )
            assertEquals( a.max , res.max )
            assertEquals( a.min, res.min )
            assertTrue( res is AADD.Leaf)
            assertEquals(0.0, res.value.r)
            assertEquals(0, res.value.xi.size)
        }
    }

    /** Intersect with a range that has no sensitivities and a scalar should work. */
    @Test
    fun intersectRangeWithNoSensitivities() {
        DDBuilder {
            val af = AffineForm(this, central = 10.0, r = 1.0, xi= hashMapOf(), range = 9.0..11.0)
            af.min = 9.0
            af.max = 11.0
            assertEquals(0, af.xi.size)
            val b: AADD.Leaf = leaf(af)
            assertEquals(0, b.value.xi.size)
            assertEquals(10.5, (b intersect real(10.5)).getRange().min, 0.00001)
            assertEquals(9.5, (real(9.5) intersect b).getRange().min, 0.00001)
            assertTrue( 9.5 .. 9.6 in ( real(9.5 .. 9.6) intersect b) )
            assertTrue( 9.5 .. 9.6 in ( b intersect real(9.5 .. 9.6)) )
        }
    }

    @Test
    fun cornerCase() {
        DDBuilder {
            val a = real(1.0 .. 2.0)
            val b = a.constrainTo(Range(2.0..2.00001))
            assertTrue(b.isScalar())
            assertTrue( (b as AADD.Leaf).value.xi.isEmpty())
        }
    }
}
