@file:Suppress("unused", "UnusedVariable", "LocalVariableName")

package dd.aaddtests

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.RealMath.abs
import io.github.tukcps.aadd.DDBuilder.RealMath.ceil
import io.github.tukcps.aadd.DDBuilder.RealMath.ceilAsLong
import io.github.tukcps.aadd.DDBuilder.RealMath.ceilToIntRange
import io.github.tukcps.aadd.DDBuilder.RealMath.div
import io.github.tukcps.aadd.DDBuilder.RealMath.exp
import io.github.tukcps.aadd.DDBuilder.RealMath.floor
import io.github.tukcps.aadd.DDBuilder.RealMath.floorAsLong
import io.github.tukcps.aadd.DDBuilder.RealMath.floorToIntRange
import io.github.tukcps.aadd.DDBuilder.RealMath.inv
import io.github.tukcps.aadd.DDBuilder.RealMath.invCeil
import io.github.tukcps.aadd.DDBuilder.RealMath.invFloor
import io.github.tukcps.aadd.DDBuilder.RealMath.ln
import io.github.tukcps.aadd.DDBuilder.RealMath.log
import io.github.tukcps.aadd.DDBuilder.RealMath.minus
import io.github.tukcps.aadd.DDBuilder.RealMath.negate
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.pow
import io.github.tukcps.aadd.DDBuilder.RealMath.power
import io.github.tukcps.aadd.DDBuilder.RealMath.sqrt
import io.github.tukcps.aadd.DDBuilder.RealMath.times
import io.github.tukcps.aadd.DDBuilder.RealMath.unaryMinus
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.create
import io.github.tukcps.aadd.values.real.ia.RealRange
import testutil.ddTest
import kotlin.math.exp
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * @class AADDTest
 * Tests for AADD data type
 */
class AADDTests {

    @Test
    // Check if infeasible paths are detected and leaves are marked.
    fun infeasibilityTest() {
        DDBuilder {
            val a = real(0.0..1.0, "n")
            val b = real(3.0..4.0, "n")
            val c1 = a greaterThan real(0.5)
            val c2 = a lessThan real(0.3)
            val d = c1.ite(a, b)
            val e = c2.ite(a, b)
            val f = d + e
            f.getRange()
            assertEquals(1, f.numInfeasible())
        }
    }

    /**
     * Check that order of indexes in maintained and that ite function orders
     * the result dd nodes according to index order.
     */
    @Test
    fun nodesOrderTest() {
        DDBuilder {
            val a = real(1.0..3.0, "i")
            val b = real(-1.0..1.0, "i")
            val c1=a greaterThan 2.0
            assertEquals(c1.index, c1.height())
            val c2=b greaterThan 0.0
            assertTrue(c1.index < c2.index)
            assertEquals(1, c2.height())
            val d=c1.ite(a * 2.0, b+1.0)
            assertEquals(c1.index, d.index)
            val e=c2.ite(d, a+b)
            assertEquals(c1.index, e.index)
        }
    }


    /** Subtraction computed as in affine arithmetic / interval arithmetic, and minimum assigned to range */
    @Test
    fun minusTest1() {
        DDBuilder {
            // toStringVerbose = true
            val a: AADD = real(1.0..2.0)
            val b: AADD = real(2.0..3.0)
            val c: AADD = a - b
            assertEquals(-2.0, c.getRange().min.finiteValue, 0.0000001)
            assertEquals(0.0, c.getRange().max.finiteValue, 0.0000001)
            val d = c - a
            assertEquals(-3.0, d.getRange().min.finiteValue, 0.00000001)
            assertEquals(-2.0, d.getRange().max.finiteValue, 0.00000001)
        }
    }

    /** Subtracting with Reals as one argument results in Reals again. */
    @Test
    fun minusTest2() {
        DDBuilder {
            val a: AADD = Reals.All
            val b: AADD = real(2.0 .. 3.0)
            val c = a-b
            assertEquals(Reals.All.min, c.getRange().min)
        }
    }


    @Test
    // Check if negation works for Scalar: value negates is - value.
    fun negateTest() = ddTest {
        val a = real(10.0)
        val r = negate(a)  as AADD.Leaf
        assertEquals(-10.0, r.getRange().min.finiteValue, 0.0001)
        assertEquals(0, r.value.xi.size)

        val b = real(5.0 .. 10.0, "b")
        val s = negate(b) as AADD.Leaf
        assertSafeInclusion(-10.0..-5.0, s, 0.0001)
        assertEquals(-2.5, s.value.xi[1L])
    }

