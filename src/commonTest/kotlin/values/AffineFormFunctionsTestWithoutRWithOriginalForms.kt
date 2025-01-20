package examples.values

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.AffineForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import com.github.tukcps.aadd.values.log


class AffineFormFunctionsTestWithoutRWithOriginalForms {

    /**
     * The affine forms are enclosed in an IA result.
     * We check that it is computed correctly using IA.
     */
    @Test
    fun expTestIA() {
        DDBuilder {
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag =true
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
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag =true
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
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag =true
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
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag =true
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
            config.originalFormsFlag = true
            config.noiseSymbolsFlag = true
            val a = AffineForm(this, 2.0..3.0, "1")
            val b = 2.0
            val c = AffineForm(this, 3.0..3.0, "1")
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
            this.config.originalFormsFlag = true
            this.config.noiseSymbolsFlag =true
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
}