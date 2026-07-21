package values.integer

import io.github.tukcps.aadd.values.BoundKind
import io.github.tukcps.aadd.values.XBool
import io.github.tukcps.aadd.values.integer.Bound
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.integer.bound
import io.github.tukcps.aadd.values.integer.root
import kotlin.test.*

class IntegerRangeTest {
    val a = IntegerRange(1, 1)
    val b = IntegerRange(2, 2)
    val d = IntegerRange(3, 3)
    val e = IntegerRange(7, 10)
    val f = IntegerRange(4, 4)
    val g = IntegerRange(2, 3)

    /**
     * An integer range is Long.MIN_VALUE to Long.MAX_VALUE.
     * */
    @Test fun testConstructor() {
        val anIR = IntegerRange()
        assertEquals(Long.MIN_VALUE, anIR.min)
        assertEquals(Long.MAX_VALUE, anIR.max)
        // assertTrue(IntegerRange.IntRangeAll)
    }

    @Test fun testConstructor2() {
        val anIR = IntegerRange(1, 2)
        assertEquals(1, anIR.min)
        assertEquals(2, anIR.max)
    }

    @Test fun testConstructor3() {
        val anIR = IntegerRange(1)
        assertEquals(1, anIR.min)
        assertEquals(1, anIR.max)
    }

    @Test fun testConstructor4() {
        val initLB = 1.0
        val initUB = 2.0
        val anIR = IntegerRange(initLB, initUB)
        assertEquals(1, anIR.min)
        assertEquals(2, anIR.max)
    }

    @Test fun testConstructor5() {
        val init = 1.0
        val anIR = IntegerRange(init)
        assertEquals(1, anIR.min)
        assertEquals(1, anIR.max)
    }

    @Test fun testConstructor6() {
        val init = IntegerRange(1, 2)
        val anIR = IntegerRange(init)
        assertEquals(1, anIR.min)
        assertEquals(2, anIR.max)
    }

    @Test
    fun testPlus() {
        val c = a + b
        assertEquals(c.min, d.min)
        assertEquals(c.max, d.max)
    }

    @Test
    fun testMinus() {
        val c = d - b
        assertEquals(c.max, a.max)
        assertEquals(c.min, a.min)
    }

    @Test
    fun testInfinityPlus1(){ // leads to range overflow
        val a = IntegerRange(1, IntegerRange.Integers.max)
        val b = IntegerRange(1, IntegerRange.Integers.max)
        val result = a+b
        assertFalse(result.isEmpty())
        assertEquals(IntegerRange.Integers.max,result.max)
    }

    @Test
    fun testInfinityPlus2(){ // leads to range overflow
        val a = IntegerRange(1, 1)
        val b = IntegerRange(1, IntegerRange.Integers.max)
        val result = a+b
        assertFalse(result.isEmpty())
        assertEquals(2,result.min)
        assertEquals(2,result.min)
        assertEquals(IntegerRange.Integers.max,result.max)
    }

    /**
     * [MIN..1] + [1..MAX] -> [MIN+1 .. *]
     */
    @Test
    fun testInfinityPlus3(){ // leads to range overflow
        val a = IntegerRange(IntegerRange.Integers.min, 1)
        val b = IntegerRange(1, IntegerRange.Integers.max)
        val result = a+b
        assertFalse(result.isEmpty())
        assertEquals(IntegerRange.Integers.min+1, result.min)
        assertEquals(BoundKind.FINITE, result.minKind)
        assertEquals(BoundKind.POSITIVE_INFINITY, result.maxKind)
        assertEquals(IntegerRange.Integers.max, result.max)
    }