    @Test
    fun negateTest2() = ddTest {
        val a = real(10.0)
        val r = negate(a) as AADD.Leaf
        // Check if negation works for AADD: value + negated value = 0
        val cond = conditions.newConstraint(create(
            this, DoubleBound.Finite(1.0) , DoubleBound.Finite(2.0), 3.0, 0.0), "cond")
        val t = internal(cond, a, r)
        val tn = - t
        val s = tn + t
        assertSafeInclusion(0.0..0.0, s, 0.00000001)
        // This assertion or above assertions fail if merging when considering the quantization error prevents merge.
        assertTrue(s is AADD.Leaf)
    }

    @Test
    fun plusTest() {
        DDBuilder {
            val a = real(10.0)
            val b = real(1.0)
            val r = a + b
            assertEquals(11.0, r.getRange().min.finiteValue, 0.001)
        }
    }

    @Test
    fun multTest() {
        DDBuilder {
            val a = real(10.0)
            val b = real(3.0)
            val r = a * b
            assertEquals(30.0, (r as AADD.Leaf).value.central)
        }
    }

    @Test
    fun expTest() {
        DDBuilder {
            val largerValue = create(this, RealRange.Reals, 10.0, 0.0, hashMapOf(1L to 2.0, 2L to 3.0))
            val affineForm1 = AffineForm.range(this, 1.0..2.0)
            // Test real
            val a = real(3.5)
            val b = real(-1.0)
            val exp1 = exp(a)
            val exp2 = exp(b)
            assertSafeInclusion(exp(3.5)..exp(3.5), exp1, 0.0000001)

            // Test Interval
            val c = leaf(affineForm1)
            val exp3 = exp(c) as AADD.Leaf
            // assertEquals(5.06, exp3.central, 0.01)
            // assertEquals(0.98, exp3.value.r, 0.01)
            assertSafeInclusion(exp(1.0)..exp(2.0), exp3, 0.01)

            val d = leaf(largerValue)
            val exp4 = exp(d) as AADD.Leaf
            // assertEquals(221755.0, exp4.value.central, 1.0)
            // assertEquals(217368.0, exp4.value.r, 1.0)
            assertEquals(exp(5.0), exp4.value.min.finiteValue, 0.00001)
            assertEquals(exp(15.0), exp4.value.max.finiteValue, 0.00001)
        }
    }

    /**
     * Tests: e^1 & e ^ -1
     */
    @Test
    fun testOfEulerNumber() = ddTest {
        val a = real(1.0)
        val b = real(-1.0)
        val c = real(0.0)

        var d : AADD = exp(a)
        assertSafeInclusion(2.718281828459045..2.7182818284590455, d, 0.00000001)
        assertSafeInclusion(exp(1.0)..exp(1.0), d, 1e-9)
        d = exp(b)
        assertSafeInclusion(exp(-1.0)..exp(-1.0), d, 0.00000001)
        d = exp(c)
        assertSafeInclusion(1.0..1.0, d, 0.01)
    }

    @Test
    fun sqrtTest() = ddTest {
        var terms = hashMapOf(1L to 2.0, 2L to 1.0)
        val largerValue = create(this, RealRange.Reals, 10.0, 0.0, terms)
        val affineForm1 = AffineForm.range(this, RealRange(1.0..2.0))
        terms = HashMap()
        terms[2L] = 0.5
        val restrictedRange = create(this, RealRange(1.1..1.9), 1.5, 0.0, terms)
        // Test real
        val a = leaf(affineForm1)
        val sqrt1 = sqrt(a) as AADD.Leaf
        assertEquals(1.2071, sqrt1.central, 0.1)
        assertEquals(1.0, sqrt1.min.finiteValue, 0.01)
        assertEquals(kotlin.math.sqrt(2.0), sqrt1.max.finiteValue, 0.01)
        val b = leaf(largerValue)
        val sqrt2 = sqrt(b) as AADD.Leaf
        assertEquals(3.13, sqrt2.central, 0.1)
        assertEquals(kotlin.math.sqrt(7.0), sqrt2.min.finiteValue, 0.01)
        assertEquals(kotlin.math.sqrt(13.0), sqrt2.max.finiteValue, 0.01)
        val c = leaf(restrictedRange)
        val sqrt3 = sqrt(c) as AADD.Leaf
        assertEquals(1.21, sqrt3.central, 0.1)
        assertEquals(1.0, sqrt1.min.finiteValue, 0.01)
        assertEquals(kotlin.math.sqrt(a.max.finiteValue), sqrt1.max.finiteValue, 0.01)
    }

