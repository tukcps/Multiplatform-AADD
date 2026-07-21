package values.real.math

import io.github.tukcps.aadd.values.real.math.ErrorFreeTransforms
import kotlin.math.nextDown
import kotlin.math.nextUp
import kotlin.test.*

class ErrorFreeTransformsTest {

    @Test
    fun twoSumExact() {
        val r = ErrorFreeTransforms.twoSum(1.0, 2.0)

        assertEquals(3.0, r.value)
        assertEquals(0.0, r.error)
        assertTrue(r.exact)
    }

    @Test
    fun twoSumInexact() {
        val r = ErrorFreeTransforms.twoSum(0.1, 0.2)
        assertEquals(0.1 + 0.2, r.value)
        assertFalse(r.exact)
        val exact = r.value + r.error
        assertTrue(exact >= r.value.nextDown())
        assertTrue(exact <= r.value.nextUp())
    }

    @Test
    fun splitReconstructsOriginal() {
        val s = ErrorFreeTransforms.split(12345.6789)

        assertEquals(12345.6789, s.hi + s.lo)
    }

    @Test
    fun splitZero() {
        val s = ErrorFreeTransforms.split(0.0)

        assertEquals(0.0, s.hi)
        assertEquals(0.0, s.lo)
    }

    @Test
    fun splitNegative() {
        val s = ErrorFreeTransforms.split(-42.5)

        assertEquals(-42.5, s.hi + s.lo)
    }

    @Test
    fun splitSubnormal() {
        val s = ErrorFreeTransforms.split(Double.MIN_VALUE)

        assertEquals(Double.MIN_VALUE, s.hi + s.lo)
    }

    @Test
    fun splitInfinity() {
        val s = ErrorFreeTransforms.split(Double.POSITIVE_INFINITY)

        assertTrue(s.hi.isInfinite())
    }

    @Test
    fun splitNaN() {
        val s = ErrorFreeTransforms.split(Double.NaN)

        assertTrue(s.hi.isNaN())
    }

    // Known limitation of algorithm.
    @Ignore
    @Test
    fun splitDoesNotOverflowNearMaxValue() {
        val s = ErrorFreeTransforms.split(Double.MAX_VALUE)

        assertTrue(s.hi.isFinite(), "splitter overflowed")
        assertTrue(s.lo.isFinite(), "splitter overflowed")

        assertEquals(Double.MAX_VALUE, s.hi + s.lo)
    }

    @Test
    fun twoProdExact() {
        val r = ErrorFreeTransforms.twoProd(2.0, 3.0)

        assertEquals(6.0, r.value)
        assertEquals(0.0, r.error)
        assertTrue(r.exact)
    }

    @Test
    fun twoProdZero() {
        val r = ErrorFreeTransforms.twoProd(123.0, 0.0)

        assertEquals(0.0, r.value)
        assertEquals(0.0, r.error)
    }

    @Test
    fun twoProdInexact() {
        val r = ErrorFreeTransforms.twoProd(0.1, 0.2)

        assertNotEquals(0.0, r.error)

        val exact = 0.1 * 0.2
        assertEquals(exact, r.value)
        assertEquals(exact, r.value + r.error)
    }

    @Test
    fun twoProdInfinity() {
        val r = ErrorFreeTransforms.twoProd(Double.POSITIVE_INFINITY, 2.0)

        assertTrue(r.value.isInfinite())
        assertEquals(0.0, r.error)
    }

    @Test
    fun twoProdNaN() {
        val r = ErrorFreeTransforms.twoProd(Double.NaN, 2.0)

        assertTrue(r.value.isNaN())
    }

    @Test
    fun twoProdNegative() {
        val r = ErrorFreeTransforms.twoProd(-2.0, 3.0)

        assertEquals(-6.0, r.value)
        assertEquals(0.0, r.error)
        assertTrue(r.exact)
    }
}