    /**
     * MIN .. 1 - 1 .. MAX -> * .. MAX-1
     */
    @Test
    fun testInfinityMinus(){ // leads to range overflow
        val a = IntegerRange(IntegerRange.Integers.min, 1)
        val b = IntegerRange(1, IntegerRange.Integers.max)
        val result = a-b
        assertFalse(result.isEmpty())
        assertEquals(IntegerRange.Integers.min, result.min)
        assertEquals(BoundKind.NEGATIVE_INFINITY, result.minKind)
        assertEquals(0, result.max)
        assertEquals(BoundKind.FINITE, result.maxKind)
    }

    @Test
    fun minus() {
        assertEquals(
            IntegerRange(2L, 5L),
            IntegerRange(5L, 8L) - IntegerRange(3L, 3L)
        )

        assertEquals(
            IntegerRange(1L, 7L),
            IntegerRange(3L, 5L) - IntegerRange(-2L, 2L)
        )

        assertEquals(
            IntegerRange(-7L, -5L),
            IntegerRange(-5L, -3L) - IntegerRange(2L, 2L)
        )

        // Overflow to +∞
        assertEquals(
            IntegerRange(Bound.PositiveInfinity, Bound.PositiveInfinity),
            IntegerRange(Long.MAX_VALUE, Long.MAX_VALUE) - IntegerRange(-1L, -1L)
        )

        // Overflow to -∞
        assertEquals(
            IntegerRange(Bound.NegativeInfinity, Bound.NegativeInfinity),
            IntegerRange(Long.MIN_VALUE, Long.MIN_VALUE) - IntegerRange(1L, 1L)
        )

        // Finite - (+∞)
        assertEquals(
            IntegerRange(5L.bound(), Bound.PositiveInfinity),
            IntegerRange(5L, 5L) - IntegerRange(Bound.NegativeInfinity, 0L.bound())
        )

        // (+∞) - finite
        assertEquals(
            IntegerRange(5L.bound(), Bound.PositiveInfinity),
            IntegerRange(5L.bound(), Bound.PositiveInfinity) - IntegerRange(0L, 0L)
        )
    }

    @Test
    fun testInfinityTimes1(){ // leads to range overflow
        val a = IntegerRange(1, IntegerRange.Integers.max)
        val b = IntegerRange(IntegerRange.Integers.min, IntegerRange.Integers.max)
        val result = a*b
        assertFalse(result.isEmpty())
        assertEquals(IntegerRange.Integers.min,result.min)
        assertEquals(IntegerRange.Integers.max,result.max)
    }

    @Test
    fun testInfinityTimes2(){ // leads to range overflow
        val a = IntegerRange(1, IntegerRange.Integers.max)
        val b = IntegerRange(-2, -2)
        val result = a*b
        assertFalse(result.isEmpty())
        assertEquals(IntegerRange.Integers.min,result.min)
        assertEquals(-2,result.max)
    }

    @Test
    fun testInfinityTimes3(){ // leads to range overflow
        val a = IntegerRange(1, IntegerRange.Integers.max)
        val b = IntegerRange(1, IntegerRange.Integers.max)
        val c = IntegerRange(1, IntegerRange.Integers.max)
        val result = a*b*c
        assertFalse(result.isEmpty())
        assertEquals(1,result.min)
        assertEquals(IntegerRange.Integers.max,result.max)
    }

    @Test
    fun testInfinityDiv1(){ // leads to range overflow
        val a = IntegerRange(1, IntegerRange.Integers.max)
        val b = IntegerRange(1, IntegerRange.Integers.max)
        val result = a/b
        assertFalse(result.isEmpty())
        assertEquals(0,result.min)
        assertEquals(IntegerRange.Integers.max,result.max)
    }


    @Test @Ignore
    fun testRoot(){
        val a = IntegerRange(3,3)
        val b = IntegerRange(125,125)
        val result=b.root(a)
        assertEquals(5,result.min)
        assertEquals(5,result.max)
    }

    @Test
    fun testLog(){
        val a = IntegerRange(32,32)
        val b = IntegerRange(2,2)
        val result=a.log(b)
        assertEquals(5,result.min)
        assertEquals(5,result.max)
    }