    /**
     * Tests: Log of any any number with a specified base
     * i.e. a generalized logarithm function
     */
    @Test
    fun testLogBaseFxn() {
        DDBuilder {
            val a: Real = real(AffineForm.range(this, 1.0..1.0, "a"))
            var b: Real = log(a, 10.0)
            assertSafeInclusion(0.0..0.0, b, 0.001)
            b = log(leaf(AffineForm.range(this, 100.0..100.0, "a")), 10.0)
            //println("b = " + b)
            assertSafeInclusion(2.0..2.0, b, 0.001)
            b = log(leaf(AffineForm.range(this, 100.0..100.0, "a")), 5.0)
            //println("b = " + b)
            assertSafeInclusion(ln(100.0) / ln(5.0)..ln(100.0) / ln(5.0), b, 0.001)
            b = log(leaf(AffineForm.range(this, 128.0..128.0, "a")), 2.0)
            //println("b = " + b)
            assertSafeInclusion(ln(128.0) / ln(2.0)..ln(128.0) / ln(2.0), b, 0.001)
            b = log(leaf(AffineForm.range(this, 1.5..1.5, "a")), 2.0)
            //println("b = " + b)
            assertSafeInclusion(0.5849625007211561..0.5849625007211561, b.getRange(), 0.001)
            b = log(leaf(AffineForm.range(this, 10.5..10.5, "a")), 2.0)
            //println("b = " + b)
            assertSafeInclusion(3.39231742277876..3.39231742277876, b.getRange(), 0.001)
            b = log(leaf(AffineForm.scalar(this, 8.0)), 2.0)
            //println("b = " + b)
            assertSafeInclusion(3.0..3.0, b.getRange(), 0.001)
            b = log(leaf(AffineForm.range(this, 16.0..16.0, "a")), 2.0)
            //println("b = " + b)
            assertSafeInclusion(4.0..4.0, b.getRange(), 0.001)
        }
    }

    /**
     * The affine forms are enclosed in an IA result.
     * We check that it is computed correctly using IA.
     */
    @Test
    fun lnTestRange() = ddTest {
        val af = real(1.0..5.0)
        assertSafeInclusion(ln(1.0)..ln(5.0), ln(af), 0.0000001)
    }


    @Test
    fun powerTest() {
        DDBuilder {
            val a = real(2.0..3.0)
            val b = real(-1.0..3.0)
            val c = a power b
            assertSafeInclusion(1 / 3.0..27.0, c.getRange(), 0.0000001)
        }
    }

    @Test
    fun invTest() {
        DDBuilder {
            val affineForm1 = AffineForm.range(this, 1.0..2.0)
            val af3Node = real(-2.0..2.0)
            val inv = inv(af3Node)
            assertSafeInclusion(Reals.All, inv)

            // Empty -> Empty
            val empty = inv(Reals.Empty) as AADD.Leaf
            // //println("1/inf = $inv2 (shall be inf)")
            assertTrue(empty.value.isEmpty())
            
            // Real -> Real 
            val real = inv(Reals.All)
            assertSafeInclusion(Reals.All, real)

            // Regular
            val inv3Node = leaf(affineForm1)
            val inv3 = inv(inv3Node)
            assertSafeInclusion(1.0 / 2.0..1.0, inv3.getRange(), 0.001)
            //assertEquals(0.75, inv3.central, 0.0000001)
            assertSafeInclusion(0.5..1.0, inv3, 0.0000001)
            // assertEquals(0.25, inv3.radius, 0.0000001)
        }
    }

    @Test
    fun divTest() = ddTest {
        // Div by zero should return infinite, but division by scalar 0.0 should return Empty.
        val affineForm1 = AffineForm.range(this, 1.0..2.0)
        val affineForm1Node = leaf(affineForm1)
        val div = (affineForm1Node / (real(0.0))) as AADD.Leaf
        assertTrue(div.value.isEmpty())

        // Regular division
        val a = real(10.0)
        val b = real(5.0)
        var result = (a / b) as AADD.Leaf
        assertEquals(2.0, result.central)

        //Division tested by inversion + multiplication
        result = (a * inv(b) ) as AADD.Leaf
        assertEquals(2.0, result.central)
    }

