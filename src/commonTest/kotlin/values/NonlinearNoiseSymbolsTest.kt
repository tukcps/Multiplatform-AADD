package examples.values

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.Range
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NonlinearNoiseSymbolsTest {
    private val precision = 0.00001

    @Test
    fun powAffineTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.roundingErrorMappingFlag = true
            config.reductionFlag = true
            val af1 = AffineForm(this, 1.0..2.0)
            val b = af1.pow(af1)
            assertTrue(b.r.equals(0.0))
        }
    }
    @Test
    fun powDoubleTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.roundingErrorMappingFlag = true
            config.reductionFlag = true
            val af1 = AffineForm(this, 1.0..2.0)
            val b = af1.pow(2.0)
            val c = af1.pow(2.0)
            val d = b - c
            assertEquals(0.0, d.radius, precision)
        }
    }

    @Test
    fun powDoubleTest2(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.roundingErrorMappingFlag = true
            config.reductionFlag = true
            val af1 = AffineForm(this, -2.0..-1.0)
            val b = af1.pow(-2.0)
            val c = af1.pow(-2.0)
            val d = b - c
            assertEquals(0.0, d.radius, precision)
        }
    }
    @Test
    fun usageCounterTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.roundingErrorMappingFlag = true
            config.reductionFlag = true
            val af1 = AffineForm(this, 1.0..2.0)
            val a = af1.log()
            val b = af1.log()
            val y = a.plus(b)
            val x = a.plus(b)
            val z = y - x
            val c = noiseVars.getUsed()
            assertEquals(6, c)
            assertEquals(0.0, z.radius, precision)
        }
    }
    @Test
    fun thresholdTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.roundingErrorMappingFlag = true
            config.reductionFlag = true
            config.thresholdFlag = true
            val af1 = AffineForm(this, 1.0..2.0)
            val a = af1.log()
            val b = af1.log()
            val y = a.plus(b)
            val x = a.plus(b)
            val z = y - x
            val c = noiseVars.getUsed()
            assertEquals(6, c)
            assertEquals(0.0, z.radius, precision)
        }
    }

    /**
     * Tests that the invariant of affine arithmetic still holds without the r-term
     */
    @Test
    fun boundsTest1(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 1.0..2.0)
            val af2 = AffineForm(this, 1.0..2.0)
            val a = af1.times(af2)
            val al = 1 * 1
            val au = 2 * 2
            val b = af1.times(af2).times(0.5)
            val bl = 1*1*0.5
            val bu = 2*2*0.5
            val y = a.minus(b)
            val zl = al-bl
            val zu = au-bu
            assertTrue(y.min<=zl && y.max>=zu)
        }
    }
    /**
     * Tests that the invariant of affine arithmetic still holds without the r-term
     */
    @Test
    fun boundsTest2(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            val af1 = AffineForm(this, 1.0..2.0)
            val af2 = AffineForm(this, 1.0..2.0)
            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val a = lgr.times(af1)
            val al = 7.0 * 1.0
            val au = 13.0 * 2.0
            assertTrue(a.min<=al && a.max>=au)
            val b = a.inv()
            val bl = 1/au
            val bu = 1/al
            assertTrue(b.min<=bl && b.max>=bu)
            val c = b.exp()
            val cl = exp(bl)
            val cu = exp(bu)
            assertTrue(c.min<=cl && c.max>=cu)
            val d = c.log()
            val dl = ln(cl)
            val du = ln(cu)
            assertTrue(d.min<=dl && d.max>=du)
            val e = d.times(2.0)
            val el = dl.times(2.0)
            val eu = du.times(2.0)
            assertTrue(e.min<=el && e.max>=eu)
            val f = e.div(af2)
            val fu = eu.div(1.0)
            val fl = el.div(2.0)
            val difMax = f.max-fu
            val difMin = fl - f.min
            assertEquals(0.0, difMax, precision)
            assertEquals(0.0, difMin, precision)
            assertTrue(f.min<=fl && f.max>=fu)
        }
    }
    /**
     * Tests that the invariant of affine arithmetic still holds without the r-term
     */
    @Test
    fun boundsTest3(){
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)
            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals, 10.0, 0.0, terms)

            val a = lgr.times(-af1)
            val au = 7.0 * -1.0
            val al = 13.0 * -2.0
            assertTrue(a.min<=al && a.max>=au)
            val b = a.inv()
            val bl = 1/au
            val bu = 1/al
            assertTrue(b.min<=bl && b.max>=bu)
            val c = b.exp()
            val cl = exp(bl)
            val cu = exp(bu)
            assertTrue(c.min<=cl && c.max>=cu)
            val d = c.log()
            val dl = ln(cl)
            val du = ln(cu)
            assertTrue(d.min<=dl && d.max>=du)
            val e = d.times(-2.0)
            val eu = dl.times(-2.0)
            val el = du.times(-2.0)
            assertTrue(e.min<=el && e.max>=eu)
            val f = e.div(af2)
            val fu = eu.div(1.0)
            val fl = el.div(2.0)
            val difMax = f.max-fu
            val difMin = fl - f.min
            assertEquals(0.0, difMax, precision)
            assertEquals(0.0, difMin, precision)
            assertTrue(f.min<=fl && f.max>=fu)
        }
    }

    /**
     * Tests the correct mapping of the error of nonlinear functions.
     */
    @Test
    fun nonlinearMappingLnTest() {
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0 .. 2.0, 1)
            val a = af1.log()
            val b = af1.log()
            val y = a.minus(b)
            assertEquals(0.0, y.radius, 0.000001)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions.
     */
    @Test
    fun nonlinearMappingLnTest2() {
        DDBuilder{

            config.noiseSymbolsFlag = true

            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val a = lgr.log()
            val b = lgr.log()
            val y = a.minus(b)
            assertEquals(0.0, y.radius, 0.000001)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions.
     */
    @Test
    fun nonlinearMappingExpTest() {
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af2 = AffineForm(this, 1.0..2.0, 2)
            val a = af2.exp()
            val b = af2.exp()
            val y = a - b
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions.
     */
    @Test
    fun nonlinearMappingExpTest2() {
        DDBuilder{

            config.noiseSymbolsFlag = true

            val terms = HashMap<Int, Double>()
            terms[2] = 0.5
            val rst = AffineForm(this, Range(1.1, 1.9), 1.5, 0.0, terms)

            val a = rst.exp()
            val b = rst.exp()
            val y = a - b
            assertEquals(0.0, y.radius, precision)
        }
    }

    @Test
    fun nonlinearMappingInvTest() {
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0 .. 2.0, 1)
            val a = af1.inv()
            val b = af1.inv()
            val y = a - b
            assertEquals(0.0, y.radius, 0.000001)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions.
     */
    @Test
    fun nonlinearMappingInvTest2() {
        DDBuilder{

            config.noiseSymbolsFlag = true

            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val a = lgr.inv()
            val b = lgr.inv()
            val y = a - b
            assertEquals(0.0, y.radius, 0.000001)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions.
     */
    @Test
    fun nonlinearMappingTimesTest() {
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)

            val a = af1.times(af2)
            val b = af1.times(af2)
            val y = a - b
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions.
     */
    @Test
    fun advancedInvTimesTest() {
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val scl = AffineForm(this, 2.0)
            val a = af1.inv().times(scl)
            val b = af1.inv()
            val y = a - b - b
            assertEquals(0.0, y.radius, precision)
        }
    }

    /**
     * The duplicated tests check that the original forms flag does not negatively impact the previous tests.
     */

    /**
     * Tests that the invariant of affine arithmetic still holds without the r-term and with the mapping of original forms.
     */
    @Test
    fun boundsTest1Duplicated(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true
            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)
            val a = af1.times(af2)
            val al = 1 * 1
            val au = 2 * 2
            val b = af1.times(af2).times(0.5)
            val bl = 1*1*0.5
            val bu = 2*2*0.5
            val y = a.minus(b)
            val zl = al-bl
            val zu = au-bu
            assertTrue(y.min<=zl && y.max>=zu)
        }
    }
    /**
     * Tests that the invariant of affine arithmetic still holds without the r-term and with the mapping of original forms.
     */
    @Test
    fun boundsTest2Duplicated(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)
            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val a = lgr.times(af1)
            val al = 7.0 * 1.0
            val au = 13.0 * 2.0
            assertTrue(a.min<=al && a.max>=au)
            val b = a.inv()
            val bl = 1/au
            val bu = 1/al
            assertTrue(b.min<=bl && b.max>=bu)
            val c = b.exp()
            val cl = exp(bl)
            val cu = exp(bu)
            assertTrue(c.min<=cl && c.max>=cu)
            val d = c.log()
            val dl = ln(cl)
            val du = ln(cu)
            assertTrue(d.min<=dl && d.max>=du)
            val e = d.times(2.0)
            val el = dl.times(2.0)
            val eu = du.times(2.0)
            assertTrue(e.min<=el && e.max>=eu)
            val f = e.div(af2)
            val fu = eu.div(1.0)
            val fl = el.div(2.0)
            val difMax = f.max-fu
            val difMin = fl - f.min
            assertEquals(0.0, difMax, precision)
            assertEquals(0.0, difMin, precision)
            assertTrue(f.min<=fl && f.max>=fu)
        }
    }
    /**
     * Tests that the invariant of affine arithmetic still holds without the r-term and with the mapping of original forms.
     */
    @Test
    fun boundsTest3Duplicated(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0.. 2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)
            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals, 10.0, 0.0, terms)

            val a = lgr.times(-af1)
            val au = 7.0 * -1.0
            val al = 13.0 * -2.0
            assertTrue(a.min<=al && a.max>=au)
            val b = a.inv()
            val bl = 1/au
            val bu = 1/al
            assertTrue(b.min<=bl && b.max>=bu)
            val c = b.exp()
            val cl = exp(bl)
            val cu = exp(bu)
            assertTrue(c.min<=cl && c.max>=cu)
            val d = c.log()
            val dl = ln(cl)
            val du = ln(cu)
            assertTrue(d.min<=dl && d.max>=du)
            val e = d.times(-2.0)
            val eu = dl.times(-2.0)
            val el = du.times(-2.0)
            assertTrue(e.min<=el && e.max>=eu)
            val f = e.div(af2)
            val fu = eu.div(1.0)
            val fl = el.div(2.0)
            val difMax = f.max-fu
            val difMin = fl - f.min
            assertEquals(0.0, difMax, precision)
            assertEquals(0.0, difMin, precision)
            assertTrue(f.min<=fl && f.max>=fu)
        }
    }


    /**
     * Tests the correct mapping of the error of nonlinear functions when the original forms flag is set.
     */
    @Test
    fun nonlinearMappingExpTestDuplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af2 = AffineForm(this, 1.0..2.0, 2)
            val a = af2.exp()
            val b = af2.exp()
            val y = a - b
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions when the original forms flag is set.
     */
    @Test
    fun nonlinearMappingExpTest2Duplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val terms = HashMap<Int, Double>()
            terms[2] = 0.5
            val rst = AffineForm(this, Range(1.1, 1.9), 1.5, 0.0, terms)

            val a = rst.exp()
            val b = rst.exp()
            val y = a - b
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions when the original forms flag is set.
     */
    @Test
    fun nonlinearMappingInvTestDuplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val a = af1.inv()
            val b = af1.inv()
            val y = a - b
            assertEquals(0.0, y.radius, 0.000001)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions when the original forms flag is set.
     */
    @Test
    fun nonlinearMappingInvTest2Duplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag =true

            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals,10.0, 0.0, terms)

            val a = lgr.inv()
            val b = lgr.inv()
            val y = a - b
            assertEquals(0.0, y.radius, 0.000001)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions when the original forms flag is set.
     */
    @Test
    fun nonlinearMappingTimesTestDuplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)

            val a = af1.times(af2)
            val b = af1.times(af2)
            val y = a - b
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions when the original forms flag is set.
     */
    @Test
    fun advancedInvTimesTestDuplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val scl = AffineForm(this, 2.0)
            val a = af1.inv().times(scl)
            val b = af1.inv()
            val y = a - b - b
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions when the original forms flag is set.
     */
    @Test
    fun nonlinearMappingLnTestDuplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val a = af1.log()
            val b = af1.log()
            val y = a.minus(b)
            assertEquals(0.0, y.radius, 0.000001)
        }
    }
    /**
     * Tests the correct mapping of the error of nonlinear functions when the original forms flag is set.
     */
    @Test
    fun nonlinearMappingLnTest2Duplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            val lgr = AffineForm(this, Range.Reals, 10.0, 0.0, terms)

            val a = lgr.log()
            val b = lgr.log()
            val y = a.minus(b)
            assertEquals(0.0, y.radius, 0.000001)
        }
    }

    /**
     * Tests for different scenarios of the reduceNoiseSymbols function
     */

    /**
     * Tests the reduceNoiseSymbols function, when the maxSize is not exceeded.
     */
    @Test //maxSize is not exceeded, so reduceNoiseSymbols() should not change the number of noise variables
    fun reduceNoiseSymbolsTest200(){
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)

            repeat (199) {
                af1.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
            }
            af1.reduceNoiseSymbols()
            assertEquals(200, af1.xi.size)
        }
    }
    /**
     * Tests the reduceNoiseSymbols function, when the maxSize is exceeded and the merging process should be performed once.
     */
    @Test //maxSize is exceeded, so the merging process should be performed once
    fun reduceNoiseSymbolsTest201(){
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)

            repeat (200) {
                af1.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
            }
            af1.reduceNoiseSymbols()
            val size = af1.xi.size
            assertEquals(192, size)
        }
    }
    /**
     * Tests the reduceNoiseSymbols function, when the maxSize is exceeded and the merging process should be performed more than once.
     */
    @Test //maxSize is exceeded, so the merging process should be performed three times
    fun reduceNoiseSymbolsTest220(){
        DDBuilder{

            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)

            repeat(219) {
                af1.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
            }
            af1.reduceNoiseSymbols()
            assertEquals(193, af1.xi.size)
        }
    }
    /**
     * Tests the reduceNoiseSymbols function, when the maxSize is exceeded, but there are no garbageVars to reduce.
     */
    @Test //no garbageVars to reduce
    fun reduceNoiseSymbolsTestNoGarbageVars(){
        DDBuilder{

            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1) //symbol 1 is overwritten in the for loop, as newNoiseVar() starts at 1
            repeat (201){
                af1.xi[af1.builder.noiseVars.newNoiseVar()] = 1.0
            }
            af1.reduceNoiseSymbols()
            assertEquals(201, af1.xi.size)
        }
    }
    /**
     * Tests the reduceNoiseSymbols function, when the maxSize is exceeded and there are noiseVars, garbageVars and garbageVars with mapping.
     */
    @Test //check the right prioritisation
    fun reduceNoiseSymbolsPrioritisationTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true
            config.maxSymbols = 200
            config.mergeSymbols = 10

            val af1 = AffineForm(this, 1.0..2.0, 1)  //TODO: maxIndex of Noise variables is not increased?
            val af2 = AffineForm(this, 1.0..2.0, 2)
            val newGarbageKey = af1.builder.noiseVars.newGarbageVar(AffineForm.GarbageVarMapping.TIMES, af1, af2)
            af1.xi[newGarbageKey] = 0.5
            repeat (100 ) {
                af1.xi[af1.builder.noiseVars.newNoiseVar()] = 0.4
            }
            repeat (100) {
                af1.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
            }
            af1.reduceNoiseSymbols()
            assertEquals(192,af1.xi.size)
            assertEquals(0.5, af1.xi[newGarbageKey])
        }
    }
    /**
     * Tests the reduceNoiseSymbols function, after the multiplication of two affine forms.
     * Multiplication introduces 2 new noise symbols, so there are 402 symbols before the reduction.
     * So after 23 reduction steps, which reduce the number of symbols by 9, there should be 195 symbols left.
     */
    @Test
    fun reduceNoiseSymbolsMultiplicationTest(){
        DDBuilder{

            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true
            config.maxSymbols = 200
            config.mergeSymbols = 10

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)
            repeat(50) {
                af1.xi[af1.builder.noiseVars.newNoiseVar()] = 0.4
                af2.xi[af1.builder.noiseVars.newNoiseVar()] = 0.4
            }
            repeat(150) {
                af1.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
                af2.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
            }
            assertEquals(200, af1.xi.size)
            assertEquals(200, af2.xi.size)
            val result = af1.times(af2)
            assertTrue(result.xi.size <= config.maxSymbols)
        }
    }
    /**
     * Tests the reduceNoiseSymbols function, after the addition of two affine forms.
     * As an addition only introduces one additional noise symbol for round-off errors, there are 401 symbols before the reduction.
     * After 23 reduction steps, which reduce the number of symbols by 9, there should be 194 symbols left.
     */
    @Test
    fun reduceNoiseSymbolsAdditionTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true
            config.maxSymbols = 200
            config.mergeSymbols = 10

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val af2 = AffineForm(this, 1.0..2.0, 2)
            repeat (50) {
                af1.xi[af1.builder.noiseVars.newNoiseVar()] = 0.4
                af2.xi[af1.builder.noiseVars.newNoiseVar()] = 0.4
            }
            repeat (150) {
                af1.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
                af2.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
            }
            assertEquals(200, af1.xi.size)
            assertEquals(200, af2.xi.size)
            val result = af1.plus(af2)
            assertTrue(result.xi.size <= config.maxSymbols)
        }
    }
    /**
     * Tests the reduceNoiseSymbols function, after the inversion of an affine form.
     * As inversion introduces 2 new noise symbols, there are 202 symbols before the reduction.
     * After 1 reduction steps, which reduce the number of symbols by 9, there should be 193 symbols left.
     */
    @Test
    fun reduceNoiseSymbolsInverseTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true
            config.maxSymbols = 200
            config.mergeSymbols = 10

            val af1 = AffineForm(this, 1.0..2.0, 1)
            repeat (50) {
                af1.xi[af1.builder.noiseVars.newNoiseVar()] = 0.4
            }
            repeat (150) {
                af1.xi[af1.builder.noiseVars.newGarbageVar()] = 1.0
            }
            assertEquals(200, af1.xi.size)
            val result = af1.inv()
            assertTrue(result.xi.size <= config.maxSymbols)
        }
    }

    /**
     * This test checks that the invariant of affine arithmetic is still fulfilled, even when the original forms flag is not set
     * and the optimal solution with a range of 0 is not achieved.
     */
    @Test
    fun invTimesScalarTestDuplicated() {
        DDBuilder{
            config.noiseSymbolsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val scl = AffineForm(this, 2.0)

            val a = af1.times(scl).inv()                  // * 1/2
            val b = af1.inv()
            val y = b - a - a
            val upperA = 1/ (af1.min * 2)
            val lowerA = 1/ (af1.max * 2)
            val upperB = 1/ af1.min
            val lowerB = 1/af1.max
            val upperBound = upperB - upperA - upperA
            val lowerBound = lowerB - lowerA - lowerA

            assertEquals(true, y.max>=upperBound && y.min<=lowerBound)
        }
    }

    /** The following tests only make sense, when builder.config.originalFormsFlag is true.
     * They test the correct handling of scalar dependencies between times and inv operations, when the flag is set.*/

    /**
     * This test checks that the invariant of affine arithmetic is still fulfilled, when the original forms flag is set
     * and tests the correct mapping of original forms (scalar dependencies).
     */
    @Test
    fun invTimesScalarTest() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0, 1)
            val scl = AffineForm(this, 2.0)

            val a = af1.times(scl).inv()                  // * 1/2
            val b = af1.inv()
            val y = b - a - a
            val upperA = 1/ (af1.min * 2)
            val lowerA = 1/ (af1.max * 2)
            val upperB = 1/ af1.min
            val lowerB = 1/af1.max
            val upperBound = upperB - upperA - upperA
            val lowerBound = lowerB - lowerA - lowerA

            assertEquals(true, y.max>=upperBound && y.min<=lowerBound)
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun invTimesScalarTest2() {
        DDBuilder{

            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val a = af1.times(2.0).inv()
            val b = af1.times(4.0).inv()
            val y = a - b.times(2.0)
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timesInvInvTest() {
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val a = af1.times(2.0).inv().inv()
            val b = af1.inv().inv()
            val y = a - b.times(2.0)
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timesInvTimesInvTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val a = af1.times(2.0).inv().times(2.0).inv()
            val b = af1.times(1.0).inv().inv()
            val y = a - b
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun stressTestInv(){
        DDBuilder{

            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val a = af1.times(2.0).inv()
            val b = af1.times(1.0).inv().inv().inv()
            val c = a.times(2.0).inv().inv()
            val y = c - b
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timesTimesAFAFTest(){
        DDBuilder{

            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val af2 = AffineForm(this, 1.0..2.0)
            val a = af1.times(af2)
            val b = af1.times(2.0).times(af2)
            val y = b - a.times(2.0)
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timesInvTimesAFAFTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val af2 = AffineForm(this, 1.0..2.0)

            val a = af1.times(2.0).inv().times(af2)
            val b = af1.inv().times(af2)
            val y = b - a.times(2.0)
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timesTimesAFAFTestInv(){
        DDBuilder{

            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val af2 = AffineForm(this, 1.0..2.0)

            val a = af1.times(af2).inv()
            val b = af1.times(2.0).times(af2).inv()
            val y = b.times(2.0) - a
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timesTimesAFAFInvTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val af2 = AffineForm(this, 1.0..2.0)

            val a = af1.times(af2).inv()
            val b = af2.times(2.0).times(af1).inv()
            val y = a - b.times(2.0)
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timesInvTimesAFAFInvTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val af2 = AffineForm(this, 1.0..2.0)

            val a = af1.times(2.0).inv().times(af2).inv()
            val b = af1.inv().times(af2).times(0.5).inv()
            val y = b - a
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timesInvTimesAFAFInvTest2(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true

            val af1 = AffineForm(this, 1.0..2.0)
            val a = af1.times(2.0).inv().times(af1).inv()
            val b = af1.times(af1.times(2.0).inv()).inv()
            val y = b - a
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of original forms (scalar dependencies) when the original forms flag is set.
     */
    @Test
    fun timeAFAFNewCasesTest(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            config.originalFormsFlag = true
            val af1 = AffineForm(this, 1.0..2.0)
            val af2 = AffineForm(this, 1.0..2.0)
            val a = af1.times(2.0)
            val b = a.times(af2).inv()
            val c = af1.times(af2).times(2.0).inv()
            val y = b - c
            assertEquals(0.0, y.radius, precision)
        }
    }
    /**
     * Tests the correct mapping of rounding errors, when the roundingErrorMappingFlag is set.
     */
    @Test
    fun roundingErrorMappingTest() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.roundingErrorMappingFlag = true

            val gravity = 9.81
            val a = 0.9
            val areaOfOutflowPipe= exp(ln(0.5) *2) * PI
            val areaTank = 3.0*3.0

            val discreteLevel = AffineForm(this, 9.9..10.1, 10)
            // outflow after Torricelli's law
            val initAux = discreteLevel * gravity * 2.0
            val initVolumeOutflow = - initAux.sqrt() * a * areaOfOutflowPipe
            val initOutflow = initVolumeOutflow * (1.0/areaTank)

            for (time in 0 .. 10) {
                val aux = discreteLevel * gravity * 2.0
                val volumeOutflow = - aux.sqrt() * a * areaOfOutflowPipe
                val outflow = volumeOutflow * (1.0/areaTank)
                if (time == 0) {
                    assertEquals(initAux, aux)
                    assertEquals(initVolumeOutflow, volumeOutflow)
                    assertEquals(initOutflow, outflow)
                }
            }
        }
    }
}