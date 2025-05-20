package values

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.dao.toDAO
import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.Range
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class AffineFormTestWithoutR {

    private val precision = 0.000001

    /*
    private val af1 = AF(1.0, 2.0, 1)
    private val af2 = AF(1.0, 2.0, 2)
    private var scl: AffineForm? = null
    private var lgr: AffineForm? = null
    private var rst: AffineForm? = null
    private var gaussian1 :AffineForm
    private var gaussian2 :AffineForm
    private var mixedTermDNS :AffineForm*/

    /*init {

        // Larger value
        var terms = HashMap<Int, Double>()
        terms[1] = 2.0
        terms[2] = 1.0
        lgr = AF(Range.Reals,10.0, 0.0, terms)

        // Scalar form
        scl = AF(1.0)

        // Restricted range through manual min/max values
        terms = HashMap()
        terms[2] = 0.5
        rst = this.AF(Range(1.1, 1.9), 1.5, 0.0, terms)

        val terms2 = HashMap <Int,Double>()
        terms2[5] = 0.5

        // define an Affine From with gaussian Terms
        val termsGaussian = HashMap<String,Pair<AffineForm,Triple<Double,Double,Double>>>()
        termsGaussian["1"] = Pair(AF(1.0), Triple(0.0,0.5,0.0))
        termsGaussian["2"] = Pair(AF(1.0),Triple(1.0,0.5,0.0))
        gaussian1 = AF(Range(1.0,2.0), 1.5, 0.0 ,terms2)
        gaussian1.setXiGaussian(termsGaussian)

        val terms3 = HashMap <Int,Double>()
        terms3[6] = 1.0

        // define an Affine From with gaussian Terms
        val termsGaussian2 = HashMap<String,Pair<AffineForm,Triple<Double,Double,Double>>>()
        termsGaussian2["1"] = Pair(AF(1.0), Triple(0.0,0.5,0.0))
        termsGaussian2["3"] = Pair(AF(0.5),Triple(0.5,0.5,0.0))
        gaussian2 = AF(Range(0.0,2.0), 1.0, 0.0 , terms3)
        gaussian2.setXiGaussian(termsGaussian2)


        val termDNStmp = HashMap<String,Pair<AffineForm,Triple<Double,Double,Double>>>()
        termDNStmp["2"] = Pair(AF(1.5), Triple(1.0,0.5,0.0))
        val termAF = AF(0.5)
        termAF.setXiGaussian(termDNStmp)

        val termsGaussian3 = HashMap<String,Pair<AffineForm,Triple<Double,Double,Double>>>()
        termsGaussian3["1"] = Pair(AF(2.0), Triple(0.0,0.5,0.0))
        termsGaussian3["3"] = Pair(termAF,Triple(0.5,0.5,0.0))
        mixedTermDNS = AF( 1.0)
        mixedTermDNS.setXiGaussian(termsGaussian3)
    }*/

    /** A real is the range +-MAX_VALUE. It is identified by isReal() */
    @Test fun realTest() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val real = AFReals
            assertEquals(Double.NEGATIVE_INFINITY, real.min)
            assertEquals(Double.POSITIVE_INFINITY, real.max)
            assertTrue(real.isReals())
        }
    }

    @Test
    fun overloadedOperatorsTest() {
        DDBuilder {
             config.noiseSymbolsFlag = true
            val a = AffineForm(this, 1.0..2.0, "a")
            val b = AffineForm(this, 2.0..3.0, "a")
            var y = a - b
            assertEquals(0.0, y.radius, 0.000001)

            y = a*b
            assertEquals(2.0, y.min, 0.000001)
            assertEquals(6.0, y.max, 0.000001)
        }
    }


    @Test
    fun testAddForms() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 1.0 .. 2.0, "1")
            val af2 = AffineForm(this, 1.0 .. 2.0, "2")

            val sum = af1 + af2
            var termValueSum = 0.0
            for (v in sum.xi.values) {
                termValueSum += v
            }
            assertEquals(3.0, sum.central, precision)
            assertEquals(2.0, sum.min, precision)
            assertEquals(4.0, sum.max, precision)
            assertEquals(0.0, sum.r, precision)
            assertEquals(1.0, sum.radius, precision)
            assertEquals(3, sum.xi.keys.size)
            assertTrue(sum.xi.keys.contains(1))
            assertTrue(sum.xi.keys.contains(2))
            assertEquals(termValueSum, sum.radius, precision)
        }
    }

    @Test
    fun testAddFormsCustomRange() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 1.0..2.0, 1)
            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0

            // Restricted range through manual min/max values
            terms = HashMap()
            terms[2] = 0.5
            val rst = AffineForm(this, Range(1.1, 1.9), 1.5, 0.0, terms)

            val sum = af1 + rst
            assertEquals(3.0, sum.central, precision)
            assertEquals(2.1, sum.min, precision)
            assertEquals(3.9, sum.max, precision)
            assertEquals(0.0, sum.r, precision)
            // Doesn't change with artificial range!
            assertEquals(1.0, sum.radius, precision)
        }
    }


    /** Adding something to infinity results in infinity */
    @Test fun testAddInfinite() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af3 = AffineForm(this, Double.POSITIVE_INFINITY)
            val sum1 = af3 + af1
            val sum2 = af1 + af3
            assertTrue(af3.maxIsInf)
            assertTrue(sum1.minIsInf)
            assertTrue(sum2.min == Double.POSITIVE_INFINITY)
            assertTrue(sum2.max == Double.POSITIVE_INFINITY)
            assertEquals(sum1, sum2)
        }
    }

    @Test
    fun testAddNaN() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af3 = AffineForm(this, Double.NaN)
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
    fun testUnproperRanges(){
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val terms = HashMap<Int,Double>()
            terms[1]=0.5

            // Non-overlapping case: Result is an Empty set.
            val empty = AffineForm(this, Range(0.0, 0.5), 1.5, 0.0, terms)
            assertTrue(empty.isEmpty())

            val restrict = AffineForm(this, Range(1.2,1.8),1.5,0.0, terms)
            assertEquals(1.2, restrict.min, precision)
            assertEquals(1.8, restrict.max, precision)
        }
    }

    @Test
    fun testNegation() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 1.0..2.0, 1)
            val neg: AffineForm = -af1
            assertEquals(-1.5, neg.central, precision)
            assertEquals(-2.0, neg.min, precision)
            assertEquals(-1.0, neg.max, precision)
            assertEquals(0.0, neg.r, precision)
            assertEquals(0.5, neg.radius, precision)
        }
    }

    @Test
    fun testMultiplication() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true

            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val terms2 = hashMapOf(1 to -2.0, 3 to 1.0)
            val af3 = AffineForm(this, Range.Reals,10.0, 0.0, terms2)
            val mult: AffineForm = lgr.times(af3)
            assertTrue(mult.isRange())
            assertEquals(100.0, mult.central, precision)
            assertEquals(71.0, mult.min, precision)
            assertEquals(129.0, mult.max, precision)
            assertEquals(0.0, mult.r, precision)

            // Check that FP roundoff for simple scalar is included
            val b1  = AffineForm(this, 10.0) * 100.0
            assertTrue(1000.0 in b1 )
        }
    }

    @Test
    fun testExp() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true

            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val scl = AffineForm(this, 1.0)

            val af1 = AffineForm(this, 1.0..2.0, 1)

            // Test scalar
            val af3 = AffineForm(this, 3.5)
            val af4 = AffineForm(this, -1.0)
            val exp1 = scl.exp()
            val exp2 = af3.exp()
            val exp3 = af4.exp()
            assertEquals(E, exp1.central, precision)
            assertEquals(exp(3.5), exp2.central, precision)
            assertEquals(exp(-1.0), exp3.central, precision)

            // Test interval
            val exp4 = af1.exp()
            assertEquals(5.06, exp4.central, 0.01)
            assertEquals(0.0, exp4.r, 0.01)
            assertEquals(2.72, exp4.min, 0.01)
            assertEquals(7.39, exp4.max, 0.01)
            val exp5 = lgr.exp()
            assertEquals(221755.0, exp5.central, 1.0)
            assertEquals(0.0, exp5.r, 1.0)
            assertEquals(1097.0, exp5.min, 1.0)
            assertEquals(442413.0, exp5.max, 1.0)
        }
    }

    @Test
    fun testPowerOf2() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true

            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val scl = AffineForm(this, 1.0)

            val af1 = AffineForm(this, 1.0..2.0, 1)

            // Test scalar
            val af3 = AffineForm(this, 3.5)
            val af4 = AffineForm(this, -1.0)
            val exp1 = scl.power2()
            val exp2 = af3.power2()
            val exp3 = af4.power2()
            assertEquals(2.0, exp1.central, precision)
            assertEquals(2.0.pow(3.5), exp2.central, precision)
            assertEquals(2.0.pow(-1.0), exp3.central, precision)

            // Test interval [1.0,2.0]
            val exp4 = af1.power2()
            //central and r value determined by printouts
            assertEquals(3.0, exp4.central, 0.01)
            assertEquals(0.0, exp4.r, 0.01)
            assertEquals(2.0, exp4.min, 0.01)
            assertEquals(4.0, exp4.max, 0.01)
            //test [7,13]
            val exp5 = lgr.power2()
            //central and r value determined by printouts
            assertEquals(4160.0, exp5.central, 1.0)
            assertEquals(0.0, exp5.r, 1.0)
            assertEquals(2.0.pow(7.0), exp5.min, 1.0)
            assertEquals(2.0.pow(13.0), exp5.max, 1.0)
        }
    }

    @Test
    fun testLog() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            scheme=DDBuilder.ApproximationScheme.Chebyshev
            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val af1 = AffineForm(this, 1.0..2.0, 1)

            terms = HashMap()
            terms[2] = 0.5
            val rst = AffineForm(this, Range(1.1, 1.9), 1.5, 0.0, terms)

            val log1 = af1.log()
            assertEquals(0.38, log1.central, 0.01)
            assertEquals(0.0, log1.r, 0.01)
            assertEquals(0.0, log1.min, 0.01)
            assertEquals(ln(af1.max), log1.max, 0.01)
            val log2 = lgr.log()
            assertEquals(2.28, log2.central, 0.01)
            assertEquals(0.0, log2.r, 0.01)
            assertEquals(ln(lgr.min), log2.min, 0.01)
            assertEquals(ln(lgr.max), log2.max, 0.01)
            val log3 = rst.log()
            assertEquals(0.39, log3.central, 0.01)
            assertEquals(0.0, log3.r, 0.01)
            assertEquals(ln(rst.min), log3.min, 0.01)
            assertEquals(ln(rst.max), log3.max, 0.01)
        }
    }

    @Test
    fun testSqrtRange() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true

            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val af1 = AffineForm(this, 1.0..2.0, 1)

            terms = HashMap()
            terms[2] = 0.5
            val rst = AffineForm(this, Range(1.1, 1.9), 1.5, 0.0, terms)

            val sqrt1 = af1.sqrt()
            assertEquals(1.2071, sqrt1.central, 0.01)
            assertEquals(0.0, sqrt1.r, 0.01)
            assertEquals(1.0, sqrt1.min, 0.01)
            assertEquals(sqrt(2.0), sqrt1.max, 0.01)
            val sqrt2 = lgr.sqrt()
            assertEquals(3.13, sqrt2.central, 0.01)
            assertEquals(0.0, sqrt2.r, 0.01)
            assertEquals(sqrt(7.0), sqrt2.min, 0.01)
            assertEquals(sqrt(13.0), sqrt2.max, 0.01)
            val sqrt3 = rst.sqrt()
            assertEquals(1.21, sqrt3.central, 0.01)
            assertEquals(0.0, sqrt3.r, 0.01)
            assertEquals(sqrt(rst.min), sqrt3.min, 0.01)
            assertEquals(sqrt(rst.max), sqrt3.max, 0.01)
        }
    }

    @Test
    fun testSqrtScalar() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 3.0).sqrt()
            assertEquals(sqrt(3.0), af1.min, 0.001)
            assertEquals(sqrt(3.0), af1.max, 0.001)
        }
    }


    @Test
    fun testTimes(){
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val af1= AffineForm(this, 1.0..3.0,1)

            val res = af1.times(af1)
            val expected = AffineForm(this, 1.0..9.0,1)

            //print("structure " +res.xi.get(1)+ " error: "+res.r+"\n")

            assertEquals(expected.min, res.min, 0.0001)
            assertEquals(expected.max, res.max, 0.0001)
        }
    }

    /** Check that FP roundoff error is considered when computing with some large or non-representable FP values */
    @Test
    fun testTimesSafeInclusion() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 4.0)/AffineForm(this, 3.0)*AffineForm(this, PI)*AffineForm(this, 10000000.0)
            val af2 = af1 * AffineForm(this, 1.0)/AffineForm(this, 10000000.0) * AffineForm(this, 3.0) * AffineForm(this, 1.0)/AffineForm(this, PI)
            assertEquals( 4.0, af2.central, 0.000000001)
            assertTrue( 4.0 in af2)
        }
    }

    /** Inverse of range, including 1/0 and 1/(inf ... inf) */
    @Test fun testInv() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)

            // Around zero
            val af3 = AffineForm(this, -2.0..2.0)
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
            assertEquals(0.0, inv3.r, precision)
            assertEquals(0.25, inv3.radius, precision)

            // IA shall override AA if tighter
            val i = AffineForm(this, 0.1.. 5.0, 1).inv()
            assertEquals(1/5.0, i.min, 50*i.min.ulp)
            assertEquals(1/0.1, i.max, 50*i.max.ulp)
        }
    }

    @Test
    fun divTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            val a = AffineForm(this, 3.6)
            val b = AffineForm(this, 10.0)
            val c = b/a
            // toStringVerbose=true
            // //println(c)
            assertTrue( (10.0/3.6) in c)

            val a1 = AffineForm(this, 1000.0)/AffineForm(this, 3600.0)
            assertTrue(1000.0/3600.0 in a1)
            val aa = AffineForm(this, 1.0)/((AffineForm(this, 1000.0)/AffineForm(this, 3600.0)))
            assertTrue(3.6 in aa.min-aa.r .. aa.max+aa.r)
            //println(aa)

            // IA shall override AA if tighter
            val n = AffineForm(this, 2.0..3.0, 1)
            val d = AffineForm(this, 2.0..5.0, 2)
            val r = n / d
            assertEquals(n.min / d.max, r.min, 50 * r.min.ulp)
            assertEquals(n.max / d.min, r.max, 50 * r.max.ulp)

            // AA shall override IA if tighter
            val n2 = AffineForm(this, 2.0..3.0, 1)
            val d2 = AffineForm(this, 2.0..5.0, 1)
            val r2 = n2 / d2
            assertEquals(0.55, r2.min, 0.0000000001)
            assertEquals(1.2, r2.max, 0.0000000001)
        }
    }


    @Test
    fun sqrTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
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
            this.config.noiseSymbolsFlag = true
            // Division by AFReals gives AFRealsNaN
            val a = AFReals
            val b = AFReals
            val c = a/b
            assertTrue(!c.isFinite())

            // Division by AFReals of 0.0 gives AFRealsNaN
            val a2 = AffineForm(this, 0.0 )
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
    fun testCreation() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)
            val scl = AffineForm(this, 1.0)

            assertEquals(af1.central, af2.central, precision)
            assertEquals(af1.min, af2.min, precision)
            assertEquals(af1.max, af2.max, precision)
            assertEquals(af1.r, af2.r, precision)
            assertEquals(af1.radius, af2.radius, precision)
            assertEquals(1.5, af2.central, precision)
            assertEquals(1.0, af2.min, precision)
            assertEquals(2.0, af2.max, precision)
            assertEquals(0.0, af2.r, precision)
            assertEquals(0.5, af2.radius, precision)
            assertEquals(1.0, scl.central, precision)
            assertEquals(1.0, scl.min, precision)
            assertEquals(1.0, scl.max, precision)
            assertEquals(0.0, scl.radius, precision)
        }
    }

    @Test
    fun testEquality() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)

            val af3 = AffineForm(this, 1.0..2.0, 2)
            val af4 = af2.clone()
            assertFalse(af1 == af2)
            assertTrue(af3 == af2)
            assertTrue(af4 == af2)
        }
    }

    @Test
    fun testAroundZero() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val af3 = AffineForm(this, -1.0 .. 2.0)
            val af4 = AffineForm(this, 0.0 .. 2.0)
            val af5 = AffineForm(this, 0.0000001 .. 2.0)
            val af6 = AffineForm(this, -2.0..0.0)
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


    @Test
    fun isSimilarTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            val a = AffineForm(this, 1.0..2.0, 1)
            var b = AffineForm(this, 1.0..2.0, 1)
            assertTrue(a.isSimilar(b, 0.000001))
            b = AffineForm(this, 1.0..2.0, 2)
            assertFalse(a.isSimilar(b, 0.000001))
        }
    }

    @Test
    fun joinTest() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val a = AffineForm(this, 1.0..2.0, 1)
            var b = AffineForm(this, 2.0..3.0, 1)
            assertEquals(2.0, a.join(b).central)
            b = AffineForm(this, 2.0..3.0, 2)
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
            this.config.noiseSymbolsFlag = true
            val terms = hashMapOf(1 to Double.POSITIVE_INFINITY)
            val af3 = AffineForm(this, Range(2.0, 3.0), 2.5, 0.0, terms)
            //assertEquals(Range(2.0, 3.0), Range(af3.min .. af3.max))
            assertEquals(2.0, af3.min)
            assertEquals(3.0, af3.max)
        }
    }

    /**
     * if one of the noise variables is NaN, the AF is NaN as a whole.
     */
    @Test
    fun testNaNTerm() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val range = AffineForm(this, Range(0.0, 1.0), 0.0, 0.0, hashMapOf(1 to Double.NaN))
            assertTrue(range.xi.isEmpty())
            assertEquals(0.0, range.min)
            assertEquals(1.0, range.max)
        }
    }

    @Test
    fun toJsonTest() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val a = AffineForm(this, 0.0..2.0, 1)
            val json = a.toDAO().toJson()
        }
    }

    /**
     * Testing:
     * ceiling fxn. for AFs
     */
    @Test
    fun testCeilingFxnAF1() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val terms = HashMap<Int, Double>()
            val x = AffineForm(this, Range(1.47, 1.49), 1.48, 0.01, terms)

            val y = x.ceil()

            assertEquals(2.0, y.min, precision)
            assertEquals(2.0, y.max, precision)
            val yL : Long = x.ceilAsLong()
            //println("yL = $yL")
            assertEquals(2, yL)
            val yIR = x.ceiltoIntRange()
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
            this.config.noiseSymbolsFlag = true
            val terms = HashMap<Int, Double>()
            val x = AffineForm(this, Range(1.4, 1.6), 1.5, 0.1, terms)
            val y = x.ceil()
            val z = y.invCeil()
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
            this.config.noiseSymbolsFlag = true
            val terms = HashMap<Int, Double>()
            val x = AffineForm(this, Range(1.51, 1.53), 1.52, 0.01, terms)

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
            this.config.noiseSymbolsFlag = true
            val terms = HashMap<Int, Double>()
            val x = AffineForm(this, Range(1.4, 1.6), 1.5, 0.1, terms)
            val y = x.floor()
            val z = y.invFloor()

            assertEquals(1.0, y.min, precision)
            assertEquals(1.0, y.max, precision)
            assertEquals(1.0, z.min, precision)
            assertEquals(2.0, z.max, precision)
        }
    }

    @Test
    fun testPow() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            val tol = 0.01
            var x = AffineForm(this, 2.0)
            var y = 2.0
            var z = x.pow(y)

            assertEquals(4.0, z.central, tol)
            assertEquals(4.0, z.min, tol)
            assertEquals(4.0, z.max, tol)

            y = 3.0
            z = x.pow(y)

            assertEquals(8.0, z.central, tol)
            assertEquals(8.0, z.min, tol)
            assertEquals(8.0, z.max, tol)

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
        }
    }
}