    @Test
    fun testMod() {
        val c = e % f
        assertEquals(c.max, g.max)
        assertEquals(c.min, g.min)
    }

    @Test
    fun testEquals() {
        var left = IntegerRange(1, 1)
        var right = IntegerRange(1, 1)
        assertEquals(left, right)
        var negleft : IntegerRange = - left
        var negright : IntegerRange = - right
        assertEquals(negleft, negright)
        right = IntegerRange(2, 2)
        assertTrue(left != right)
        assertEquals(right.greaterThan(other = left), XBool.True)
        assertEquals(left.lessThan(other = right), XBool.True)
        negright = - right
        assertTrue(negleft != negright)
        assertEquals(negright.lessThan(other = negleft), XBool.True)

        assertEquals(negleft.greaterThan(negright), XBool.True)
        left = IntegerRange(1, 4)
        right = IntegerRange(3, 4)
        negleft = - left
        negright = - right
        /*
        if (left.lessThanOrEquals(right) == XBool.True)

           //println("left <= right")
         */
        assertEquals(left.lessThanOrEquals(right), XBool.True)
        /*
        if (negright.greaterThanOrEquals(negleft) == XBool.True)

           //println("negright >= negleft")
         */
        assertEquals(negright.greaterThanOrEquals(negleft), XBool.True)
        left = IntegerRange(3, 4)
        right = IntegerRange(3, 5)
        /*
        if (right.greaterThanOrEquals(left) == XBool.True)

           //println("right >= left")
         */
        assertEquals(right.greaterThanOrEquals(left), XBool.True)
        /*
        if (negright.lessThanOrEquals(negleft) == XBool.True)

           //println("negright <= negleft")
         */
        assertEquals(negright.lessThanOrEquals(negleft), XBool.True)
    }

    @Test
    fun testIntegerRange() {
        val a = IntegerRange(1, 2)
        assertEquals(a, IntegerRange(1, 2))
        val b = IntegerRange(2, 3)
        val c: IntegerRange = a.plus(b)
        assertEquals(c, IntegerRange(3, 5))
        val d: IntegerRange = c.minus(b)
        assertEquals(d, IntegerRange(0, 3))
        val e = a.times(b)
        assertEquals(e, IntegerRange(2, 6))
        val f = IntegerRange(2, 4)
        val g = f.div(2)
        assertEquals(g, IntegerRange(1, 2))
        val h = IntegerRange(30, 40)
        val i = IntegerRange(2, 3)
        val j = h.div(i)
        assertEquals(j, IntegerRange(10, 20))
        val aneg = a.unaryMinus()
        assertEquals(aneg, IntegerRange(-2, -1))
        val l = IntegerRange(1, 2)
        val m = IntegerRange(3, 4)
        val n = IntegerRange(1, 2)
        val q = IntegerRange(3, 5)
        assertSame(l.lessThan(other = m), XBool.True)
        assertSame(l.lessThanOrEquals(other = m), XBool.True)
        assertSame(l.lessThanOrEquals(other = n), XBool.True)
        assertSame(m.greaterThan(other = l), XBool.True)
        assertSame(m.greaterThanOrEquals(other = l), XBool.True)
        assertSame(n.greaterThanOrEquals(other = l), XBool.True)
        val o = q.minus(l)
        assertEquals(o, IntegerRange(1, 4))
        assertEquals(1L, o.min)
        assertEquals(4L, o.max)
        val p = l.minus(q)
        assertEquals(p, IntegerRange(-4, -1))
        assertEquals(-4L, p.min)
        assertEquals(-1L, p.max)
    }

