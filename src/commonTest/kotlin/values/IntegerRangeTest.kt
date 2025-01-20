package examples.values

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.XBool
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
        assertTrue(c.max == a.max)
        assertTrue(c.min == a.min)
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

    @Test
    fun testInfinityPlus3(){ // leads to range overflow
        val a = IntegerRange(IntegerRange.Integers.min, 1)
        val b = IntegerRange(1, IntegerRange.Integers.max)
        val result = a+b
        assertFalse(result.isEmpty())
        assertEquals(IntegerRange.Integers.min,result.min)
        assertEquals(IntegerRange.Integers.max,result.max)
    }

    @Test
    fun testInfinityMinus(){ // leads to range overflow
        val a = IntegerRange(IntegerRange.Integers.min, 1)
        val b = IntegerRange(1, IntegerRange.Integers.max)
        val result = a-b
        assertFalse(result.isEmpty())
        assertEquals(IntegerRange.Integers.min,result.min)
        assertEquals(0,result.max)
    }

    @Test
    fun testInfinityMinus2(){ // leads to range overflow
        val a = IntegerRange(1, IntegerRange.Integers.max)
        val b = IntegerRange(IntegerRange.Integers.min, 1)
        val result = a-b
        assertFalse(result.isEmpty())
        assertEquals(0,result.min)
        assertEquals(IntegerRange.Integers.max,result.max)
    }

    @Test
    fun testInfinityMinus3(){ // leads to range overflow
        val a = IntegerRange(9, 10)
        val b = IntegerRange(IntegerRange.Integers.min, IntegerRange.Integers.max)
        val result = a-b
        assertFalse(result.isEmpty())
        assertEquals(IntegerRange.Integers.min,result.min)
        assertEquals(IntegerRange.Integers.max,result.max)
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
    fun testSqrt(){
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

        //println("c = " + c)

        //println("e = " + e)

        //println("f = " + f)

        //println("g = " + g)
        //if (c == g)
        //
        // println("c == g")
        assertTrue(c.max == g.max)
        assertTrue(c.min == g.min)
    }

    @Test
    fun testEquals() {
        var left = IntegerRange(1, 1)
        var right = IntegerRange(1, 1)
        assertTrue(left == right)
        var negleft : IntegerRange = - left
        var negright : IntegerRange = - right
        assertTrue(negleft == negright)
        right = IntegerRange(2, 2)
        assertTrue(left != right)
        assertTrue(right.greaterThan(other = left) == XBool.True)
        assertTrue(left.lessThan(other = right) == XBool.True)
        negright = - right
        assertTrue(negleft != negright)
        assertTrue(negright.lessThan(other = negleft) == XBool.True)

        assertTrue(negleft.greaterThan(negright) == XBool.True)
        left = IntegerRange(1, 4)
        right = IntegerRange(3, 4)
        negleft = - left
        negright = - right
        /*
        if (left.lessThanOrEquals(right) == XBool.True)

           //println("left <= right")
         */
        assertTrue(left.lessThanOrEquals(right) == XBool.True)
        /*
        if (negright.greaterThanOrEquals(negleft) == XBool.True)

           //println("negright >= negleft")
         */
        assertTrue(negright.greaterThanOrEquals(negleft) == XBool.True)
        left = IntegerRange(3, 4)
        right = IntegerRange(3, 5)
        /*
        if (right.greaterThanOrEquals(left) == XBool.True)

           //println("right >= left")
         */
        assertTrue(right.greaterThanOrEquals(left) == XBool.True)
        /*
        if (negright.lessThanOrEquals(negleft) == XBool.True)

           //println("negright <= negleft")
         */
        assertTrue(negright.lessThanOrEquals(negleft) == XBool.True)
    }

    @Test
    fun testIntegerRange() {
        val a = IntegerRange(1, 2)
        assertTrue(a == IntegerRange(1, 2))
        val b = IntegerRange(2, 3)
        val c: IntegerRange = a.plus(b)
        assertTrue(c == IntegerRange(3, 5))
        val d: IntegerRange = c.minus(b)
        //System.out
        // println("a = " + a);
        //System.out
        // println("b = " + b);
        //System.out
        // println("c = " + c);
        //System.out
        // println("d = " + d);
        assertTrue(d == IntegerRange(0, 3))
        val e = a.times(b)
        assertTrue(e == IntegerRange(2, 6))
        val f = IntegerRange(2, 4)
        val g = f.div(2)
        assertTrue(g == IntegerRange(1, 2))
        val h = IntegerRange(30, 40)
        val i = IntegerRange(2, 3)
        val j = h.div(i)
        //System.out
        // println("j = " + j);
        assertTrue(j == IntegerRange(10, 20))
        val aneg = a.unaryMinus()
        assertTrue(aneg == IntegerRange(-2, -1))
        val l = IntegerRange(1, 2)
        val m = IntegerRange(3, 4)
        val n = IntegerRange(1, 2)
        val q = IntegerRange(3, 5)
        assertTrue(l.lessThan(other = m) === XBool.True)
        assertTrue(l.lessThanOrEquals(other = m) === XBool.True)
        assertTrue(l.lessThanOrEquals(other = n) === XBool.True)
        assertTrue(m.greaterThan(other = l) === XBool.True)
        assertTrue(m.greaterThanOrEquals(other = l) === XBool.True)
        assertTrue(n.greaterThanOrEquals(other = l) === XBool.True)
        val o = q.minus(l)
        // System.out
        // println("o = " + o.toString());
        assertTrue(o == IntegerRange(1, 4))
        assertTrue(o.min == 1L)
        assertTrue(o.max == 4L)
        val p = l.minus(q)
        // System.out
        // println("p = " + p.toString());
        assertTrue(p == IntegerRange(-4, -1))
        assertTrue(p.min == -4L)
        assertTrue(p.max == -1L)
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
    fun testIsIntRangeNaN() {
        val anIR : IntegerRange = IntegerRange.Empty
        if (anIR.isEmpty())
        //println("anIR is NaN")
            assertTrue(anIR.isEmpty())
    }
}