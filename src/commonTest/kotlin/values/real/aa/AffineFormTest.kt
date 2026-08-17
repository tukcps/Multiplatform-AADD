package values.real.aa

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.*
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.create
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.util.Assertions.assertEquals
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AffineFormTest {

    private val precision = 0.000001

    /** A real is the range +-MAX_VALUE. It is identified by isReal() */
    @Test fun realTest() {
        DDBuilder {
            val real = AF.All
            assertEquals(Double.NEGATIVE_INFINITY, real.min.toDouble())
            assertEquals(Double.POSITIVE_INFINITY, real.max.toDouble())
            assertTrue(real.isReals())
        }
    }

    @Test
    fun overloadedOperatorsTest() {
        DDBuilder {
            val a = AffineForm.range(this, 1.0..2.0, "1")
            val b = AffineForm.range(this, 2.0..3.0, "1")
            var y = a - b
            assertEquals(0.0, y.radius, 0.000001)

            y = a*b
            assertEquals(2.0, y.min.toDouble(), 0.000001)
            assertEquals(6.0, y.max.toDouble(), 0.000001)
        }
    }


    @Test
    fun testAddForms() {
        DDBuilder{
            val af1 = AffineForm.range(this, 1.0..2.0, "1")
            val af2 = AffineForm.range(this, 1.0..2.0, "2")

            val sum = af1 + af2
            var termValueSum = 0.0
            for (v in sum.xi.values) {
                termValueSum += v
            }
            assertEquals(3.0, sum.central, precision)
            assertEquals(2.0, sum.min.toDouble(), precision)
            assertEquals(4.0, sum.max.toDouble(), precision)
            assertEquals(1.0, sum.radius, precision)
            assertEquals(2, sum.xi.keys.size)
            assertTrue(sum.xi.keys.contains(1))
            assertTrue(sum.xi.keys.contains(2))
            assertEquals(termValueSum, sum.radius, precision)
        }
    }

    @Test
    fun testAddFormsCustomRange() {
        DDBuilder{
            val af1 = AffineForm.range(this, 1.0..2.0, "1")
            var terms = hashMapOf(1L to 2.0, 2L to 1.0)
            // Restricted range through manual min/max values
            terms = HashMap()
            terms[2] = 0.5
            val rst = create(this, RealRange(1.1..1.9), 1.5, 0.0, terms)

            val sum = af1 + rst
            assertEquals(3.0, sum.central, precision)
            assertEquals(2.1, sum.min.toDouble(), precision)
            assertEquals(3.9, sum.max.toDouble(), precision)
            // Doesn't change with artificial range!
            assertEquals(1.0, sum.radius, precision)
        }
    }


    /** Adding something to infinity results in infinity */
    @Test fun testAddInfinite() {
        DDBuilder{
            val af1 = AffineForm.range(this, 1.0..2.0, "1")
            val af3 = AffineForm.scalar(this, Double.POSITIVE_INFINITY)
            val sum1 = af3 + af1
            val sum2 = af1 + af3
            assertTrue(af3.max.isInfinite)
            assertTrue(sum1.min.isInfinite)
            assertEquals(Double.POSITIVE_INFINITY, sum2.min.toDouble())
            assertEquals(Double.POSITIVE_INFINITY, sum2.max.toDouble())
            assertEquals(sum1, sum2)
        }
    }

    /**
     * Any operation with Empty or (for Double: NaN) results in Empty.
     */
    @Test
    fun testAddNaN() {
        DDBuilder {
            val af1 = AffineForm.range(this, 1.0..2.0, "1")
            val af3 = AffineForm.scalar(this, Double.NaN)
            val sum1 = af3 + af1
            val sum2 = af1 + af3
            assertTrue(af3.isEmpty())
            assertTrue(sum1.isEmpty())
            assertTrue(sum2.isEmpty())
        }
    }

    /**
     * The min/max value of a new AF is set to the minimum of AA resp. IA range representation.
     * If IA and AA have intervals that are not overlapping, we set IA range to Empty Set representation
     */
    @Test
    fun testSpecificRanges(){
        DDBuilder {
            val terms = hashMapOf(1L to 0.5)

            // Non-overlapping case: Result is an Empty set.
            val empty = create(this, RealRange(0.0, 0.5), 1.5, 0.0, terms)
            assertTrue(empty.isEmpty())

            val restrict = create(this, RealRange(1.2, 1.8),1.5,0.0, terms)
            assertEquals(1.2, restrict.min.finiteValue, precision)
            assertEquals(1.8, restrict.max.finiteValue, precision)
        }
    }

    @Test
    fun testNegation() {
        DDBuilder {
            val af1 = AffineForm.range(this, 1.0 .. 2.0)
            val neg: AffineForm = -af1
            assertEquals(-1.5, neg.central, precision)
            assertEquals(-2.0 .. -1.0, neg, precision)
            assertEquals(0.5, neg.radius, precision)
        }
    }

    @Test
    fun testMultiplication() {
        DDBuilder{
            val terms = hashMapOf(1L to 2.0, 2L to 1.0)
            val lgr = create(this, RealRange.Reals, 10.0, 0.0, terms)
            val terms2 = hashMapOf(1L to -2.0, 3L to 1.0)
            val af3 = create(this, RealRange.Reals,10.0, 0.0, terms2)
            val mult: AffineForm = lgr * af3
            assertTrue(!mult.isScalar())
            assertEquals(100.0, mult.central, precision)
            assertEquals(71.0..129.0, mult, precision)

            // Check that FP roundoff for simple scalar is included
            val b1  = AffineForm.scalar(this, 10.0) * 100.0
            assertTrue(1000.0 in b1 )
        }
    }

    @Test
    fun testUnaryMinus() {
        DDBuilder {
            val v = AffineForm.range(this, 1.0..2.0, "1")
            val vm: AffineForm = negate(v)
            assertEquals(-2.0..-1.0, vm)
            assertEquals(0.5, vm.radius)
        }
    }

    @Test
    fun testAbs(){
        DDBuilder{
            val x= AffineForm.range(this, -2.0 .. 1.0, "1")
            val y= AffineForm.range(this, 0.5 .. 2.5, "2")
            val z1 = abs(x)
            val z2 = abs(y)
            assertEquals(0.0..2.0, z1, precision)
            assertEquals(-1.5, z1.xi[1]!!,precision)
            assertEquals(0.5..2.5, z2,precision)
            assertEquals(1.0, z2.xi[2]!!,precision)
        }
    }

    @Test
    fun testPowerOf2() {
        DDBuilder{
            val terms = hashMapOf(1L to 2.0, 2L to 1.0)
            val lgr = create(this, RealRange.Reals, 10.0, 0.0, terms)
            val scl = AffineForm.scalar(this, 1.0)
            val af1 = AffineForm.range(this, 1.0 .. 2.0, "1")

            // Test scalar
            val af3 = AffineForm.scalar(this, 3.5)
            val af4 = AffineForm.scalar(this, -1.0)
            val exp1 = power2(scl)
            val exp2 = power2(af3)
            val exp3 = power2(af4)
            assertEquals(2.0, exp1.central, 1.0)
            assertEquals(2.0.pow(3.5), exp2.central, 20.0)
            assertEquals(2.0.pow(-1.0), exp3.central, 10.0)

            // Test interval [1.0,2.0]
            val exp4 = power2(af1)
            //central and r value determined by printouts
            assertEquals(3.0, exp4.central, 20.0)
            assertEquals(2.0 .. 4.0, exp4, 2.0)
            //test [7,13]
            val exp5 = power2(lgr)
            //central and r value determined by printouts
            assertEquals(2.0.pow(7.0) .. 2.0.pow(13.0), exp5, 100.0)
        }
    }

    /*
    @Test
    fun testLogBase2Chebyshev() {
        DDBuilder{
            scheme = DDBuilder.ApproximationScheme.Chebyshev
            var terms = HashMap<Int, Double>()
            terms[1] = 1.0
            terms[2] = 1.0
            val lgr = create(this, RealRange.Reals, 6.0, 0.0, terms)

            val af1 = AffineForm.range(this, 1.0 .. 2.0, "1")

            terms = HashMap()
            terms[2] = 0.5
            val rst = create(this, RealRange(1.1, 1.9), 1.5, 0.0, terms)

            val log1 = log(af1, 2.0)
            //assertEquals(0.38, log1.central, 0.01)
            //assertTrue(0.3>= log1.r)
            assertEquals(0.0, log1.min, 0.01)
            assertEquals(1.0, log1.max, 0.01)
            val log2 = lgr.log(2.0)
            //assertEquals(2.28, log2.central, 0.01)
            //assertTrue(0.03>= log2.r)
            assertEquals(2.0, log2.min, 0.01)
            assertEquals(3.0, log2.max, 0.01)
            val log3 = rst.log(2.0)
            //assertEquals(0.39, log3.central, 0.01)
            //assertTrue(0.03>= log3.r )
            assertEquals(ln(1.1)/ln(2.0), log3.min, 0.01)
            assertEquals(ln(1.9)/ln(2.0), log3.max, 0.01)
        }
    }

    /**
     * ln of a range that includes 0 returns a range only with
     * no sensitivities. min is -inf, max can be computed.
     */
    @Test
    fun testLogCornerAtZero(){
        DDBuilder{
            val result = AffineForm(this, 0.0 .. 500.0).log()
            assertEquals(Double.NEGATIVE_INFINITY, result.min)
            assertEquals( ln(500.0), result.max, 0.001)
        }
    }

    @Test
    fun testSqrtRange() {
        DDBuilder {
            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = create(this, RealRange.Reals,10.0, 0.0, terms)

            val af1 = AffineForm(this, 1.0 .. 2.0, 1)

            terms = HashMap()
            terms[2] = 0.5
            val rst = create(this, RealRange(1.1, 1.9), 1.5, 0.0, terms)

            val sqrt1 = af1.sqrt()
            assertEquals(1.2071, sqrt1.central, 0.01)
            assertEquals(0.03, sqrt1.r, 0.01)
            assertEquals(1.0, sqrt1.min, 0.01)
            assertEquals(sqrt(2.0), sqrt1.max, 0.01)
            val sqrt2 = lgr.sqrt()
            assertEquals(3.13, sqrt2.central, 0.01)
            assertEquals(0.06, sqrt2.r, 0.01)
            assertEquals(sqrt(7.0), sqrt2.min, 0.01)
            assertEquals(sqrt(13.0), sqrt2.max, 0.01)
            val sqrt3 = rst.sqrt()
            assertEquals(1.21, sqrt3.central, 0.01)
            assertEquals(0.02, sqrt3.r, 0.01)
            assertEquals(sqrt(rst.min), sqrt3.min, 0.01)
            assertEquals(sqrt(rst.max), sqrt3.max, 0.01)
        }
    }

    @Test
    fun testSqrtChebyshev(){
        DDBuilder {
            scheme = DDBuilder.ApproximationScheme.MinRange

            val v9 = AffineForm(this, 3.0) * AffineForm(this, 3.0)
            val v16 = AffineForm(this, 4.0) * AffineForm(this, 4.0)
            val v1 = AffineForm(this, 1.0) * AffineForm(this, 1.0)
            val v16t1 = v16 * v1
            val v25 = v9 + v16t1
            val v5 = v25.sqrt()
            assertTrue(5.0 in v5)

            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = create(this, RealRange.Reals,10.0, 0.0, terms)

            val af1 = AffineForm(this, 1.0 .. 2.0, 1)

            terms = HashMap()
            terms[2] = 0.5
            val rst = create(this, RealRange(1.1, 1.9), 1.5, 0.0, terms)

            val sqrt1 = af1.sqrt()
            assertEquals(1.0, sqrt1.min, 0.01)
            assertEquals(sqrt(2.0), sqrt1.max, 0.01)
            val sqrt2 = lgr.sqrt()
            assertEquals(sqrt(7.0), sqrt2.min, 0.01)
            assertEquals(sqrt(13.0), sqrt2.max, 0.01)
            val sqrt3 = rst.sqrt()
            assertEquals(sqrt(rst.min), sqrt3.min, 0.01)
            assertEquals(sqrt(rst.max), sqrt3.max, 0.01)


            val af5 = AffineForm(this, 3.0).sqrt()
            assertEquals(sqrt(3.0), af5.min, 0.001)
            assertEquals(sqrt(3.0), af5.max, 0.001)

            val af6 = AffineForm(this, 0.0).sqrt()
            assertEquals(0.0, af6.central, precision)
            assertEquals(sqrt(0.0), af6.min, precision)
            assertEquals(sqrt(0.0), af6.max, precision)

            //val af7 = AffineForm(this, -2.0, -1.0, -1)
            //assertEquals(AFRealsNaN, af7)

            val af8 = AffineForm(this, 0.0 .. 1.0)
            assertEquals(sqrt(0.0), af8.min, precision)
            assertEquals(sqrt(1.0), af8.max, precision)

        }
    }

    @Test
    fun testSqrtScalar() {
        DDBuilder {
            val af1 = AffineForm(this, 3.0).sqrt()
            assertEquals(sqrt(3.0), af1.min, 0.001)
            assertEquals(sqrt(3.0), af1.max, 0.001)
        }
    }


    @Test
    fun testTimes(){
        DDBuilder{
            val af1= AffineForm(this, 1.0 .. 3.0, 1)
            val res = af1.times(af1)
            val expected = AffineForm(this, 1.0 .. 9.0, 1)
            assertEquals(expected.min, res.min, 0.0001)
            assertEquals(expected.max, res.max, 0.0001)
        }
    }

    /** Check that FP roundoff error is considered when computing with some large or non-representable FP values */
    @Test
    fun testTimesSafeInclusion() {
        DDBuilder {
            val af1 = AffineForm(this, 4.0)/AffineForm(this, 3.0)*AffineForm(this, PI)*AffineForm(this, 10000000.0)
            val af2 = af1 * AffineForm(this, 1.0)/AffineForm(this, 10000000.0) * AffineForm(this, 3.0) * AffineForm(this, 1.0)/AffineForm(this, PI)
            assertEquals( 4.0, af2.central, 0.000000001)
            assertTrue( 4.0 in af2)
        }
    }

    /** Inverse of range, including 1/0 and 1/(inf ... inf) */
    @Test fun testInv() {
        DDBuilder{

            val af1 = AffineForm(this, 1.0 .. 2.0, 1)

            // Around zero
            val af3 = AffineForm(this, -2.0 .. 2.0)
            val inv = af3.inv()
            assertTrue(inv.isReals())

            // Infinity should be preserved
            val inf = AffineForm(this, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            val inv2 = inf.inv()
            assertTrue(inv2.isReals())

            // Regular
            val inv3 = af1.inv()
            assertEquals(0.75, inv3.central, precision)
            assertEquals(0.5, inv3.min, precision)
            assertEquals(1.0, inv3.max, precision)
            assertEquals(0.125, inv3.r, precision)
            assertEquals(0.25, inv3.radius, precision)

            // IA shall override AA if tighter
            val i = AffineForm(this, 0.1 .. 5.0, 1).inv()
            assertEquals(1/5.0, i.min, 50*i.min.ulp)
            assertEquals(1/0.1, i.max, 50*i.max.ulp)
        }
    }

    @Test
    fun testInvChebyshev(){
        DDBuilder{
            this.scheme = DDBuilder.ApproximationScheme.Chebyshev

            val af1 = AffineForm(this, 1.0.. 2.0, 1)
            val zero = AffineForm(this, 0.0)
            val af6 = AffineForm(this, -4.0 .. -1.0)

            // Around zero
            val af3 = AffineForm(this, -2.0 .. 2.0)
            val inv = af3.inv()
            assertTrue(inv.isReals())

            // Reals -> Reals
            val reals = Reals.inv() as AADD.Leaf
            assertTrue(reals.value.isReals())

            // Regular
            val inv3 = af1.inv()
            assertEquals(0.5, inv3.min, precision)
            assertEquals(1.0, inv3.max, precision)

            // IA shall override AA if tighter
            val i = AffineForm(this, 0.1 .. 5.0, 1).inv()
            assertEquals(1/5.0, i.min, 50*i.min.ulp)
            assertEquals(1/0.1, i.max, 50*i.max.ulp)

            val inv4 = AffineForm(this, 0.0..1.0).inv()
            assertEquals(1.0, inv4.min, 0.0000001)
            assertEquals(Double.POSITIVE_INFINITY, inv4.max)

            val inv5 = zero.inv()
            assertEquals(AFEmpty, inv5)

            val inv6 = AffineForm(this, -1.0 .. 0.0).inv()
            assertEquals(Double.NEGATIVE_INFINITY, inv6.min)
            assertEquals(-1.0, inv6.max, 0.0000001)

            val inv7 = af6.inv()
            assertEquals(1/(-4.0), inv7.max, 50*i.max.ulp)
            assertEquals(1/(-1.0), inv7.min, 50*i.min.ulp)
        }
    }
    @Test
    fun testPowCornerAtZero(){
        DDBuilder{
            val res = AffineForm(this, 0.0 .. 500.0)

            val res2 = res.inv()
            assertEquals(1.0/500.0, res2.min, 0.000001)
            assertEquals(Double.POSITIVE_INFINITY, res2.max)

            val res3 = res.pow(AffineForm(this, 1.0 .. 2.0))
            assertEquals(0.0, res3.min, 0.000001)
            assertEquals(500.0*500.0, res3.max, 0.000001)

        }
    }

    @Test
    fun divTest() {
        DDBuilder {
            val a = AffineForm(this, 3.6)
            val b = AffineForm(this, 10.0)
            val c = b/a
            assertTrue( (10.0/3.6) in c)

            val a0 = AffineForm(this, 0.0 .. 1000.0)
            val divisor = AffineForm(this, 2.0)
            val res = a0 / divisor

            assertEquals(0.0, res.min, 0.0.ulp)
            assertEquals(500.0, res.max, 0.00000001)

            val a1 = AffineForm(this, 1000.0)/AffineForm(this, 3600.0)
            assertTrue(1000.0/3600.0 in a1)
            val aa = AffineForm(this, 1.0)/((AffineForm(this, 1000.0)/AffineForm(this, 3600.0)))
            assertTrue(3.6 in aa.min-aa.r .. aa.max+aa.r)

            // IA shall override AA if tighter
            val n = AffineForm(this, 2.0 .. 3.0, 1)
            val d = AffineForm(this, 2.0 .. 5.0, 2)
            val r = n / d
            assertEquals(n.min / d.max, r.min, 50 * r.min.ulp)
            assertEquals(n.max / d.min, r.max, 50 * r.max.ulp)

            // AA shall override IA if tighter
            val n2 = AffineForm(this, 2.0 .. 3.0, 1)
            val d2 = AffineForm(this, 2.0 .. 5.0, 1)
            val r2 = n2 / d2
            assertEquals(0.55, r2.min, 0.0000000001)
            assertEquals(1.2, r2.max, 0.0000000001)
        }
    }


    @Test
    fun sqrTest() {
        DDBuilder {
            val v9 = AffineForm(this, 3.0) * AffineForm(this, 3.0)
            val v16 = AffineForm(this, 4.0) * AffineForm(this, 4.0)
            val v1 = AffineForm(this, 1.0) * AffineForm(this, 1.0)
            val v16t1 = v16 * v1
            val v25 = v9 + v16t1
            val v5 = v25.sqrt()
            assertTrue(5.0 in v5)
        }
    }

    /**
     * Div by zero should return infinity or reals
     * Regular division is tested by inversion + multiplication
     **/
    @Test
    fun divTestDivBy0() {
        DDBuilder {
            // Division by AFReals gives AFRealsNaN
            val a = AFReals
            val b = AFReals
            val c = a/b
            assertTrue(c.isReals())

            // Division by AFReals of 0.0 gives AFRealsNaN
            val a2 = AffineForm(this, 0.0)
            val c2 = a2/b
            assertTrue(!c2.isFinite())

            val af1 = AffineForm(this, 1.0 .. 2.0)
            val zero = AffineForm(this, 0.0)
            val div = af1 / zero
            assertTrue(div.isEmpty())

            val rangeZero = AffineForm(this, -1.0..1.0)
            val divR = af1 / rangeZero
            assertTrue(divR.isReals())
        }
    }


    @Test
    fun testEquality() {
        DDBuilder{

            val af1 = AffineForm(this, 1.0 .. 2.0, 1)
            val af2 = AffineForm(this, 1.0 .. 2.0, 2)

            val af3 = AffineForm(this, 1.0 .. 2.0, 2)
            val af4 = af2.clone()
            assertNotEquals(af1, af2)
            assertEquals(af3, af2)
            assertEquals(af4, af2)
        }
    }

    @Test
    fun testAroundZero() {
        DDBuilder{
            val af3 = AffineForm(this, -1.0 .. 2.0)
            val af4 = AffineForm(this, 0.0 .. 2.0)
            val af5 = AffineForm(this, 0.0000001 .. 2.0)
            val af6 = AffineForm(this, -2.0 .. 0.0)
            val af7 = AffineForm(this, -2.0 .. -0.0000001)
            assertFalse(af3.isWeaklyPositive)
            assertFalse(af3.isWeaklyNegative)
            assertTrue(af4.isWeaklyPositive)
            assertFalse(af4.isStrictlyPositive)
            assertFalse(af4.isWeaklyNegative)
            assertTrue(af5.isWeaklyPositive)
            assertTrue(af5.isStrictlyPositive)
            assertFalse(af5.isWeaklyNegative)
            assertTrue(af6.isWeaklyNegative)
            assertFalse(af6.isStrictlyNegative)
            assertFalse(af6.isWeaklyPositive)
            assertTrue(af7.isWeaklyNegative)
            assertTrue(af7.isStrictlyNegative)
            assertFalse(af7.isWeaklyPositive)
        }
    }

    /**
     * Test function for the relu function over affine forms. The input in this test is a range that has negative
     * as well as positive values. Thus the result should be only the range that has the positive values. This is realised
     * through an AADD that should have in one leaf a 0 range and in the other the orginal range but constrained to those
     * values that are positive. This should always be the expected outcome of this test.
     * */
    @Test
    fun reluTestAffineForm()
    {
        DDBuilder {
            val af = AffineForm(this,-1.0..1.0)
            val relu_res = relu(af,this)
            relu_res.getRange()
            assertEquals(1.0, relu_res.max)
            assertTrue { (relu_res.min <= 0.0 + 0.001) && ( 0.0 - 0.001 <= relu_res.min) }
        }
    }

    @Test
    fun isSimilarTest() {
        DDBuilder {
            val a = AffineForm(this, 1.0 .. 2.0, 1)
            var b = AffineForm(this, 1.0 .. 2.0, 1)
            assertTrue(a.isSimilar(b, 0.000001))
            b = AffineForm(this, 1.0 .. 2.0, 2)
            assertFalse(a.isSimilar(b, 0.000001))
        }
    }

    @Test
    fun joinTest() {
        DDBuilder{
            val a = AffineForm(this, 1.0 .. 2.0, 1)
            var b = AffineForm(this, 2.0.. 3.0, 1)
            assertEquals(2.0, a.join(b).central)
            b = AffineForm(this, 2.0 .. 3.0, 2)
            assertEquals(2.0, a.join(b).central)
        }
    }

    /**
     * If there is an infinite term (e.g., due to overflow), we drop the AA part.
     * But we can continue with IA.
     */
    @Test
    fun testInfiniteTerm() {
        DDBuilder{
            val terms = hashMapOf(1 to Double.POSITIVE_INFINITY)
            val af3 = create(this, 2.0 .. 3.0, 2.5, 0.0, terms)
            assertEquals(2.0 .. 3.0, af3.min .. af3.max)
            // assertEquals(Double.NEGATIVE_INFINITY, af3.min )
            // assertEquals( Double.POSITIVE_INFINITY, af3.max)
        }
    }

    /**
     * if one of the noise variables is NaN, the AF is NaN as a whole.
     */
    @Test
    fun testNaNTerm() {
        DDBuilder{
            val range = create(this, 0.0 .. 1.0, 0.0, 0.0, hashMapOf(1 to Double.NaN))
            assertTrue(range.xi.isEmpty())
            assertEquals(0.0, range.min)
            assertEquals(1.0, range.max)
        }
    }


    /**
     * Testing:
     * ceiling fxn. for AFs
     */
    @Test
    fun testCeilingFxnAF1() {
        DDBuilder{
            val terms = HashMap<Int, Double>()
            val x = create(this, RealRange(1.47, 1.49), 1.48, 0.01, terms)

            val y = x.ceil()
            //println("x = [" + x.min + ", " + x.max + "]" )
            //println("y = [" + y.min + ", " + y.max + "]" )
            assertEquals(2.0, y.min, precision)
            assertEquals(2.0, y.max, precision)
            val yL : Long = x.ceilAsLong()
            //println("yL = $yL")
            assertEquals(2, yL)
            val yIR = x.ceilToIntRange()
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]" )
            assertEquals(2, yIR.min)
            assertEquals(2, yIR.max)
        }
    }

    /**
     * Testing:
     * ceiling fxn. for AFs
     */
    @Test
    fun testInvCeilFxnAF1() {
        DDBuilder{
            val terms = HashMap<Int, Double>()
            val x = create(this, RealRange(1.4, 1.6), 1.5, 0.1, terms)
            val y = x.ceil()
            val z = y.invCeil()

            //println("x = [" + x.min + ", " + x.max + "]" )
            //println("y = x.ceil() = [" + y.min + ", " + y.max + "]" )
            //println("z = y.invCeil() = [" + z.min + ", " + z.max + "]" )

            assertEquals(2.0, y.min, precision)
            assertEquals(2.0, y.max, precision)
            assertEquals(1.0, z.min, precision)
            assertEquals(2.0, z.max, precision)
        }
    }

    /**
     * Testing:
     * ceiling fxn. for AFs
     */
    @Test
    fun testFloorFxnAF1() {
        DDBuilder{
            val x = create(this, RealRange(1.51, 1.53), 1.52, 0.01)

            val y = x.floor()
            //println("x = [" + x.min + ", " + x.max + "]" )
            //println("y = [" + y.min + ", " + y.max + "]" )
            assertEquals(1.0, y.min, precision)
            assertEquals(1.0, y.max, precision)
            val yL : Long = x.floorAsLong()
            //println("yL = $yL")
            assertEquals(1, yL)
            val yIR = x.floorToIntRange()
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]" )
            assertEquals(1, yIR.min)
            assertEquals(1, yIR.max)
        }
    }

    /**
     * Testing:
     * ceiling fxn. for AFs
     */
    @Test
    fun testInvFloorFxnAF1() {
        DDBuilder{
            val terms = HashMap<Int, Double>()
            val x = create(this, RealRange(1.4, 1.6), 1.5, 0.1, terms)
            val y = x.floor()
            val z = y.invFloor()

            //println("x = [" + x.min + ", " + x.max + "]" )
            //println("y = x.floor() = [" + y.min + ", " + y.max + "]" )
            //println("z = y.invFloor() = [" + z.min + ", " + z.max + "]" )

            assertEquals(1.0, y.min, precision)
            assertEquals(1.0, y.max, precision)
            assertEquals(1.0, z.min, precision)
            assertEquals(2.0, z.max, precision)
        }
    }

    @Test
    fun testQuadratic1(){
        DDBuilder {

            val x = AffineForm(this, -20.0 .. 20.0, 1)
//        val x = range(0.0..20.0)

            val a = x * x
            assertEquals(-400.0, a.min,precision)
            assertEquals(400.0, a.max,precision)
            // will x1 == 0.0 because the central value therefore the multiplication plane is zero
            assertEquals(0.0,a.xi[1]!!, precision)
        }
    }

    @Test
    fun testPowEvalDownEmulate() {
        DDBuilder {
            val tol = 0.01
            val x = AffineForm(this, 0.1 .. 2.0)
            val z1 = x.pow(AffineForm(this, 1.0 .. 2.0))
            val z2 = z1.pow(AffineForm(this, 0.5 ..1.0))
            val z3 = z2.pow(AffineForm(this, 0.5 .. 1.0))
            val z4 = z3.pow(AffineForm(this, 0.5 .. 1.0))
            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            //assertEquals(1.95, z.central, tol)
            assertEquals(0.01, z4.min, tol)
            assertEquals(4.0, z4.max, tol)
        }
    }

    @Test
    fun testPowEvalDownEmulate2() {
        DDBuilder {
            val tol = 0.01
            val nts = HashMap<Int,Double>()
            nts[5]= 0.02561483618895184
            nts[3]=  0.9499999999999994
            val x = leaf(create(this, RealRange(0.009999999999999999981,4.0000000000000003),2.0050000000000017,1.0193851638110518,nts))
            val y = real(1.0).div(this.leaf(AffineForm(this, 1.0..2.0, 5)))
            val z1 = x.value.pow((y as AADD.Leaf).value)

            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            //assertEquals(1.95, z.central, tol)
            assertEquals(0.01, z1.min, tol)
            assertEquals(4.0, z1.max, tol)
        }
    }

    @Test
    fun testPowAffineInput() {
        DDBuilder{
            val tol = 0.01
            /*var x = AffineForm(this, 2.0)
            var y = 2.0
            var z = x.pow(y)

            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            assertEquals(4.0, z.central, tol)
            assertEquals(4.0, z.min, tol)
            assertEquals(4.0, z.max, tol)

            x = AffineForm(this, 0.1,2.0,-1)
            z = x.pow(AffineForm(this, 1.0,2.0,-1))

            assertEquals(0.01, z.min, tol)
            assertEquals(4.0, z.max, tol)*/

            var x = AffineForm(this, 0.1 .. 2.0)
            val z1 = x.pow(AffineForm(this, 1.0 .. 2.0))
            val z2 = z1.pow(AffineForm(this, 0.5 .. 1.0))
            val z3 = z2.pow(AffineForm(this, 0.5 .. 1.0))
            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            //assertEquals(1.95, z.central, tol)
            assertEquals(0.01, z3.min, tol)
            assertEquals(4.0, z3.max, tol)


            x = AffineForm(this, 3.0)
            var y = 3.0
            var z = x.pow(y)

            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            assertEquals(27.0, z.central, tol)
            assertEquals(27.0, z.min, tol)
            assertEquals(27.0, z.max, tol)

            x = AffineForm(this, 3.0)
            y = 0.0
            z = x.pow(y)

            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            assertEquals(1.0, z.central, tol)
            assertEquals(1.0, z.min, tol)
            assertEquals(1.0, z.max, tol)

            x = AffineForm(this, 3.0)
            y = 1.0
            z = x.pow(y)

            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            assertEquals(3.0, z.central, tol)
            assertEquals(3.0, z.min, tol)
            assertEquals(3.0, z.max, tol)
        }
    }

    @Test fun testPowRange() {
        DDBuilder {
            val a = real(1.5 .. 3.5)
            val b = real(2.5 .. 4.5)
            val result = a.pow(b)
            assertEquals(1.5.pow(2.5), result.getRange().min, 0.000001)
            assertEquals(3.5.pow(4.5), result.getRange().max, 0.000001)
        }
    }

    @Test fun testPowRangeLessThanOne() {
        DDBuilder {
            val a = real(0.1..2.0)
            val b = real(1.0 .. 2.0)
            val result = a.pow(b)
            assertEquals(0.1.pow(2.0), result.getRange().min)
            assertEquals(2.0.pow(2.0), result.getRange().max)
        }
    }


    @Test
    fun testPowerDoubleChebyshev(){
        DDBuilder{
            this.scheme = DDBuilder.ApproximationScheme.Chebyshev

            //TODO: tests passed even though there is a division by zero for scalar affine forms
            val tol = 0.01
            var x = AffineForm(this, 2.0)
            var y = 2.0
            var z = x.pow(y)

            assertEquals(4.0, z.central, tol)
            assertEquals(4.0, z.min, tol)
            assertEquals(4.0, z.max, tol)

            x = AffineForm(this, 3.0)
            y = 3.0
            z = x.pow(y)

            assertEquals(27.0, z.central, tol)
            assertEquals(27.0, z.min, tol)
            assertEquals(27.0, z.max, tol)

            x = AffineForm(this, 3.0)
            y = 0.0
            z = x.pow(y)

            assertEquals(1.0, z.central, tol)
            assertEquals(1.0, z.min, tol)
            assertEquals(1.0, z.max, tol)

            x = AffineForm(this, 3.0)
            y = 1.0
            z = x.pow(y)


            assertEquals(3.0, z.central, tol)
            assertEquals(3.0, z.min, tol)
            assertEquals(3.0, z.max, tol)

            //TODO: fix problems with division by zero for Exponent = 0 and Exponent = 1
/*
            x = AffineForm(this, 1.0,2.0,-1)
            y = 1.0
            z = x.pow(y)

            assertEquals(1.0, z.min, tol)
            assertEquals(2.0, z.max, tol)


            x = AffineForm(this, 1.0,2.0,-1)
            y = 0.0
            z = x.pow(y)

            assertEquals(1.0, z.min, tol)
            assertEquals(1.0, z.max, tol)
 */
            x = AffineForm(this, 1.0..2.0)
            y = 2.0
            z = x.pow(y)

            assertEquals(1.0, z.min, tol)
            assertEquals(4.0, z.max, tol)

        }
    }



    //TODO: Test Chebyshev for Log
    /*
    @Test
    fun testLogChebyshev(){

    }
    @Test
    fun testLogDoubleChebyshev(){

    }
    */

     */
}