    @Test
    fun checkApply() {
        DDBuilder {
            // Two BDD with different index.
            val ai = real(-1.0..1.0)
            val tr = real(0.1)
            val tr2 = real(0.2)
            val c1 = ai greaterThanOrEquals tr
            val c2 = ai.lessThanOrEquals(tr2)
            assertNotEquals(c1.index , c2.index)
            val a = real(1.0)
            val b = real(2.0)
            val d = c1.ite(a, b)
            assertEquals(c1.index , d.index)
            assertEquals(1, c1.height())
            val e = c2.ite(a, b)
            assertEquals(c2.index , e.index)
            assertEquals(e.height() , c2.height())
            val f = e.plus(d)
            assertEquals(2, f.height())
        }
    }

    /**
     * ITE of an AADD leave with a BDD of height 1 must result in merged AADD with height 1.
     */
    @Test fun iteTest2() {
        DDBuilder {
            val a = real(-1.0..1.0)
            val b = real(0.0)
            val c = a.greaterThanOrEquals(b)
            assertEquals(1, c.height())
            val d = c.ite(a, b)
            assertEquals(1, d.height())
        }
    }

    @Test fun reduceTest() = with(DDBuilder()) {
        val ai = real(-1.0..1.0, "1")
        val tr = real(0.1)
        val tr2 = real(0.2)
        val c1 = ai.greaterThanOrEquals(tr)
        val c2 = ai.lessThanOrEquals(tr2)
        val a = this.real(1.0..2.0, "1")
        val b = this.real(2.0..3.0, "1")
        val d = c1.ite(a, b)
        val e = c2.ite(a, b)
        assertEquals(c2.index, e.index)
        assertEquals(e.height(), c2.height())
        val f = e.plus(d)
        assertEquals(2, f.height())
    }


