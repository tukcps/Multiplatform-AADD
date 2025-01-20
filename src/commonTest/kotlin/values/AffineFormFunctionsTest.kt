package values

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.DDInternalError
import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.Range
import com.github.tukcps.aadd.values.log
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class AffineFormFunctionsTest {

    private val precision = 10E-6

    /**
     * The affine forms are enclosed in an IA result.
     * We check that it is computed correctly using IA.
     */
    @Test
    fun expTestIA() {
        DDBuilder {
            val af = real(1.0..5.0)
            assertEquals(exp(1.0), af.exp().getRange().min, 0.0000001)
            assertEquals(exp(5.0), af.exp().getRange().max, 0.0000001)
        }
    }

    /**
     * The affine forms are enclosed in an IA result.
     * We check that it is computed correctly using IA.
     */
    @Test
    fun sqrtTestIA() {
        DDBuilder {
            val af = real(1.0..5.0)
            assertEquals(sqrt(1.0), af.sqrt().getRange().min, 0.0000001)
            assertEquals(sqrt(5.0), af.sqrt().getRange().max, 0.0000001)
        }
    }

    /**
     * The affine forms are enclosed in an IA result.
     * We check that it is computed correctly using IA.
     */
    @Test
    fun logTestIA() {
        DDBuilder {
            val af = real(1.0..5.0)
            assertEquals(ln(1.0), af.log().min, 0.0000001)
            assertEquals(ln(5.0), af.log().max, 0.0000001)
        }
    }

    /**
     * The affine forms are enclosed in an IA result.
     * We check that it is computed correctly using IA.
     */
    @Test
    fun powTestAF() {
        DDBuilder {
            val af = real(1.0..5.0)
            assertEquals(exp(1.0), af.exp().min, 0.0000001)
            assertEquals(exp(5.0), af.exp().max, 0.0000001)
        }
    }

    /**
     * Tests: pow
     */
    @Test
    fun testPowFxn() {
        DDBuilder {
            val a = AffineForm(this, 2.0..3.0, 1)
            val b = 2.0
            val c = AffineForm(this, 3.0..3.0, 1)

            var d : AffineForm = a.pow(b)
            //println("d = " + d)
            assertEquals(4.0, d.min, 0.001)
            assertEquals(9.0, d.max, 0.001)
            //println("a = " + a)
            //println("b = " + b)
            d = a.pow(c)
            //println("d = " + d)
            assertEquals(8.0, d.min, 0.001)
            assertEquals(27.0, d.max, 0.001)
        }
    }

    /**
     * Tests: Log of any number with a specified base
     * i.e., a generalized logarithm function
     */
    @Test
    fun testLogBaseFxn() {
        DDBuilder {
            val a = AffineForm(this, 1.0..1.0, 1)
            var b = log(10.0, a)

            //println("a = " + a)
            //println("b = " + b)
            assertEquals(0.0, b.min, 0.001)
            assertEquals(0.0, b.max, 0.001)
            b = log(10.0, AffineForm(this, 100.0..100.0, 1))
            //println("b = " + b)
            assertEquals(2.0, b.min, 0.001)
            assertEquals(2.0, b.max, 0.001)
            b = log(5.0, AffineForm(this, 100.0..100.0, 1))
            //println("b = " + b)
            assertEquals(ln(100.0) / ln(5.0), b.min, 0.001)
            assertEquals(ln(100.0) / ln(5.0), b.max, 0.001)
            b = log(2.0, AffineForm(this, 128.0..128.0, 1))
            //println("b = " + b)
            assertEquals(ln(128.0) / ln(2.0), b.min, 0.001)
            assertEquals(ln(128.0) / ln(2.0), b.max, 0.001)
            b = log(2.0, AffineForm(this, 1.5..1.5, 1))
            //println("b = " + b)
            assertEquals(0.5849625007211561, b.min, 0.001)
            assertEquals(0.5849625007211561, b.max, 0.001)
            b = log(2.0, AffineForm(this, 10.5..10.5, 1))
            //println("b = " + b)
            assertEquals(3.39231742277876, b.min, 0.001)
            assertEquals(3.39231742277876, b.max, 0.001)
            b = log(2.0, AffineForm(this, 8.0..8.0, 1))
            //println("b = " + b)
            assertEquals(3.0, b.min, 0.001)
            assertEquals(3.0, b.max, 0.001)
            b = log(2.0, AffineForm(this, 16.0..16.0, 1))
            //println("b = " + b)
            assertEquals(4.0, b.min, 0.001)
            assertEquals(4.0, b.max, 0.001)
        }
    }

    @Test
    //closed range with positive r
    //passes also closed range with two negative r's
    fun testAffineFormPlus() {
        val builder = DDBuilder()
        val range = Range(5.0, 10.0)
        val central = 7.5
        val r = -1.0
        val xi = hashMapOf(1 to 2.0, 2 to 4.0)
        assertFailsWith<DDInternalError> {
            val myAffine = AffineForm(builder, range, central, r, xi)
            val range2 = Range(10.0, 20.0)
            val central2 = 15.0
            val r2 = -2.0
            val yi = HashMap<Int, Double>()
            yi[1] = 3.0
            yi[2] = 1.0
            val affineToAdd = AffineForm(builder, range2, central2, r2, yi)

            val addedAffine = myAffine.plus(affineToAdd)
            assertEquals(22.5, addedAffine.central, 0.01)
        }
    }

    @Test
    // -inf to Real with positive r
    fun test2AffineFormPlus() {
        val min: Double = Double.NEGATIVE_INFINITY
        val range = Range(min, 15.0)
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 4.0

        val yi = HashMap<Int, Double>()
        val sharedNoise = xi[1]
        if (sharedNoise != null) {
            yi[1] = sharedNoise.toDouble()
        }
        yi[2] = 3.0
        val r = 2.0
        val central = 5.0
        DDBuilder {
            val a = AffineForm(this, range, central, r, xi)
            val b = AffineForm(this, Range(10.0, 20.0), 8.0, 1.0, yi)
            val addingTwoAF = a.plus(b)
            assertEquals(13.0, addingTwoAF.central, .01)
        }
    }

    @Test
    // Real to +inf with positive r
    fun test3AffineFormPlus() {
        val max: Double = Double.POSITIVE_INFINITY
        val range = Range(-12.0, max)
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 4.0

        val yi = HashMap<Int, Double>()
        val sharedNoise = xi[1]
        if (sharedNoise != null) {
            yi[1] = sharedNoise.toDouble()
        }
        yi[2] = 3.0
        val r = 2.0
        val central = 5.0
        DDBuilder {
            val a = AffineForm(this, range, central, r, xi)
            val b = AffineForm(this, Range(10.0, 20.0), 8.0, 1.0, yi)
            val addingTwoAF = a.plus(b)
            assertEquals(13.0, addingTwoAF.central, .01)
        }
    }

    @Test
    //[ to positive infinity with negative r
    //test passed with both when two affine from has two negative r's and one Af has
    //neg and another has positive r
    fun test4AffineFormPlus() {
        val max: Double = Double.POSITIVE_INFINITY
        val range = Range(-12.0, max)
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 4.0

        val yi = HashMap<Int, Double>()
        val sharedNoise = xi[1]
        if (sharedNoise != null) {
            yi[1] = sharedNoise.toDouble()
        }
        yi[2] = 3.0
        val r = 2.0
        val central = 5.0
        DDBuilder {
            val a = AffineForm(this, range, central, r, xi)
            val b = AffineForm(this, 10.0 .. 20.0, 8.0, 1.0, yi)
            val addingTwoAF = a.plus(b)
            assertEquals(13.0, addingTwoAF.central, .01)


        }
    }

    @Test
    //-inf to Real with negative r
    fun test5AffineFormPlus() {
        val min: Double = Double.NEGATIVE_INFINITY
        val max: Double = Double.POSITIVE_INFINITY
        val range = Range(min, 15.0)
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 4.0

        val yi = HashMap<Int, Double>()
        val sharedNoise = xi[1]
        if (sharedNoise != null) {
            yi[1] = sharedNoise.toDouble()
        }
        yi[2] = 3.0
        val r = 2.0
        val central = 5.0
        DDBuilder {
            val a = AffineForm(this, range, central, r, xi)
            val b = AffineForm(this, 10.0 .. max, 8.0, 1.0, yi)
            val addingTwoAF = a.plus(b)


            assertEquals(13.0, addingTwoAF.central, .01)


        }
    }

    @Test
    fun testAffineFromPlusWithScalar() {
        DDBuilder {
            assertFailsWith<DDInternalError> {
                val a = AffineForm(this, Double.NEGATIVE_INFINITY .. 15.0, central=5.0, r = -2.0,  hashMapOf(1 to 2.0, 2 to 4.0))
                val addAfWithScalar = a.plus(30.0)
                assertEquals(35.0, addAfWithScalar.central, .01)
            }
        }
    }


    @Test
    fun testAffineFormMinus() {
        val builder = DDBuilder()
        val range = Range(20.0, 40.0)
        val central = 10.0
        val r = 1.0
        val xi = HashMap<Int, Double>()
        xi[1] = 50.0
        xi[2] = 75.0
        val myAffineA = AffineForm(builder, range, central, r, xi)

        val range2 = Range(5.0, 10.0)
        val central2 = 7.5
        val r2 = 1.0
        val yi = HashMap<Int, Double>()
        yi[1] = 2.0
        yi[2] = 4.0
        val myAffineB = AffineForm(builder, range2, central2, r2, yi)
        val subAffineForm = myAffineA.minus(myAffineB)
        assertEquals(2.5, subAffineForm.central, .01)
    }

    @Test
    fun test1AffineFormTimes() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 4.0
        xi[3] = 8.0
        xi[4] = -9.0
        val yi = HashMap<Int, Double>()
        val sharedNoise = xi[1]
        val sharedNoiseTwo = xi[2]
        if (sharedNoise != null && sharedNoiseTwo != null) {
            yi[1] = sharedNoise.toDouble()
            yi[2] = sharedNoiseTwo.toDouble()
        }
        yi[3] = 3.0
        yi[4] = 10.0

        DDBuilder {
            val a = AffineForm(this, Range(-5.0, 15.0), 5.0, 2.0, xi)
            val b = AffineForm(this, Range(10.0, 20.0), 8.0, 1.0, yi)
            val multipliedTwoAF = a.times(b)
            assertEquals(40.0, multipliedTwoAF.central, .01)

        }
    }

    @Test
    fun test2AffineFormTimes() {
        val min: Double = Double.NEGATIVE_INFINITY
        val max: Double = Double.POSITIVE_INFINITY
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 4.0
        xi[3] = 8.0
        xi[4] = -9.0
        val yi = HashMap<Int, Double>()
        val sharedNoise = xi[1]
        val sharedNoiseTwo = xi[2]
        if (sharedNoise != null && sharedNoiseTwo != null) {
            yi[1] = sharedNoise.toDouble()
            yi[2] = sharedNoiseTwo.toDouble()
        }
        yi[3] = 3.0
        yi[4] = 10.0

        DDBuilder {
            val a = AffineForm(this, Range(min, 15.0), 5.0, 2.0, xi)
            val b = AffineForm(this, Range(10.0, max), 8.0, 1.0, yi)
            val multipliedTwoAF = a.times(b)
            assertEquals(40.0, multipliedTwoAF.central, .01)
        }
    }

    @Test
    fun test3AffineFormTimes() {
        val xi = HashMap<Int, Double>()
        xi[1] = 3.0
        xi[2] = -5.0
        xi[3] = 4.9
        DDBuilder {
            val a = AffineForm(this, Range(2.0, 50.0), 5.0, 1.0, xi)
            val b = AffineForm(this, 11.0)
            val multiplyAfWithScalar = a.times(b)
            assertEquals(55.0, multiplyAfWithScalar.central, 0.01)
        }
    }

    @Test
    fun test1AffineFormExp() {
        val xi = HashMap<Int, Double>()
        xi[1] = 1.0
        xi[2] = 3.0
        xi[3] = 4.9
        DDBuilder {
            val a = AffineForm(this, Range(2.0, 50.0), 4.0, 1.0, xi)
            val b = a.exp()
            assertEquals(7.3896, b.min, .01)
        }
    }

    @Test
    fun test2AffineFormExp() {
        val xi = HashMap<Int, Double>()
        xi[1] = 1.0
        xi[2] = 3.0
        xi[3] = 4.9
        DDBuilder {
            val a = AffineForm(this, Range(-2.0, 10.0), 4.0, 1.0, xi)
            val b = a.exp()
            assertEquals(0.135, b.min, .01)
        }
    }

    @Test
    fun test3AffineFormExp() {
        val xi = HashMap<Int, Double>()
        xi[1] = 1.0
        xi[2] = 3.0
        xi[3] = 4.9
        DDBuilder {
            val a = AffineForm(this, Range(-10.0, -2.0), 4.0, 1.0, xi)
            val b = a.exp()
            assertEquals(0.13534, b.max, .01)
        }
    }

    @Test
    fun test4AffineFormExp() {
        DDBuilder {
            val a = AffineForm(this, 2.0)
            val b = a.exp()
            assertEquals(7.38905, b.min, .001)
        }

    }

    @Test
    fun test5AffineFormExp() {
        val xi = HashMap<Int, Double>()
        xi[1] = 1.0
        xi[2] = 3.0
        xi[3] = 4.9
        val min: Double = Double.NEGATIVE_INFINITY
        DDBuilder {
            val a = AffineForm(this, Range(min, 4.0), 4.0, 1.0, xi)
            val b = a.exp()
            assertEquals(54.598150033, b.max, .01)
        }
    }

    @Test
    fun test1AffineFormLog() {
        val xi = HashMap<Int, Double>()
        xi[1] = 1.0
        xi[2] = 3.0
        xi[3] = 4.9
        DDBuilder {
            val a = AffineForm(this, Range(2.0, 50.0), 4.0, 1.0, xi)
            val b = a.log()
            assertEquals(0.693147, b.min, .01)
        }
    }

    @Test
    fun test2AffineFormLog() {
        val min: Double = Double.NEGATIVE_INFINITY
        val xi = HashMap<Int, Double>()
        xi[1] = 1.0
        xi[2] = 3.0
        xi[3] = 4.9
        DDBuilder {
            val a = AffineForm(this, Range(-2.0, 10.0), 4.0, 1.0, xi)
            val b = a.log()
            assertEquals(min, b.min, .01)
        }
    }

    @Test
    fun test3AffineFormLog() {
        DDBuilder {
            val xi = hashMapOf(1 to 1.0, 2 to 3.0, 3 to 4.9)
            val a = AffineForm(this, Range(-10.0, -2.0), 4.0, 1.0, xi)
            val b = a.log()
            assertEquals(Double.NEGATIVE_INFINITY, b.min, .01)
            assertEquals(ln(a.max), b.max, .01)
        }
    }

    @Test
    fun testOfChainComputation() {
        DDBuilder {
            val a = AffineForm(this, 1.0 .. 5.0, 1)
            val b = AffineForm(this, 1.0 .. 5.0, 1)
            val subAB = a - b
            subAB.exp()
            println(subAB.min)
            println(subAB.max)
            assertEquals(0.0, subAB.min, .00001)

        }

    }


    @Test
    fun testOfChainComputationAA() {
        DDBuilder {
            val a = AffineForm(this, 1.0 .. 5.0, 1)
            val b = AffineForm(this, 10.0 .. 12.0, 1)
            val d = AffineForm(this, -2.0 .. 7.0, 1) // x_1 = 4.5
            val add = a.plus(b)  // 11.0, 17.0
            val c = add.log() // 2.3978952..., 2.8332133...  x_1 = 0.217659...
            val e = c.minus(d) //-4.6021047..., 4.8332133...


            //println(e.min) // Difference of 2*x_c1 = 0.4353...
            //println(e.max) //0.41169
            assertTrue(e.min > -4.6021047 && e.max < 4.8332133)

        }

    }

    @Test
    fun testOfAffineFunInv() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        DDBuilder {
            val a = AffineForm(this, Range(3.0, 7.0), 5.0, 0.0, xi)

            val b = a.inv() // 1.0/7.0..1.0/3.0
            //println(b.min)
            //println(b.max)
            assertEquals(1.0/7.0, b.min, precision)
            assertEquals(1.0/3.0, b.max, precision)
        }


    }

    @Test
    fun testOfAffineFunInv2() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 3.0
        xi[3] = -1.0
        xi[4] = -2.5
        DDBuilder {
            val a = AffineForm(this, Range(-7.0, 3.0), 5.0, 3.0, xi) //-6.5..3.0

            val b = a.inv() // central 5.0 not in the range -6.5..3.0 ???  -6.5 = 5.0 - (2.0 + 3.0 + 1.0 + 2.5 + 3.0) --> 0.0 included in interval --> result = infinity
            //println(b.min)
            //println(b.max)
            assertEquals(this.AFReals, b)
        }
    }

    @Test
    fun testOfAffineFunInv3() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        DDBuilder {
            val a = AffineForm(this, Range(2.0, 7.0), 5.0, 0.0, xi) // 3.0..7.0
            val b = a.inv() // 1.0/7.0..1.0/3.0
            assertEquals(1.0/7.0, b.min, precision)
            assertEquals(1.0/3.0, b.max, precision)
        }
    }

    @Test
    fun testOfAffineFunInv4() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        DDBuilder {
            val a = AffineForm(this, 2.0..7.0, 5.0, 2.0, xi)
            // central + xi + r = 9.0 --> 7.0 is a higher upper bound,
            // central - xi - r = 1.0 --> 2.0 is a higher lower bound
            val b = a.inv() // 1.0/7.0..1.0/2.0
            //println(b.min)
            //println(b.max)
            assertEquals(1.0/7.0, b.min, precision)
            assertEquals(1.0/2.0, b.max, precision)
        }
    }

    @Test
    fun testOfAffineFunInv5() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 3.0
        xi[3] = -1.0
        xi[4] = -2.5
        DDBuilder {
            val a = AffineForm(this, Range(1.0, 12.0), 5.0, 3.0, xi) // 1.0..12.0, central + xi + r > 12, central - xi - r < 1

            val b = a.inv() //1.0/12.0..1.0/1.0
            //println(b.min)
            //println(b.max)
            assertEquals(1.0/12.0, b.min, precision)
            assertEquals(1.0/1.0, b.max, precision)
        }
    }

    @Test
    fun testOfAffineFunInv6() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        DDBuilder {
            val a = AffineForm(this, Range(-7.0, -2.0), 12.0, 1.0, xi)
            // central + xi + r = 15.0, central - xi - r = 9.0
            // --> no intersection of the ranges --> min = infinity, max = -infinity --> NaN?

            val b = a.inv()
            println(b.min)
            println(b.max)
            //TODO: min > max --> NaN ?
        }
    }

    @Test
    fun testOfAffineFunInv7() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        DDBuilder {
            val a = AffineForm(this, Range(-2.5, -1.0), 12.0, 3.0, xi) // again no intersection --> NaN???
            val b = a.inv()
            println(b.min)
            println(b.max)
            //TODO: min > max --> NaN ?
        }
    }

    @Test
    fun testOfAffineFunInv8() {
        DDBuilder {
            val a = AffineForm(this, 2.0) // scalar 2.0
            val b = a.inv() // scalar 1.0/2.0 --> min = max ; tolerance ca. 2 ulp
            assertEquals(0.5, b.min, 2*b.min.ulp)
            assertEquals(0.5, b.max, 2*b.max.ulp)
        }
    }

    @Test
    fun testOfAffineFunInv9() {
        DDBuilder {
            val a = AffineForm(this, -2.0) // scalar -2.0
            val b = a.inv() // scalar -1.0/2.0; tolerance ca. 2 ulp
            assertEquals(-0.5, b.min, 2*b.min.ulp)
            assertEquals(-0.5, b.max, 2*b.max.ulp)
        }
    }


    @Test

    fun testOfAffineFunSqrtMin1() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        DDBuilder {
            val a = AffineForm(this, Range(2.0, 4.0), 4.0, 2.0, xi) // central + xi + r = 8.0, central - xi - r = 0.0 --> 2.0..4.0
            val b = a.sqrt() // sqrt(2.0)..2.0
            assertEquals(sqrt(2.0), b.min, precision)
            assertEquals(2.0, b.max, precision)
        }
    }

    @Test
    fun testOfAffineFunSqrtMin2() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        DDBuilder {
            val a = AffineForm(this, Range(-4.0, -2.0), 4.0, 2.0, xi) // central + xi + r = 8.0, central - xi - r = 0.0 --> no intersection --> NaN?

            val b = a.sqrt()
            println(b.min)
            println(b.max)
            //TODO: min > max --> NaN ?

        }
    }

    @Test
    fun testOfAffineFunSqrtMin3() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 3.0
        xi[3] = 1.0
        DDBuilder {
            val a = AffineForm(this, Range(2.0, 3.0), 5.0, 2.0, xi) // central + xi + r = 13.0, central - xi - r = -2.0 --> 2.0..3.0

            val b = a.sqrt() //sqrt(2.0)..sqrt(3.0)
            //println(b.min)
            //println(b.max)
            assertEquals(sqrt(2.0), b.min, precision)
            assertEquals(sqrt(3.0), b.max, precision)
        }
    }

    @Test
    fun testOfAffineFunSqrtMin4() {
        DDBuilder {
            val xi = hashMapOf(1 to 2.0, 2 to 3.0, 3 to 1.0)
            val a = AffineForm(this, Range(-2.0, 3.0), 5.0, 2.0, xi)
            // central + xi + r = 13.0, central - xi - r = -2.0 --> -2.0..3.0
            val b = a.sqrt()
            assertEquals(0.0, b.min)
            assertEquals(sqrt(3.0), b.max, 0.0000001)
        }
    }

    @Test
    fun testOfAffineSin() {
        val xi = HashMap<Int, Double>()
        xi[1] = 0.2
        xi[2] = 0.3
        DDBuilder {
            val a = AffineForm(this, Range(0.0, 1.0), 0.5, 1.0, xi) // central - xi - r = -1.0, central + xi + r = 2.0 --> 0.0..1.0
            val b = a.sin() // sin(0.0)=0.0 .. sin(1.0)=0.84147...
            assertEquals(sin(0.0), b.min, precision)
            assertEquals(sin(1.0), b.max, precision)
        }
    }

    @Test
    fun testAffineSin2() {
        DDBuilder {
            val a = AffineForm(this, 0.3 .. 0.75, "b")
            val b = a.sin() //sin(0.3) = 0.2955 .. sin(0.75) = 0.6816...
            assertEquals(sin(0.3), b.min, precision)
            assertEquals(sin(0.75), b.max, precision)
        }
    }

    @Test
    fun testAffineSin3() {
        DDBuilder {
            val a = AffineForm(this, 0.0 .. 2.6, "t")
            val b = a.sin()
            println(b.min)
            println(b.max)
            //assertEquals(sin(0.0), b.min, precision) //TODO: -1.0 instead of 0.0
            assertEquals(1.0, b.max, precision)
        }
    }

    @Test
    fun testAffineSin4() {
        val xi = HashMap<Int, Double>()
        xi[1] = 2.0
        xi[2] = 3.0
        DDBuilder {
            val a = AffineForm(this, Range(7.0, 10.0), 2.0, 1.0, xi) // central - xi - r = -4.0, central + xi + r = 8.0 --> 7.0..8.0
            val b = a.sin() //sin(7.0) = 0.656986 .. sin(8.0) = 0.989358
            println(b.min)
            println(b.max)
            //TODO: -1.0..1.0 instead of 0.66..0.99
        }
    }

    @Test
    fun testAffineSin5() {
        DDBuilder {
            val a = AffineForm(this, -.75 .. 1.2, "b")
            val b = a.sin()
            assertEquals(sin(-.75), b.min, precision)
            assertEquals(sin(1.2), b.max, precision)
        }
    }

    @Test
    fun testAffinePow() {
        DDBuilder {
            val a = AffineForm(this, 2.0 .. 5.0, "a")
            val b = AffineForm(this, 2.0 .. 5.0, "b")
            val iAMaxTest = 5.0.pow(5.0)
            val iaMinTest = 2.0.pow(2.0)
            val c = a.pow(b)
            assertEquals(iaMinTest, c.min, 0.00001)
            assertEquals(iAMaxTest, c.max)
            //println(c.max)
        }
    }

    @Test
    fun testAffinePow2() {
        val xi = HashMap<Int, Double>()
        xi[1] = 1.1
        xi[2] = 0.3
        val yi = HashMap<Int, Double>()
        yi[1] = 0.5
        yi[2] = 1.5
        DDBuilder {
            val a = AffineForm(this, Range(0.9, 5.0), 2.0, 1.0, xi)
            val b = AffineForm(this, Range(0.5, 3.0), 2.1, 1.0, yi)
            val c = a.pow(b)
            val testIaMax = 5.0.pow(3.0)
            val testIaMin = 0.9.pow(3.0)
            assertTrue(c.max <= testIaMax)
            assertEquals(testIaMin, c.min, 0.00001)
        }
    }

    @Test
    fun testAffinePow3() {
        DDBuilder {
            val a = AffineForm(this, 2.0 .. 5.0, "q")
            val b = a.pow(2.0)
            val c = a.times(a)
            assertEquals(b.max ,c.max,0.00001)
        }
    }

    @Test
    fun testAffinePow4() {
        val xi = HashMap<Int, Double>()
        xi[1] = 1.1
        xi[2] = 0.3

        DDBuilder {
            val a = AffineForm(this, Range(2.5, 5.0), 1.5, 1.0, xi)
            val b = a.pow(2.0)
            val c = a.times(a)
            assertEquals(b.max,c.max,0.000001)
        }
    }

    @Test
    fun testAffinePow5() {
        val xi = HashMap<Int, Double>()
        xi[1] = 1.1
        xi[2] = 0.3
        xi[3] = 0.6

        DDBuilder {
            val a = AffineForm(this, Range(2.5, 5.0), 1.5, 1.0, xi)
            val b = a.pow(0.5)
            val c = a.sqrt()
            assertEquals(b.max,c.max,0.000001)
        }
    }

    @Test
    fun testAffinePow6() {
        DDBuilder {
            val a = AffineForm(this, 2.0 .. 5.0, "q")
            val b = a.pow(0.5)
            val c = a.sqrt()
            assertEquals(b.max, c.max, 0.000001)
            assertEquals(b.min, c.min, 0.000001)
        }
    }
    @Test
    fun testAffinePow7(){
        DDBuilder {
            val a = AffineForm(this, 2.0 .. 5.0, "q")
            val b = a.pow(-1.0)
            val c = a.inv()
            assertEquals(b.max, c.max, 0.000001)
        }
    }

    @Test
    fun testAffineCos(){
        DDBuilder {
            val xi = HashMap<Int, Double>()
            xi[1] = 0.2
            xi[2] = 0.3
            val a = AffineForm(this, Range(0.0 .. 1.0), 0.5, 1.0, xi) // central - xi - r = -1.0, central + xi + r = 2.0 --> 0.0..1.0
            val b = a.cos() // cos(0)=1.0 .. cos(1.0)=0.5403023..
            assertEquals(1.0, b.max, precision)
            assertEquals(0.5403023, b.min, precision)
        }
    }

    @Test
    fun testAffineCos2(){
        DDBuilder {
            val a = AffineForm(this, 0.1..1.0, 0.5, 1.0, hashMapOf(1 to 0.2, 2 to 0.3))
            // central - xi - r = -1.0, central + xi + r = 2.0 --> 0.0..1.0
            val b = a.cos() // cos(0.1)=0.995004 .. cos(1.0)=0.5403023..
            assertEquals(cos(0.1), b.max, precision)
            assertEquals(cos(1.0), b.min, precision)
        }
    }

    @Test
    fun testAffineCos3(){
        DDBuilder{
            val a = AffineForm(this, 0.2 .. 1.0, 0.5, 1.0, hashMapOf(1 to 0.2, 2 to 0.3))
            // central - xi - r = -1.0, central + xi + r = 2.0 --> 0.0..1.0
            val b = a.cos() // cos(0.2)=0.980067 .. cos(1.0)=0.5403023..
            assertEquals(cos(0.2), b.max, precision)
            assertEquals(cos(1.0), b.min, precision)
        }
    }

    @Test
    fun testArcSin01(){
        DDBuilder{
            val xi = HashMap<Int, Double>()
            xi [1] = 0.5
            val a = AffineForm(this, -1.0 .. 1.0, 0.0, 0.5, hashMapOf(1 to 0.5))
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }

    @Test
    fun testArcSin02(){
        DDBuilder{
            val a = AffineForm(this, -1.0..1.0, 0.0, 1.0, hashMapOf(1 to 0.0))
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }
    @Test
    fun testArcSin1(){
        DDBuilder {
            val a = AffineForm(this, -0.5 .. 0.5, 0.0, 0.0, hashMapOf(1 to 0.5))
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(-0.5), b.min, precision)
            assertEquals(asin(0.5), b.max, precision)
        }
    }

    @Test
    fun testArcSin2(){
        DDBuilder {
            val a = AffineForm(this, -1.0 .. 1.0, 0.0, 0.0, hashMapOf(1 to 1.0))
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(-1.0), b.min, precision)
            assertEquals(asin(1.0), b.max, precision)
        }
    }

    @Test
    fun testArcSin3(){
        DDBuilder{
            val a = AffineForm(this, 0.25 .. 0.9)
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }

    @Test
    fun testArcSin4(){
        DDBuilder {
            val a = AffineForm(this, -0.2 .. 0.8, 0.3, 0.0, hashMapOf(1 to 0.5))
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(-0.2), b.min, precision)
            assertEquals(asin(0.8), b.max, precision)
        }
    }

    @Test
    fun testArcSin5(){
        DDBuilder{
            val a = AffineForm(this, -0.4..-0.1)
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }

    @Test
    fun testArcSin6(){
        DDBuilder{
            val a = AffineForm(this, 0.0..0.1)
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }

    @Test
    fun testArcSin7(){
        DDBuilder{
            val a = AffineForm(this, -0.1 .. 0.0)
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }

    @Test
    fun testArcSin8(){
        DDBuilder{
            val a = AffineForm(this, 1.0)
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }

    @Test
    fun testArcSin9(){
        DDBuilder{
            val a = AffineForm(this, 0.0 .. 1.1)
            val b = a.arcsin()
            assertEquals(0.0, b.min)
            assertEquals(1.5708, b.max, 0.001)
        }
    }

    @Test
    fun testArcSin10(){
        DDBuilder{
            val a = AffineForm(this, -1.01 .. 0.0)
            val b = a.arcsin()
            assertEquals(-1.5708, b.min, 0.001)
            assertEquals(0.0, b.max, 0.001)
        }
    }

    @Test
    fun testArcSin11(){
        DDBuilder{
            val a = AffineForm(this, -1.1 .. 1.1)
            val b = a.arcsin()
            assertEquals(asin(-1.0), b.min, 0.0001)
            assertEquals(asin(1.0), b.max, 0.001)
        }
    }

    @Test
    fun testArcSin12(){
        DDBuilder{
            val a = AffineForm(this, 1.2..1.1)
            val b = a.arcsin()
            assertEquals(this.AFEmpty, b)
        }
    }

    @Test
    fun testArcSin13(){
        DDBuilder{
            val a = AffineForm(this, -1.0)
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }

    @Test
    fun testArcSin14(){
        DDBuilder{
            val a = AffineForm(this, 0.0)
            val b = a.arcsin()
            assertTrue(asin(a.max)-b.max <= 0.0)
            assertTrue(asin(a.min)-b.min >= 0.0)
            assertEquals(asin(a.min), b.min, precision)
            assertEquals(asin(a.max), b.max, precision)
        }
    }

    @Test
    fun testArcCos01(){
        DDBuilder{
            val xi = HashMap<Int, Double>()
            xi [1] = 0.5
            val a = AffineForm(this, -1.0..1.0, 0.0, 0.5, xi)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos02(){
        DDBuilder {
            val a = AffineForm(this, -1.0..1.0, 0.0, 1.0, hashMapOf(1 to 0.0))
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }
    @Test
    fun testArcCos1(){
        DDBuilder{
            val xi = HashMap<Int, Double>()
            xi [1] = 0.5
            val a = AffineForm(this, Range(-0.5,0.5), 0.0, 0.0, xi)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(-0.5), b.max, precision)
            assertEquals(acos(0.5), b.min, precision)
        }
    }

    @Test
    fun testArcCos2(){
        DDBuilder {
            val a = AffineForm(this, -1.0 .. 1.0, 0.0, 0.0, hashMapOf(1 to 1.0))
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos3(){
        DDBuilder{
            val a = AffineForm(this, 0.3 .. 0.9)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos4(){
        DDBuilder{
            val a = AffineForm(this, -0.3 .. 0.7)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos5(){
        DDBuilder{
            val a = AffineForm(this, -0.4 .. -0.1)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos6(){
        DDBuilder{
            val a = AffineForm(this, 0.0 .. 0.1)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos7(){
        DDBuilder{
            val a = AffineForm(this, -0.1 .. 0.0)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos8(){
        DDBuilder{
            val a = AffineForm(this, 1.0)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos9(){
        DDBuilder{
            val a = AffineForm(this, 0.0..1.1)
            val b = a.arccos()
            assertEquals(acos(1.0), b.min, 0.00001)
            assertEquals(acos(0.0), b.max, 0.00001)
        }
    }

    @Test
    fun testArcCos10(){
        DDBuilder{
            val a = AffineForm(this, -1.01 .. 0.0)
            val b = a.arccos()
            assertEquals(acos(0.0), b.min, 0.00001)
            assertEquals(acos(-1.0), b.max, 0.00001)
        }
    }

    @Test
    fun testArcCos11(){
        DDBuilder{
            val a = AffineForm(this, -1.1..1.1)
            val b = a.arccos()
            assertEquals(acos(-1.0), b.max, 0.000001)
            assertEquals(acos(1.0), b.min, 0.00001)
        }
    }

    @Test
    fun testArcCos12(){
        DDBuilder{
            val a = AffineForm(this, 1.2 .. 1.3)
            val b = a.arccos()
            assertEquals(this.AFEmpty, b)
        }
    }

    @Test
    fun testArcCos13(){
        DDBuilder{
            val a = AffineForm(this, -1.0)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }

    @Test
    fun testArcCos14(){
        DDBuilder{
            val a = AffineForm(this, 0.0)
            val b = a.arccos()
            assertTrue(acos(a.min)-b.max <= 0.0)
            assertTrue(acos(a.max)-b.min >= 0.0)
            assertEquals(acos(a.min), b.max, precision)
            assertEquals(acos(a.max), b.min, precision)
        }
    }
}