    @Test
    fun testDivisionSpecialCases() {

        // positive / 0 = +∞
        assertEquals(
            IntegerRange(Bound.PositiveInfinity, Bound.PositiveInfinity),
            IntegerRange(5, 10) / IntegerRange(0)
        )

        // negative / 0 = -∞
        assertEquals(
            IntegerRange(Bound.NegativeInfinity, Bound.NegativeInfinity),
            IntegerRange(-10, -5) / IntegerRange(0)
        )

        // mixed / 0 = [-∞,+∞]
        assertEquals(
            IntegerRange(Bound.NegativeInfinity, Bound.PositiveInfinity),
            IntegerRange(-5, 5) / IntegerRange(0)
        )

        // 0 / 0 = [-∞,+∞]
        assertEquals(
            IntegerRange(Bound.NegativeInfinity, Bound.PositiveInfinity),
            IntegerRange(0) / IntegerRange(0)
        )

        // divisor contains zero
        assertEquals(
            IntegerRange(Bound.NegativeInfinity, Bound.PositiveInfinity),
            IntegerRange(10, 20) / IntegerRange(-1, 1)
        )
    }

    @Test
    fun testDivisionIntervals() {

        assertEquals(
            IntegerRange(2),
            IntegerRange(6) / IntegerRange(3)
        )

        assertEquals(
            IntegerRange(2, 3),
            IntegerRange(6, 7) / IntegerRange(2, 3)
        )

        assertEquals(
            IntegerRange(-3, -2),
            IntegerRange(6, 7) / IntegerRange(-3, -2)
        )

        assertEquals(
            IntegerRange(-3, -2),
            IntegerRange(-7, -6) / IntegerRange(2, 3)
        )

        assertEquals(
            IntegerRange(2, 3),
            IntegerRange(-7, -6) / IntegerRange(-3, -2)
        )
    }

    @Test
    fun testDivisionInfinity() {

        assertEquals(
            IntegerRange(Bound.PositiveInfinity, Bound.PositiveInfinity),
            IntegerRange(Bound.PositiveInfinity, Bound.PositiveInfinity) / IntegerRange(2)
        )

        assertEquals(
            IntegerRange(Bound.NegativeInfinity, Bound.NegativeInfinity),
            IntegerRange(Bound.NegativeInfinity, Bound.NegativeInfinity) / IntegerRange(2)
        )

        assertEquals(
            IntegerRange(Bound.NegativeInfinity, Bound.PositiveInfinity),
            IntegerRange(Bound.NegativeInfinity, Bound.PositiveInfinity) / IntegerRange(2)
        )
    }

    @Test
    fun testSubset() {
        val setA = IntegerRange(2, 3)
        val setB = IntegerRange(1, 4)
        val setC = IntegerRange(1, 4)
        /*
        if (setA.isSubsetof(setB))

           //println("setA is a subset of setB")
        */
        assertTrue(setA in setB)
        /*
        if (setA.isProperSubsetof(setB))

           //println("setA is a proper subset of setB")
        */
        assertTrue(setA.isProperSubsetof(setB))
        /*
        if (!setC.isProperSubsetof(setB))
         */

        //println("setC is not a proper subset of setB")
        assertTrue(!setC.isProperSubsetof(setB))
    }

    @Test
    fun testEmpty() {
        val anIR : IntegerRange = IntegerRange.Empty
        if (anIR.isEmpty())
            assertTrue(anIR.isEmpty())
    }

    @Test
    fun testSqr() {
        val a = IntegerRange(1, 2)
        val aSquare = a.sqr()
        assertEquals(1, aSquare.min)
        assertEquals(4, aSquare.max)

        val b = IntegerRange(-10, 2)
        val bSquare = b.sqr()
        assertEquals(0, bSquare.min)
        assertEquals(100, bSquare.max)

        val c = IntegerRange(1, 100000000000)
        val cSquare = c.sqr()
        assertEquals(1, cSquare.min)
        assertTrue(!cSquare.isFinite)
    }

    @Test
    fun testSqrt() {
        val a = IntegerRange(1, 2)
        val aSqrt = a.sqrt()
        assertEquals(1, aSqrt.min)
        assertEquals(2, aSqrt.max)
    }
}