    /**
     * Testing:
     * ceiling fxn. for AADDs
     * Test version 1
     */
    @Test
    fun testCeilingFxnAADD1() {
        DDBuilder {
            val precision = 0.001
            val x = real(1.47..1.49)
            val z: AADD

            val y: AADD = ceil(x)
            assertSafeInclusion(2.0..2.0, y, precision)
            z = invCeil(y)
            assertSafeInclusion(1.0..2.0, z.getRange(), precision)
            val yL: Long = ceilAsLong(x)
            assertEquals(2, yL)
            val yIR: IntegerRange = ceilToIntRange(x)
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]")
            assertEquals(2L..2L, yIR)
            //println("End of test of Ceiling Fxn. for AADD data type\n")
        }
    }

    /**
     * Testing:
     * ceiling fxn. for AADDs
     * Test version 2
     */
    @Test
    fun testCeilingFxnAADD2() {
        DDBuilder {
            val precision = 0.001
            val x = real(1.47..2.49)
            val z: AADD

            val y: AADD = ceil(x)
            assertSafeInclusion(2.0..3.0, y.getRange(), precision)
            z = invCeil(y)

            assertSafeInclusion(1.0..3.0, z, precision)
            val yL: Long = ceilAsLong(x)
            //println("yL = " + yL)
            assertEquals(3, yL)
            val yIR: IntegerRange = ceilToIntRange(x)
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]")
            assertEquals(2L..3L, yIR)
            //println("End of test of Ceiling Fxn. for AADD data type\n")
        }
    }

    /**
     * Testing:
     * ceiling fxn. for AADDs
     * Test version 1
     */
    @Test
    fun testFloorFxnAADD1() {
        DDBuilder {
            val precision = 0.001
            val x = real(1.51..1.53)
            val z: AADD

            //println("Beginning test of Floor Fxn. for AADD data type, version 1\n")
            val y: AADD = floor(x)
            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            assertSafeInclusion(1.0..1.0, y.getRange(), precision)
            z = invFloor(y)
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")
            assertSafeInclusion(1.0..2.0, z.getRange(), precision)
            val yL: Long = floorAsLong(x)
            //println("yL = " + yL)
            assertEquals(1, yL)
            val yIR: IntegerRange = floorToIntRange(x)
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]")
            assertEquals(1L..1L, yIR)
            //println("End of test of Floor Fxn. for AADD data type\n")
        }
    }

    /**
     * Testing:
     * ceiling fxn. for AADDs
     * Test version 2
     */
    @Test
    fun testFloorFxnAADD2() {
        DDBuilder {
            val precision = 0.001
            val x = real(1.51..2.53)
            val z: AADD

            //println("Beginning test of Floor Fxn. for AADD data type, version 2\n")
            val y: AADD = floor(x)
            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            assertSafeInclusion(1.0..2.0, y, precision)
            z = invFloor(y)
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")
            assertSafeInclusion(1.0..3.0, z, precision)
            val yL: Long = floorAsLong(x)
            //println("yL = " + yL)
            assertEquals(1, yL)
            val yIR = floorToIntRange(x)
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]")
            assertEquals(1L..2L, yIR)
            //println("End of test of Floor Fxn. for AADD data type\n")
        }
    }

    /**
     * Testing:
     * AADD floor fxn. as it appears in the neural network example using values for run 4
     * Input values are as follows:
     * - C: Real(24.0 .. 24.0)." // variable to be changed
     * - C_w: Real(50.0 .. 50.0)."
     * - K: Real(24.0 .. 24.0)."
     * - F: Real(9.0 .. 9.0)."
     * - s: Real(1.0 .. 1.0)."
     * - p: Real(1.0 .. 1.0)." // padding, 0 = NOT enabled, 1 = enabled
     * - i: Real(1.0 .. 1.0)."
     * - t_l: Real(0.0 .. 0.0)."
     * - timeinyears: Real(10.0 .. 10.0)
     */
    @Test
    fun testFloorFxnFromNeuralNetworkRun4_var_a_pb() {
        DDBuilder {
            val precision = 0.001

            //println("\nBeginning test of Floor Fxn. from Neural Network Example using AADD data type, var is a_pb\n")
            val F = real(9.0 .. 9.0)
            val p = real(1.0..1.0)
            val s = real(1.0..1.0)
            val C_wb = floor(F / 2.0)
            val C_wb_s_floor_arg: AADD = (C_wb - 1.0) / s + 1.0
            val a_pb: AADD = p * floor(C_wb_s_floor_arg)

            assertSafeInclusion(9.0..9.0, F.getRange(), precision)
            assertSafeInclusion(1.0..1.0, p.getRange(), precision)
            assertSafeInclusion(1.0..1.0, s.getRange(), precision)
            assertSafeInclusion(4.0..4.0, C_wb.getRange(), precision)
            assertSafeInclusion(4.0..4.0, C_wb_s_floor_arg.getRange(), precision)
            assertSafeInclusion(4.0..4.0, a_pb.getRange(), precision)
        }
    }

    /**
     * Testing:
     * AADD floor fxn. as it appears in the neural network example using values for run 4
     * Input values are as follows:
     * C: Real(24.0 .. 24.0). // variable to be changed
     * C_w: Real(50.0 .. 50.0).
     *  K: Real(24.0 .. 24.0).
     *  F: Real(9.0 .. 9.0).
     *  s: Real(1.0 .. 1.0).
     * p: Real(1.0 .. 1.0). // padding, 0 = NOT enabled, 1 = enabled
     * i: Real(1.0 .. 1.0).
     * t_l: Real(0.0 .. 0.0).
     * timeinyears: Real(10.0 .. 10.0)
     */
    @Test
    fun testFloorFxnFromNeuralNetworkRun4_var_a_w() {
        DDBuilder {
            val precision = 0.001
            val C_w = real(50.0..50.0)
            val F = real(9.0..9.0)
            val s = real(1.0..1.0)
            val p = real(1.0..1.0)// padding, 0 = NOT enabled, 1 = enabled
            val C_wb = floor(F / 2.0)
            val C_w_hat: AADD = C_w + p * 2.0 * C_wb
            val a_w: AADD = floor((C_w_hat - F) / s + 1.0)

            assertSafeInclusion(9.0..9.0, F, precision)
            assertSafeInclusion(1.0..1.0, p, precision)
            assertSafeInclusion(1.0..1.0, s, precision)
            assertSafeInclusion(4.0..4.0, C_wb, precision)
            assertSafeInclusion(58.0..58.0, C_w_hat, precision)
            assertSafeInclusion(50.0..50.0, a_w, precision)
        }
    }

    @Test
    fun testITEBounds(){
        DDBuilder {
            var a: AADD = real(-1.0..1.0)
            IF(a greaterThanOrEquals 0.0)
            a = assign(a, a + 10.0)
            ELSE()
            a = assign(a, a - 10.0)
            END()
            assertEquals(1, a.height())
            assertSafeInclusion(-11.0..11.0, a, 0.000001)
            //  println(a.toIteString())
            a.getRange()
            assertSafeInclusion(-11.0..11.0, a, 0.000001)
            // println(a.toIteString())
        }
    }

    @Test
    fun testQuadratic1(){
        DDBuilder {
            //Worst Case for Multiplication
            val x = real(-20.0..20.0, 1.toString())
            val a = x.times(x) as AADD.Leaf
            assertSafeInclusion(-400.0..400.0, a, 0.0000001)
            // will x1 == 0.0 because central value therefore the multiplication plane is zero
            assertEquals(0.0, a.value.xi[1]!!, 0.0000001)
        }
    }

    @Test
    fun testQuadratic2ViaPow(){
        DDBuilder {
            val x = real(0.5..20.0, 1.toString())
            val a = pow(x, 2.0) as AADD.Leaf
            assertSafeInclusion(0.25..400.0, a, 0.0000001)
            // assertTrue(a.radius < 500.0)
        }
    }

    @Test
    fun testQuadratic3ViaPow(){
        DDBuilder {
            val x = real(0.5..20.0)
            val a = x power (real(2.0) as AADD.Leaf)
            // assertTrue(a.radius < 500.0)
            assertSafeInclusion(0.25..400.0, a, 0.0000001)
        }
    }

    /* @Test
    fun testQuadratic4ViaPow(){
        DDBuilder {
            val x = real(0.5..20.0 )
            val a = x.power(real(2.0)) as AADD.Leaf
            assertEquals(9.75, a.value.xi[1]!! ,0.0000001)
            assertEquals(0.25..400.0, a.value, 0.0000001)
        }
    }
*/
    @Test
    fun testPowAffine(){
        DDBuilder {
            val x = real(0.5..20.0, 2.toString())
            val a = x power real(2.0 .. 3.0, 1.toString())
            assertSafeInclusion(0.125..8000.0, a, 0.0000001)
            //assertEquals(9.75,a.value.xi[2])
        }
    }

    @Test
    fun testAbs(){
        DDBuilder {
            val x = real(-2.0..1.0, 1.toString())
            val y = real(-3.0..-1.0, 2.toString())

            val decision = x.greaterThan(-1.5)
            val tree = decision.ite(x, y)

            val z = abs(tree)

            val r = z.getRange()

            assertSafeInclusion(0.0..3.0, z, 0.0000001)
            assertEquals(3, z.numLeaves())

            val rightChild = if (z is AADD.Internal) z.T else Bool.Empty

            assertEquals(2, rightChild.numLeaves())
            val rightleftChild = if (rightChild is AADD.Internal) rightChild.F else Bool.Empty
            val rightleftChildValue = if (rightleftChild is AADD.Leaf) rightleftChild.getRange() else AF.Empty
            assertSafeInclusion(0.0..1.5, rightleftChildValue, 0.0000001)
            // assertEquals(-1.5, rightleftChildValue.xi[1]!!,0.0000001)

            val leftiChild = if (z is AADD.Internal) z.F else Bool.Empty

            val value = if (leftiChild is AADD.Leaf) leftiChild.value else AF.Empty
            assertSafeInclusion(1.0..3.0, value, 0.0000001)
            assertEquals(-1.0, value.xi[2L]!!, 0.0000001)
        }
    }

    @Test
    fun testPowRangeCloseToZero(){
        DDBuilder {
            val x = real(0.1..2.0)
            val z = x power real(1.0..2.0)

            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            //assertEquals(1.95, z.central, tol)
            assertSafeInclusion(0.01..4.0, z, 0.0000001)
        }
    }

    /**
     *
     * */
    @Test
    fun pathConditionTest() {
        DDBuilder {
            val A = real(0.0..0.0) greaterThanOrEquals real(1.0)
            val B = real(0.0..0.0) lessThanOrEquals real(1.0)
            var rangeC: AADD = real(1.0..1.0)
            IF(B)
            IF(A)
            rangeC = assign(rangeC, real(2.0))
            END()
            END()
            assertSafeInclusion(1.0..1.0, rangeC)
        }
    }
}