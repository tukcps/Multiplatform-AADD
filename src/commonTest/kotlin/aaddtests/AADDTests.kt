@file:Suppress("unused", "UnusedVariable")

package aaddtests

import com.github.tukcps.aadd.*
import com.github.tukcps.aadd.functions.floor
import com.github.tukcps.aadd.functions.log
import com.github.tukcps.aadd.functions.pow
import com.github.tukcps.aadd.values.*
import kotlin.math.exp
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * @class AADDTest
 * Test suite for AADD data type
 * @author Christoph Grimm, Jack D. Martin
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
            val f = d.plus(e)
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
            val d=c1.ite(a*2.0, b+1.0)
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
            val a: AADD = real(1.0 .. 2.0)
            val b: AADD = real(2.0 .. 3.0)
            val c = a-b
            assertEquals(-2.0, c.getRange().min, 0.0000001)
            assertEquals(0.0, c.getRange().max, 0.0000001)
            val d = c-a
            assertEquals(-3.0, d.getRange().min, 0.00000001)
            assertEquals(-2.0, d.getRange().max, 0.00000001)
        }
    }

    /** Subtracting with Reals as one argument results in Reals again. */
    @Test
    fun minusTest2() {
        DDBuilder {
            val a: AADD = Reals
            val b: AADD = real(2.0 .. 3.0)
            val c = a-b
            assertEquals(Reals.min, c.getRange().min)
        }
    }


    @Test
    // Check if negation works for Scalar: value negates is - value.
    fun negate() {
        DDBuilder {
            val a = real(10.0)
            val r = a.negate()  as AADD.Leaf
            assertEquals(-10.0, r.getRange().min, 0.0001)
            assertEquals(0, r.value.xi.size)

            val b = real(5.0 .. 10.0, "b")
            val s = b.negate()  as AADD.Leaf
            assertEquals(-10.0, s.getRange().min, 0.0001)
            assertEquals(-5.0, s.getRange().max, 0.0001)
            assertEquals(-2.5, s.value.xi[1])
        }
    }

    @Test
    fun negate2() {
        DDBuilder {
            val a = real(10.0)
            val r = a.negate()  as AADD.Leaf
            // Check if negation works for AADD: value + negated value = 0
            val cond = conds.newConstraint(AffineForm(this, 1.0 .. 2.0, 3), "cond")
            val t = internal(cond, a, r)
            val tn = t.negate()
            val s = tn.plus(t)
            assertEquals(0.0, s.getRange().min, 0.00000001)
            assertEquals(0.0, s.getRange().max, 0.00000001)
            // This assertion or above assertions fail if merging when considering the quantization error prevents merge.
            assertTrue(s is AADD.Leaf)
        }
    }

    @Test
    fun plusTest() {
        DDBuilder {
            val a = real(10.0)
            val b = real(1.0)
            val r = a.plus(b)
            assertEquals(r.getRange().min, 11.0, 0.001)
        }
    }

    @Test
    fun multTest() {
        DDBuilder {
            val a = real(10.0)
            val b = real(3.0) as AADD.Leaf
            val r = a.times(b) as AADD.Leaf
            assertEquals(30.0, r.central)
        }
    }

    @Test
    fun expTest() {
        DDBuilder {
            val largerValue = AffineForm(this, Range.Reals, 10.0, 0.0, hashMapOf(1 to 2.0, 2 to 3.0))
            val affineForm1 = AffineForm(this, 1.0 .. 2.0)
            // Test real
            val a = real(3.5)
            val b = real(-1.0)
            val exp1 = a.exp()
            val exp2 = b.exp()
            assertEquals(exp(3.5), exp1.getRange().min, 0.0000001)
            assertEquals(exp(-1.0), exp2.getRange().min, 0.0000001)

            // Test Interval
            val c = leaf(affineForm1)
            val exp3 = c.exp() as AADD.Leaf
            assertEquals(5.06, exp3.central, 0.01)
            assertEquals(0.98, exp3.value.r, 0.01)
            assertEquals(2.72, exp3.min, 0.01)
            assertEquals(7.39, exp3.max, 0.01)

            val d = leaf(largerValue)
            val exp4 = d.exp() as AADD.Leaf
            // assertEquals(221755.0, exp4.value.central, 1.0)
            // assertEquals(217368.0, exp4.value.r, 1.0)
            assertEquals(exp(5.0), exp4.value.min, 0.00001)
            assertEquals(exp(15.0), exp4.value.max, 0.00001)
        }
    }

    /**
     * Tests: e^1 & e ^ -1
     */
    @Test
    fun testOfEulersNumber() {
        DDBuilder {
            val a = real(1.0)
            val b = real(-1.0)
            val c = real(0.0)

            var d : AADD = a.exp()
            assertEquals(2.7182818284590446, d.getRange().min, 0.01)
            assertEquals(2.7182818284590446, d.getRange().max, 0.01)
            d = b.exp()
            assertEquals(0.3678794411714423, d.getRange().min, 0.01)
            assertEquals(0.3678794411714423, d.getRange().max, 0.01)
            d = c.exp()
            assertEquals(1.0, d.getRange().min, 0.01)
            assertEquals(1.0, d.getRange().max, 0.01)
        }
    }

    @Test
    fun sqrtTest() {
        DDBuilder {
            var terms = hashMapOf(1 to 2.0, 2 to 1.0)
            val largerValue = AffineForm(this, Range.Reals, 10.0, 0.0, terms)
            val affineForm1 = AffineForm(this, 1.0 .. 2.0)
            terms = HashMap()
            terms[2] = 0.5
            val restrictedRange = AffineForm(this, 1.1 .. 1.9, 1.5, 0.0, terms)
            // Test real
            val a = leaf(affineForm1)
            val sqrt1 = a.sqrt() as AADD.Leaf
            assertEquals(1.2071, sqrt1.central, 0.01)
            assertEquals(0.03, sqrt1.r, 0.01)
            assertEquals(1.0, sqrt1.min, 0.01)
            assertEquals(kotlin.math.sqrt(2.0), sqrt1.max, 0.01)
            val b = leaf(largerValue)
            val sqrt2 = b.sqrt() as AADD.Leaf
            assertEquals(3.13, sqrt2.central, 0.01)
            assertEquals(0.06, sqrt2.r, 0.01)
            assertEquals(kotlin.math.sqrt(7.0), sqrt2.min, 0.01)
            assertEquals(kotlin.math.sqrt(13.0), sqrt2.max, 0.01)
            val c = leaf(restrictedRange)
            val sqrt3 = c.sqrt() as AADD.Leaf
            assertEquals(1.21, sqrt3.central, 0.01)
            assertEquals(0.02, sqrt3.r, 0.01)
            assertEquals(1.0, sqrt1.min, 0.01)
            assertEquals(kotlin.math.sqrt(a.max), sqrt1.max, 0.01)
        }
    }

    @Test
    fun logTest() {
        DDBuilder {
            scheme=DDBuilder.ApproximationScheme.Chebyshev
            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val largerValue = AffineForm(this, central = 10.0, r = 0.0, xi=terms)
            val affineForm1 = AffineForm(this, 1.0 .. 2.0)
            terms = HashMap()
            terms[2] = 0.5
            val restrictedRange = AffineForm(this, 1.1 .. 1.9, 1.5, 0.0, terms)

            val a = leaf(affineForm1)
            val log1 = a.log() as AADD.Leaf
            assertEquals(0.38, log1.central, 0.01)
            assertEquals(0.02, log1.r, 0.01)
            assertEquals(0.0, log1.min, 0.01)
            assertEquals(ln(a.max), log1.max, 0.01)
            val b = leaf(largerValue)
            val log2 = b.log() as AADD.Leaf
            assertEquals(2.28, log2.central, 0.01)
            assertEquals(0.02, log2.r, 0.01)
            assertEquals(ln(b.min), log2.min, 0.01)
            assertEquals(ln(b.max), log2.max, 0.01)
            val c = leaf(restrictedRange)
            val log3 = c.log() as AADD.Leaf
            assertEquals(0.39, log3.central, 0.01)
            assertEquals(0.02, log3.value.r, 0.01)
            assertEquals(ln(c.min), log3.min, 0.01)
            assertEquals(ln(c.max), log3.max, 0.01)

            // Check the result of IA part.
            val x = real(1.0 .. 5.0)
            val logx = x.log()
            assertEquals(ln(1.0), logx.getRange().min, 0.000001)
            assertEquals(ln(5.0), logx.getRange().max, 0.000001)
        }
    }

    /**
     * Tests: Log of any any number with a specified base
     * i.e. a generalized logarithm function
     */
    @Test
    fun testLogBaseFxn() {
        DDBuilder {
            val a = leaf(AffineForm(this, 1.0 .. 1.0, "a"))
            var b = log(10.0, a)

            //println("a = " + a)
            //println("b = " + b)
            assertEquals(0.0, b.getRange().min, 0.001)
            assertEquals(0.0, b.getRange().max, 0.001)
            b = log(10.0, leaf(AffineForm(this, 100.0..100.0, "a")))
            //println("b = " + b)
            assertEquals(2.0, b.getRange().min, 0.001)
            assertEquals(2.0, b.getRange().max, 0.001)
            b = log(5.0, leaf(AffineForm(this, 100.0 .. 100.0, "a")))
            //println("b = " + b)
            assertEquals(ln(100.0) / ln(5.0), b.getRange().min, 0.001)
            assertEquals(ln(100.0) / ln(5.0), b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 128.0..128.0, "a")))
            //println("b = " + b)
            assertEquals(ln(128.0) / ln(2.0), b.getRange().min, 0.001)
            assertEquals(ln(128.0) / ln(2.0), b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 1.5 .. 1.5, "a")))
            //println("b = " + b)
            assertEquals(0.5849625007211561, b.getRange().min, 0.001)
            assertEquals(0.5849625007211561, b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 10.5 .. 10.5, "a")))
            //println("b = " + b)
            assertEquals(3.39231742277876, b.getRange().min, 0.001)
            assertEquals(3.39231742277876, b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 8.0 .. 8.0, "a")))
            //println("b = " + b)
            assertEquals(3.0, b.getRange().min, 0.001)
            assertEquals(3.0, b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 16.0..16.0, "a")))
            //println("b = " + b)
            assertEquals(4.0, b.getRange().min, 0.001)
            assertEquals(4.0, b.getRange().max, 0.001)
        }
    }

    /**
     * The affine forms are enclosed in an IA result.
     * We check that it is computed correctly using IA.
     */
    @Test
    fun logTestIA() {
        DDBuilder {
            scheme=DDBuilder.ApproximationScheme.Chebyshev
            val af = real(1.0..5.0)
            assertEquals(ln(1.0), af.log().getRange().min, 0.0000001)
            assertEquals(ln(5.0), af.log().getRange().max, 0.0000001)
        }
    }

    @Test
    fun powerTest() {
        DDBuilder {
            val a = real(2.0..3.0)
            val b = real(-1.0..3.0)
            val c = a power b
            assertEquals(1/3.0, c.getRange().min, 0.0000001)
            assertEquals(27.0, c.getRange().max, 0.0000001)
        }
    }

    @Test
    fun invTest() {
        DDBuilder {
            val affineForm1 = AffineForm(this, 1.0 .. 2.0)
            val af3Node = real(-2.0..2.0)
            val inv = af3Node.inv() as AADD.Leaf
            assertTrue(inv.value.isReals())

            // Empty -> Empty
            val empty = Empty.inv() as AADD.Leaf
            // //println("1/inf = $inv2 (shall be inf)")
            assertTrue(empty.value.isEmpty())
            
            // Real -> Real 
            val real = Reals.inv() as AADD.Leaf
            assertTrue(real.value.isReals())

            // Regular
            val inv3Node = leaf(affineForm1)
            val inv3 = inv3Node.inv() as AADD.Leaf
            assertEquals(0.75, inv3.central, 0.0000001)
            assertEquals(0.5, inv3.min, 0.0000001)
            assertEquals(1.0, inv3.max, 0.0000001)
            assertEquals(0.125, inv3.r, 0.0000001)
            assertEquals(0.25, inv3.radius, 0.0000001)
        }
    }

    @Test
    fun divTest() {
        // Div by zero should return infinite
        DDBuilder {
            val affineForm1 = AffineForm(this, 1.0 .. 2.0)
            val zeroNode = real(0.0)
            val affineForm1Node = leaf(affineForm1)
            val div = affineForm1Node.div(zeroNode) as AADD.Leaf
            assertTrue(div.value.isEmpty())

            // Regular division
            val a = real(10.0)
            val b = real(5.0)
            var result = a.div(b) as AADD.Leaf
            assertEquals(result.central, 2.0)

            //Division tested by inversion + multiplication
            result = a.times(b.inv()) as AADD.Leaf
            assertEquals(result.central, 2.0)
        }
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
            assertEquals(c1.height() , 1)
            val e = c2.ite(a, b)
            assertEquals(c2.index , e.index)
            assertEquals(e.height() , c2.height())
            val f = e.plus(d)
            assertEquals(f.height() , 2)
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

    @Test fun reduceTest() {
        val builder = DDBuilder()
        // Two BDD with different index.
        with(builder) {
            val ai = real(-1.0..1.0, "1")
            val tr = real(0.1)
            val tr2 = real(0.2)
            val c1 = ai.greaterThanOrEquals(tr)
            val c2 = ai.lessThanOrEquals(tr2)
            val a = this.real(1.0..2.0, "1")
            val b = this.real(2.0..3.0, "1")
            val d = c1.ite(a, b)
            val e = c2.ite(a, b)
            assertTrue(c2.index == e.index)
            assertTrue(e.height() == c2.height())
            val f = e.plus(d)
            assertEquals(2, f.height())
        }
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

            //println("Beginning test of Ceiling Fxn. for AADD data type, version 1\n")
            val y: AADD = x.ceil()
            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            assertEquals(2.0, y.getRange().min, precision)
            assertEquals(2.0, y.getRange().max, precision)
            z = y.invCeil()
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")
            assertEquals(1.0, z.getRange().min, precision)
            assertEquals(2.0, z.getRange().max, precision)
            val yL: Long = x.ceilAsLong()
            //println("yL = " + yL)
            assertEquals(2, yL)
            val yIR: IntegerRange = x.ceiltoIntRange()
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]")
            assertEquals(2, yIR.min)
            assertEquals(2, yIR.max)
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

            //println("Beginning test of Ceiling Fxn. for AADD data type, version 2\n")
            val y: AADD = x.ceil()
            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            assertEquals(2.0, y.getRange().min, precision)
            assertEquals(3.0, y.getRange().max, precision)
            z = y.invCeil()
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")
            assertEquals(1.0, z.getRange().min, precision)
            assertEquals(3.0, z.getRange().max, precision)
            val yL: Long = x.ceilAsLong()
            //println("yL = " + yL)
            assertEquals(3, yL)
            val yIR: IntegerRange = x.ceiltoIntRange()
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]")
            assertEquals(2, yIR.min)
            assertEquals(3, yIR.max)
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
            val y: AADD = x.floor()
            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            assertEquals(1.0, y.getRange().min, precision)
            assertEquals(1.0, y.getRange().max, precision)
            z = y.invFloor()
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")
            assertEquals(1.0, z.getRange().min, precision)
            assertEquals(2.0, z.getRange().max, precision)
            val yL: Long = x.floorAsLong()
            //println("yL = " + yL)
            assertEquals(1, yL)
            val yIR: IntegerRange = x.floorToIntRange()
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]")
            assertEquals(1, yIR.min)
            assertEquals(1, yIR.max)
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
            val y: AADD = x.floor()
            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            assertEquals(1.0, y.getRange().min, precision)
            assertEquals(2.0, y.getRange().max, precision)
            z = y.invFloor()
            //println("y = [" + y.getRange().min + ", " + y.getRange().max + "]")
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")
            assertEquals(1.0, z.getRange().min, precision)
            assertEquals(3.0, z.getRange().max, precision)
            val yL: Long = x.floorAsLong()
            //println("yL = " + yL)
            assertEquals(1, yL)
            val yIR = x.floorToIntRange()
            //println("yIR = [" + yIR.min + ", " + yIR.max + "]")
            assertEquals(1, yIR.min)
            assertEquals(2, yIR.max)
            //println("End of test of Floor Fxn. for AADD data type\n")
        }
    }

    /**
     * Testing:
     * AADD floor fxn. as it appears in the neural network example using values for run 4
     * Input values are as follows:
     * C: Real(24.0 .. 24.0)." // variable to be changed
     * C_w: Real(50.0 .. 50.0)."
     *  K: Real(24.0 .. 24.0)."
     *  F: Real(9.0 .. 9.0)."
     *  s: Real(1.0 .. 1.0)."
     * p: Real(1.0 .. 1.0)." // padding, 0 = NOT enabled, 1 = enabled
     * i: Real(1.0 .. 1.0)."
     * t_l: Real(0.0 .. 0.0)."
     * timeinyears: Real(10.0 .. 10.0)
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

            //println("F = [" + F.getRange().min + ", " + F.getRange().max + "]")
            //println("p = [" + p.getRange().min + ", " + p.getRange().max + "]")
            //println("s = [" + s.getRange().min + ", " + s.getRange().max + "]")
            //println("C_wb = [" + C_wb.getRange().min + ", " + C_wb.getRange().max + "]")
            //println("C_wb_s_floor_arg = [" + C_wb_s_floor_arg.getRange().min + ", " + C_wb_s_floor_arg.getRange().max + "]")
            //println("a_pb = [" + a_pb.getRange().min + ", " + a_pb.getRange().max + "]")
            assertEquals(9.0, F.getRange().min, precision)
            assertEquals(9.0, F.getRange().max, precision)
            assertEquals(1.0, p.getRange().min, precision)
            assertEquals(1.0, p.getRange().max, precision)
            assertEquals(1.0, s.getRange().min, precision)
            assertEquals(1.0, s.getRange().max, precision)
            assertEquals(4.0, C_wb.getRange().min, precision)
            assertEquals(4.0, C_wb.getRange().max, precision)
            assertEquals(4.0, C_wb_s_floor_arg.getRange().min, precision)
            assertEquals(4.0, C_wb_s_floor_arg.getRange().max, precision)
            assertEquals(3.0, a_pb.getRange().min, precision)
            assertEquals(4.0, a_pb.getRange().max, precision)
            //println("\nEnd of test of Floor Fxn. from Neural Network Example using AADD data type, var is a_pb\n")
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
            val C_w = real(50.0 .. 50.0)
            val F = real(9.0 .. 9.0)
            val s = real(1.0 .. 1.0)
            val p = real(1.0 .. 1.0)// padding, 0 = NOT enabled, 1 = enabled
            val C_wb = floor(F / 2.0)
            val C_w_hat: AADD = C_w + p * 2.0 * C_wb
            val a_w: AADD = floor((C_w_hat - F) / s + 1.0)

            assertEquals(9.0, F.min, precision)
            assertEquals(9.0, F.max, precision)
            assertEquals(1.0, p.min, precision)
            assertEquals(1.0, p.max, precision)
            assertEquals(1.0, s.min, precision)
            assertEquals(1.0, s.max, precision)
            assertEquals(4.0, C_wb.min, precision)
            assertEquals(4.0, C_wb.max, precision)
            assertEquals(57.99999999999999, C_w_hat.min, precision)
            assertEquals(58.00000000000001, C_w_hat.max, precision)
            assertEquals(49.0, a_w.min, precision)
            assertEquals(50.0, a_w.max, precision)
        }
    }

    @Test
    fun testITEBounds(){
        DDBuilder {
            config.lpCallsBeforeOutput=false
            var a : AADD = real(-1.0..1.0)
            IF(a greaterThanOrEquals 0.0)
                a = assign(a, a + 10.0)
            ELSE()
                a = assign(a, a - 10.0)
            END()
            assertEquals(1, a.height())
            assertEquals(-11.0, a.min, 0.000001)
            assertEquals(11.0, a.max, 0.000001)
            //  println(a.toIteString())
            a.getRange()
            assertEquals(-11.0, a.min, 0.000001)
            assertEquals(11.0, a.max, 0.000001)
            // println(a.toIteString())
        }
    }

    @Test
    fun testQuadratic1(){
        DDBuilder {
            //Worst Case for Multiplication
            val x = real(-20.0..20.0, 1.toString())
            val a = x.times(x) as AADD.Leaf
            assertEquals(-400.0, a.min,0.0000001)
            assertEquals(400.0, a.max,0.0000001)
            // will x1 == 0.0 because central value therefore the multiplication plane is zero
            assertEquals(0.0,a.value.xi[1]!!, 0.0000001)
        }
    }

    @Test
    fun reluTestAADD() {
        DDBuilder {
            var a : Real = real(-1.0..1.0)
            val relu_res = a.relu()
            println(relu_res)
            //println((relu_res as AADD.Internal).F.status)
        }
    }

    @Test
    fun testQuadratic2ViaPow(){
        DDBuilder {
            val x = real(0.5..20.0, 1.toString())
            val a = x.pow(2.0) as AADD.Leaf
            assertEquals(9.75, a.value.xi[1]!! ,0.0000001)
            assertEquals(0.25, a.min,0.0000001)
            assertEquals(400.0, a.max,0.0000001)
        }
    }

    @Test
    fun testQuadratic3ViaPow(){
        DDBuilder {
            val x = real(0.5..20.0)
            val a = x.pow(real(2.0)) as AADD.Leaf
            assertEquals(9.75, a.value.xi[1]!! ,0.0000001)
            assertEquals(0.25, a.min,0.0000001)
            assertEquals(400.0, a.max,0.0000001)
        }
    }

    @Test
    fun testQuadratic4ViaPow(){
        DDBuilder {
            val x = real(0.5..20.0 )
            val a = x.power(real(2.0)) as AADD.Leaf
            assertEquals(9.75, a.value.xi[1]!! ,0.0000001)
            assertEquals(0.25, a.min,0.0000001)
            assertEquals(400.0, a.max,0.0000001)
        }
    }

    @Test
    fun testPowAffine(){
        DDBuilder {

            val x = real(0.5..20.0, 2.toString())

            val a = x.power(real(2.0 .. 3.0, 1.toString()))
            assertEquals(0.125, a.min, 0.0000001)
            assertEquals(8000.0, a.max, 0.0000001)
            //assertEquals(9.75,a.value.xi[2])
        }
    }

    @Test
    fun testAbs(){
        DDBuilder {
            val x= real(-2.0 .. 1.0, 1.toString())
            val y= real(-3.0 .. -1.0, 2.toString())

            val decision = x.greaterThan(-1.5)

            val tree = decision.ite(x, y)

            val z = tree.abs()

            val r = z.getRange()

            assertEquals(z.min,0.0,0.0000001)
            assertEquals(z.max,3.0,0.0000001)
            assertEquals(z.numLeaves(),3)

            val rightChild = if(z is AADD.Internal)z.T else NaB

            assertEquals(rightChild.numLeaves(),2)
            val rightleftChild = if(rightChild is AADD.Internal)rightChild.F else NaB
            val rightleftChildValue =  if(rightleftChild is AADD.Leaf) rightleftChild.value else AFEmpty
            assertEquals(rightleftChildValue.min, 0.0,0.0000001)
            assertEquals(rightleftChildValue.max, 1.5,0.0000001)
            assertEquals(rightleftChildValue.xi[1]!!, -1.5,0.0000001)

            val leftiChild = if(z is AADD.Internal)z.F else NaB

            val value = if(leftiChild is AADD.Leaf) leftiChild.value else AFEmpty
            assertEquals(value.min, 1.0,0.0000001)
            assertEquals(value.max,3.0,0.0000001)
            assertEquals(value.xi[2]!!,-1.0,0.0000001)
        }
    }

    @Test
    fun testPowRangeCloseToZero(){
        DDBuilder {
            val x = real(0.1 .. 2.0)
            val z = x.pow(real(1.0 .. 2.0))

            //println("x = " + x.central)
            //println("x = [" + x.min + ", " + x.max + "]")
            //println("y = " + y)
            //println("z = x^y = " + z.central)
            //println("z = x^y = [" + z.min + ", " + z.max + "]")

            //assertEquals(1.95, z.central, tol)
            assertEquals(0.01, z.min, 0.0000001)
            assertEquals(4.0, z.max, 0.0000001)
        }
    }

    /**
     * Testing:
     * pow fxn. for AADDs
     * Test PowFxnAADD1
     */
    @Test
    fun testPowFxnAADD1() {
        DDBuilder {
            val tol = 0.01
            var x = real(2.0..2.0)
            var y = AffineForm(this, 2.0)
            var z = pow(x, y)

            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = " + y)
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")

            assertEquals(4.0, z.getRange().min, tol)
            assertEquals(4.0, z.getRange().max, tol)

            y = AffineForm(this, 3.0)
            z = x.pow(y)

            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = " + y)
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")

            assertEquals(8.0, z.getRange().min, tol)
            assertEquals(8.0, z.getRange().max, tol)

            x = real(3.0..3.0)
            y = AffineForm(this, 3.0)
            z = x.pow(y)

            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = " + y)
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")

            assertEquals(27.0, z.getRange().min, tol)
            assertEquals(27.0, z.getRange().max, tol)

            x = real(3.0..3.0)
            y = AffineForm(this, 0.0)
            z = x.pow(y)

            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = " + y)
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")

            assertEquals(1.0, z.getRange().min, tol)
            assertEquals(1.0, z.getRange().max, tol)

            x = real(3.0..3.0)
            y = AffineForm(this, 1.0)
            z = x.pow(y)

            //println("x = [" + x.getRange().min + ", " + x.getRange().max + "]")
            //println("y = " + y)
            //println("z = [" + z.getRange().min + ", " + z.getRange().max + "]")

            assertEquals(3.0, z.getRange().min, tol)
            assertEquals(3.0, z.getRange().max, tol)
        }
    }
    /**
     * Tests: pow
     */
    @Test
    fun testPowFxn() {
        DDBuilder {
            val a = real(2.0)
            val b = AffineForm(this, 2.0 .. 2.0)
            val c = 3.0

            var d : AADD = pow(a, c)
            //println("d = " + d)
            assertEquals(8.0, d.getRange().min, 0.01)
            assertEquals(8.0, d.getRange().max, 0.01)
            d = pow(a, b)
            //println("d = " + d)
            assertEquals(4.0, d.getRange().min, 0.01)
            assertEquals(4.0, d.getRange().max, 0.01)
        }
    }
    /**
     *
     * */
    @Test
    fun pathConditionTest() {
        DDBuilder {
            val A = real(0.0 .. 0.0) greaterThanOrEquals real(1.0)
            val B = real(0.0 .. 0.0) lessThanOrEquals real(1.0)
            var rangeC: AADD = real(1.0 .. 1.0)
            IF(B)
            IF(A)
            rangeC = assign(rangeC,real(2.0))
            END()
            END()
            assertEquals(rangeC.getRange().min,1.0)
            assertEquals(rangeC.getRange().max,1.0)
        }
    }

    /*
    The next 9 tests are stress tests for any and all Interval and/or Affine Form
    data types.  This stress test was developed by Dr. Siegfried Rump.

    The limit for the diophantine equation 9x^4 - y^4 + 2y^2 = 1.0 was the pair
    (x, y) = (10864.0, 18817.0).  In fact, by 2003, far had exceeded this limit.

    Any floating-point, Interval, or Affine Form data type that cannot perform this calculation correctly is not
    a modern implementation.  Hand-developed data types exist for the following: fixed-point,
    floating-point (i.e. real), and Intervals.  The limit is, as of 2003, into the quadrillions or quintillions for both
    inputs (x, y), somewhere along the 33rd to 35th pair that exist as solutions for said diophantine equation.
    In fact, any data type unable to successfully perform this calculation, at least at the 33rd solution
    pair, is not a modern implementation.  In all actuality, however, if a data-type is not able to generate
    as narrow, or narrower, intervals than that of the hand-coded (2003) data-types, then it is still not a
    modern implementation.  So, while it may satisfy the Rump test, it has a second test it really needs to
    satisfy, that being narrower intervals than those generated in previous art.

    This basic standard must be met.
     */

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     *
     * At this level, AADD successfully passes this Rump test.
     */
    @Test
    fun testAADDAgainstRumpEquation0() {
        DDBuilder {
            val x = real(0.0 ..  0.0)
            val y = real(0.999 .. 1.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)
            z = xintermed - yintermed1 + yintermed2
            //println("z = " + z)
            assertEquals(0.9939880079910001, z.getRange().min, 0.01)
            assertEquals(1.006003992007, z.getRange().max, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     * At this level, AADD successfully passes this Rump test.
     */
    @Test
    fun testAADDAgainstRumpEquation1() {
        DDBuilder {
            val x = real(0.999 ..  1.001)
            val y = real(1.999 .. 2.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)
            z = xintermed - yintermed1 + yintermed2
            assertTrue(1.0 in z)
            //println("z = " + z)
            assertEquals(0.940015960007987, z.getRange().min, 0.01)
            assertEquals(1.060048040008009, z.getRange().max, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     * At this level, AADD fails this Rump test!
     *  There are a couple of things to note here.
     *  (1)  It does not yield the expected value of 1, which all data types must do to pass the Rump test.
     *  (2)  Not only does it fail to yield the value of 1 as a result, the yielded result
     *       bounds zero, thus it is a meaningless result, unless zero was expected, which it was not.
     *       [-2.5074841679942343, 4.508628168009962] does NOT bound the number 1!
     *       This is well below the i = 8 threshold with (x, y) = (10864, 18817).
     *       It is even more below the modern (2003) threshold for i = 33.
     */
    @Test
    fun testAADDAgainstRumpEquation2() {
        DDBuilder {
            val x = real(3.999 ..  4.001)
            val y = real(6.999 .. 7.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)
            z = xintermed - yintermed1 + yintermed2
            println("z = " + z)
            assertTrue(1.0 in z)
            //assertEquals(-2.5074841679942343, z.getRange().min, 0.01)
            //assertEquals(4.508628168009962, z.getRange().max, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     *
     * At this level, AADD fails this Rump test!
     *
     * See: disccusion for test number 2, above
     *
     * In addition, it can be seen that, not only does the interval bound zero, the interval
     * is getting wider as the inputs get wider.  Inputs are: (x, y) = (15, 26)
     */
    @Test
    fun testAADDAgainstRumpEquation3() {
        DDBuilder {
            val x = real(14.999 ..  15.001)
            val y = real(25.999 .. 26.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)
            z = xintermed - yintermed1 + yintermed2
            assertTrue(1.0 in z)
            //println("z = " + z)
            // assertEquals(-188.1961126405928, z.getRange().min, 0.01)
            // assertEquals(190.21230464242183, z.getRange().max, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     *
     * At this level, AADD fails this Rump test!
     *
     * Again, as in the discussion for test number 3, the result is widening here.
     * Input is: (x, y) = (56, 97)
     */
    @Test
    fun testAADDAgainstRumpEquation4() {
        DDBuilder {
            val x = real(55.999 ..  56.001)
            val y = real(96.999 .. 97.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)

            z = xintermed - yintermed1 + yintermed2
            assertTrue(1.0 in z)

            //println("z = " + z)
            // assertEquals(-9934.50788718558, z.getRange().min, 0.01)
            // assertEquals(9936.73367101702, z.getRange().max, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     *
     * At this level, AADD fails this Rump test!
     *
     * Again, as in the discussion for test number 3, the result is widening here.
     * Input is: (x, y) = (209, 362)
     */
    @Test
    fun testAADDAgainstRumpEquation5() {
        DDBuilder {
            val x = real(208.999 ..  209.001)
            val y = real(361.999 .. 362.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)
            z = xintermed - yintermed1 + yintermed2
            assertTrue(1.0 in z)
            //println("z = " + z)
            // assertEquals(-517882.25827537477, z.getRange().min, 0.01)
            // assertEquals(517887.40354304016, z.getRange().max, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     * x = range(779.999 ..  780.001)
     * y = range(1350.999 .. 1351.001)
     *
     * At this level, AADD fails this Rump test!
     *
     * Again, as in the discussion for test number 3, the result is widening here.
     * Input is: (x, y) = (780, 1351)
     */
    @Test
    fun testAADDAgainstRumpEquation6() {
        DDBuilder {
            val x = real(779.999 ..  780.001)
            val y = real(1350.999 .. 1351.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)
            z = xintermed - yintermed1 + yintermed2
            assertTrue(1.0 in z)
            //println("z = " + z)
            // assertEquals(-2.693994029393387E7, z.getRange().min, 0.01)
            // assertEquals(2.6939986099601746E7, z.getRange().max, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     * dVar("x", 2910.999 .. 2911.001)
     * dVar("y", 5041.999 .. 5042.001)
     *
     * At this level, AADD fails this Rump test!
     *
     * Again, as in the discussion for test number 3, the result is widening here.
     * Input is: (x, y) = (2911, 5042)
     */
    @Test
    fun testAADDAgainstRumpEquation7() {
        DDBuilder {
            val x = real(2910.999 .. 2911.001)
            val y = real(5041.999 .. 5042.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)
            z = xintermed - yintermed1 + yintermed2
            assertTrue(1.0 in z)
            //println("z = " + z)
            // assertEquals(-1.4006373204736328E9, z.getRange().min, 0.01)
            // assertEquals(1.4006377912236328E9, z.getRange().max, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     * dVar("x", 10863.999 .. 10864.001)
     * dVar("y", 18816.999 .. 18817.001)
     *
     * Again, as in the discussion for test number 3, the result is widening here.
     * Input is: (x, y) = (10864, 18817)
     *
     * At this level, AADD fails this Rump test!
     */
    @Test
    fun testAADDAgainstRumpEquation8() {
        DDBuilder {
            val x = real(10863.999 .. 10864.001)
            val y = real(18816.999 .. 18817.001)
            val nine = real(9.0)
            val two = real(2.0)
            val z : AADD

            val xintermed : AADD = nine * pow(x, 4.0)
            val yintermed1 : AADD = pow(y, 4.0)
            val yintermed2 : AADD = two * pow(y, 2.0)
            z = xintermed - yintermed1 + yintermed2
            //println("z = " + z)
            assertTrue(1.0 in z)
            // assertEquals(-7.281044418696875E10, z.getRange().min, 0.01)
            // assertEquals(7.281045273496875E10, z.getRange().max, 0.01)
        }
    }

    /* End of stress tests designed by Dr. Rump */
}