package aaddtests

import com.github.tukcps.aadd.*
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.functions.log
import com.github.tukcps.aadd.functions.pow
import com.github.tukcps.aadd.values.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.math.exp
import kotlin.math.ln

/**
 * @class AADDTest
 * Test suite for AADD data type
 */
internal class AADDTestsWithoutRWithOriginalForms {

    @Test
    // Check if infeasible paths are detected and leaves are marked.
    fun infeasibilityTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
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
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true
            val a= this.real(1.0..3.0, "i")
            val b= this.real(-1.0..1.0, "i")
            val c1=a greaterThan 2.0
            val index_c1 = c1.index
            assertEquals(1, c1.height())
            val c2=b greaterThan 0.0
            val index_c2 = c2.index
            assertTrue(index_c1<index_c2)
            assertEquals(1, c2.height())
            val d=c1.ite(a*2.0, b+1.0)
            assertEquals(index_c1, d.index)
            val e=c2.ite(d, a+b)
            assertEquals(index_c1, e.index)
        }
    }


    /** Subtraction computed as in affine arithmetic / interval arithmetic, and minimum assigned to range */
    @Test
    fun minusTest1() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val a: AADD = Reals
            val b: AADD = real(2.0 .. 3.0)
            val c = a-b
            //println("c=$c")
            assertEquals(Reals.min, c.getRange().min)
        }
    }


    @Test
    // Check if negation works for Scalar: value negates is - value.
    fun negate() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val a = real(10.0)
            val r = a.negate()
            assertEquals(-10.0, r.getRange().min, 0.0001)

            // Check if negation works for AADD: value + negated value = 0
            val cond = conds.newConstraint(AffineForm(this, 1.0..2.0, 3), "")
            val t = internal(cond, a, r)
            val tn = t.negate()
            val s = tn.plus(t)
            assertEquals(0.0, s.getRange().min, 0.00000001)
            assertEquals(0.0, s.getRange().max, 0.00000001)
            // This assertion or the above assertions fail if merging when considering the quantization error prevents merge.
            assertTrue(s is AADD.Leaf)
        }
    }

    @Test
    fun plusTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val a = real(10.0)
            val b = real(1.0)
            val r = a.plus(b)
            assertEquals(r.getRange().min, 11.0, 0.001)
        }
    }

    @Test
    fun multTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val a = real(10.0)
            val b = real(3.0) as AADD.Leaf
            val r = a.times(b) as AADD.Leaf
            assertEquals(30.0, r.central)
        }
    }

    @Test
    fun expTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val largerValue = AffineForm(this, Range.Reals, 10.0, 0.0, terms)
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
            assertEquals(0.0, exp3.value.r, 0.01)
            assertEquals(2.72, exp3.min, 0.01)
            assertEquals(7.39, exp3.max, 0.01)
            val d = leaf(largerValue)
            val exp4 = d.exp() as AADD.Leaf
            assertEquals(221755.0, exp4.value.central, 1.0)
            assertEquals(0.0, exp4.value.r, 1.0)
            assertEquals(1097.0, exp4.value.min, 1.0)
            assertEquals(442413.0, exp4.value.max, 1.0)
        }
    }

    /**
     * Tests: e^1 & e ^ -1
     */
    @Test
    fun testOfEulersNumber() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val a = real(1.0)
            val b = real(-1.0)
            val c = real(0.0)

            var d : AADD = a.exp()
            assertEquals(2.7182818284590446, d.getRange().min, 0.001)
            assertEquals(2.7182818284590446, d.getRange().max, 0.001)
            d = b.exp()
            assertEquals(0.3678794411714423, d.getRange().min, 0.001)
            assertEquals(0.3678794411714423, d.getRange().max, 0.001)
            d = c.exp()
            assertEquals(1.0, d.getRange().min, 0.001)
            assertEquals(1.0, d.getRange().max, 0.001)
        }
    }

    @Test
    fun sqrtTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val largerValue = AffineForm(this, Range.Reals, 10.0, 0.0, terms)
            val affineForm1 = AffineForm(this, 1.0 .. 2.0)
            terms = HashMap()
            terms[2] = 0.5
            val restrictedRange = AffineForm(this, 1.1 .. 1.9, 1.5, 0.0, terms)
            // Test real
            val a = leaf(affineForm1)
            val sqrt1 = a.sqrt() as AADD.Leaf
            assertEquals(1.2071, sqrt1.central, 0.01)
            assertEquals(0.0, sqrt1.r, 0.01)
            assertEquals(1.0, sqrt1.min, 0.01)
            assertEquals(kotlin.math.sqrt(2.0), sqrt1.max, 0.01)
            val b = leaf(largerValue)
            val sqrt2 = b.sqrt() as AADD.Leaf
            assertEquals(3.13, sqrt2.central, 0.01)
            assertEquals(0.0, sqrt2.r, 0.01)
            assertEquals(kotlin.math.sqrt(7.0), sqrt2.min, 0.01)
            assertEquals(kotlin.math.sqrt(13.0), sqrt2.max, 0.01)
            val c = leaf(restrictedRange)
            val sqrt3 = c.sqrt() as AADD.Leaf
            assertEquals(1.21, sqrt3.central, 0.01)
            assertEquals(0.0, sqrt3.r, 0.01)
            assertEquals(1.0, sqrt1.min, 0.001)
            assertEquals(kotlin.math.sqrt(a.max), sqrt1.max, 0.001)
        }
    }

    @Test
    fun logTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            scheme=DDBuilder.ApproximationScheme.Chebyshev
            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val largerValue = AffineForm(this, Range.Reals, 10.0, 0.0, terms)
            val affineForm1 = AffineForm(this, 1.0 .. 2.0)
            terms = HashMap()
            terms[2] = 0.5
            val restrictedRange = AffineForm(this, 1.1 .. 1.9, 1.5, 0.0, terms)

            val a = leaf(affineForm1)
            val log1 = a.log() as AADD.Leaf
            assertEquals(0.38, log1.central, 0.01)
            assertEquals(0.0, log1.r, 0.01)
            assertEquals(0.0, log1.min, 0.01)
            assertEquals(ln(a.max), log1.max, 0.01)
            val b = leaf(largerValue)
            val log2 = b.log() as AADD.Leaf
            assertEquals(2.28, log2.central, 0.01)
            assertEquals(0.0, log2.r, 0.001)
            assertEquals(ln(b.min), log2.min, 0.01)
            assertEquals(ln(b.max), log2.max, 0.01)
            val c = leaf(restrictedRange)
            val log3 = c.log() as AADD.Leaf
            assertEquals(0.39, log3.central, 0.01)
            assertEquals(0.0, log3.value.r, 0.01)
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
     * i.e., a generalized logarithm function
     */
    @Test
    fun testLogBaseFxn() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val a = leaf(AffineForm(this, 1.0..1.0))
            var b = log(10.0, a)
            assertEquals(0.0, b.getRange().min, 0.001)
            assertEquals(0.0, b.getRange().max, 0.001)
            b = log(10.0, leaf(AffineForm(this, 100.0..100.0)))
            assertEquals(2.0, b.getRange().min, 0.001)
            assertEquals(2.0, b.getRange().max, 0.001)
            b = log(5.0, leaf(AffineForm(this, 100.0..100.0)))
            assertEquals(ln(100.0) / ln(5.0), b.getRange().min, 0.001)
            assertEquals(ln(100.0) / ln(5.0), b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 128.0..128.0)))
            assertEquals(ln(128.0) / ln(2.0), b.getRange().min, 0.001)
            assertEquals(ln(128.0) / ln(2.0), b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 1.5..1.5)))
            assertEquals(0.5849625007211561, b.getRange().min, 0.001)
            assertEquals(0.5849625007211561, b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 10.5..10.5)))
            assertEquals(3.39231742277876, b.getRange().min, 0.001)
            assertEquals(3.39231742277876, b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 8.0..8.0)))
            assertEquals(3.0, b.getRange().min, 0.001)
            assertEquals(3.0, b.getRange().max, 0.001)
            b = log(2.0, leaf(AffineForm(this, 16.0..16.0)))
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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val af = real(1.0..5.0)
            assertEquals(ln(1.0), af.log().getRange().min, 0.0000001)
            assertEquals(ln(5.0), af.log().getRange().max, 0.0000001)
        }
    }

    @Test
    fun powerTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true

            val affineForm1 = AffineForm(this, 1.0..2.0)
            // Including zero; division by Zero should result in infinity
            val af3Node = real(-2.0..2.0)
            val inv = af3Node.inv() as AADD.Leaf
            // //println("inv=$inv")
            assertTrue(inv.value.isReals())

            // Infinity should be preserved
            val reals = Reals.inv() as AADD.Leaf
            assertTrue(reals.value.isReals())

            // empty should be preserved
            val empty = Empty.inv() as AADD.Leaf
            assertTrue(empty.value.isEmpty())

            // Regular
            val inv3Node = leaf(affineForm1)
            val inv3 = inv3Node.inv() as AADD.Leaf
            assertEquals(0.75, inv3.central, 0.0000001)
            assertEquals(0.5, inv3.min, 0.0000001)
            assertEquals(1.0, inv3.max, 0.0000001)
            assertEquals(0.0, inv3.r, 0.0000001)
            assertEquals(0.25, inv3.radius, 0.0000001)
        }
    }

    @Test
    fun divTest() {
        // Div by zero should return infinite
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val affineForm1 = AffineForm(this, 1.0 .. 2.0)
            val zeroNode = real(0.0)
            val affineform1Node = leaf(affineForm1)
            val div = affineform1Node.div(zeroNode) as AADD.Leaf
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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            // Two BDD with different index.
            val ai = this.real(-1.0..1.0)
            val tr = real(0.1)
            val tr2 = real(0.2)
            val c1 = ai greaterThanOrEquals tr
            val c2 = ai.lessThanOrEquals(tr2)
            assertTrue(c1.index != c2.index)
            val a = real(1.0)
            val b = real(2.0)
            val d = c1.ite(a, b)
            assertTrue(c1.index == d.index)
            assertTrue(c1.height() == 1)
            val e = c2.ite(a, b)
            assertTrue(c2.index == e.index)
            assertTrue(e.height() == c2.height())
            val f = e.plus(d)
            assertTrue(f.height() == 2)
        }
    }

    /**
     * ITE of an AADD leave with a BDD of height 1 must result in merged AADD with height 1.
     */
    @Test fun iteTest2() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            var a: AADD = this.real(-1.0..1.0)
            val b = real(0.0)
            val c = a.greaterThanOrEquals(b)
            assertEquals(1, c.height())
            a = c.ite(a, b)
            assertEquals(1, a.height())
        }
    }

    @Test fun reduceTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val ai = this.real(-1.0..1.0, "1")
            val tr = real(0.1)
            val tr2 = real(0.2)
            val c1 = ai.greaterThanOrEquals(tr)
            val c2 = ai.lessThanOrEquals(tr2)
            val a = this.real(1.0..2.0, "1")
            val b = real(2.0..3.0, "1")
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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
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
     * pow fxn. for AADDs
     * Test PowFxnAADD1
     */
    @Test
    fun testPowFxnAADD1() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val tol = 0.01
            var x = real(2.0..2.0)
            var y = AffineForm(this, 2.0)
            var z = pow(x, y)

            assertEquals(4.0, z.getRange().min, tol)
            assertEquals(4.0, z.getRange().max, tol)

            y = AffineForm(this, 3.0)
            z = x.pow(y)

            assertEquals(8.0, z.getRange().min, tol)
            assertEquals(8.0, z.getRange().max, tol)

            x = real(3.0..3.0)
            y = AffineForm(this, 3.0)
            z = x.pow(y)
            assertEquals(27.0, z.getRange().min, tol)
            assertEquals(27.0, z.getRange().max, tol)

            x = real(3.0..3.0)
            y = AffineForm(this, 0.0)
            z = x.pow(y)

            assertEquals(1.0, z.getRange().min, tol)
            assertEquals(1.0, z.getRange().max, tol)

            x = real(3.0..3.0)
            y = AffineForm(this, 1.0)
            z = x.pow(y)

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
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val a = real(2.0)
            val b = AffineForm(this, 2.0..2.0, 1)
            val c = 3.0

            var d : AADD = pow(a, c)
            assertEquals(8.0, d.getRange().min, 0.001)
            assertEquals(8.0, d.getRange().max, 0.001)
            d = pow(a, b)
            assertEquals(4.0, d.getRange().min, 0.001)
            assertEquals(4.0, d.getRange().max, 0.001)
        }
    }
    /**
     *
     * */
    @Test
    fun pathConditionTest() {
        DDBuilder {
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true
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
}