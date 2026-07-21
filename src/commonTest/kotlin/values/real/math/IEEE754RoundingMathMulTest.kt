package values.real.math

import io.github.tukcps.aadd.values.real.math.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.math.Rounding
import kotlin.test.*

class IEEE754RoundingMathMulTest {

    private val math = IEEE754RoundingMath

    @Test
    fun exactProduct() {
        assertEquals(6.0, math.mul(2.0, 3.0, Rounding.NEAREST))
        assertEquals(6.0, math.mul(2.0, 3.0, Rounding.DOWN))
        assertEquals(6.0, math.mul(2.0, 3.0, Rounding.UP))
    }

    @Test
    fun inexactProduct() {
        val towardnegative = math.mul(0.1, 0.2, Rounding.DOWN)
        val nearestEVEN = math.mul(0.1, 0.2, Rounding.NEAREST)
        val towardpositive = math.mul(0.1, 0.2, Rounding.UP)

        assertTrue(towardnegative <= nearestEVEN)
        assertTrue(nearestEVEN <= towardpositive)
    }

    @Test
    fun zero() {
        assertEquals(0.0, math.mul(123.0, 0.0, Rounding.NEAREST))
        assertEquals(-0.0, math.mul(-123.0, 0.0, Rounding.NEAREST))
    }

    @Test
    fun signs() {
        assertEquals(-6.0, math.mul(-2.0, 3.0, Rounding.NEAREST))
        assertEquals(6.0, math.mul(-2.0, -3.0, Rounding.NEAREST))
    }

    @Test
    fun infinity() {
        assertTrue(math.mul(Double.POSITIVE_INFINITY, 2.0, Rounding.NEAREST).isInfinite())
        assertTrue(math.mul(Double.NEGATIVE_INFINITY, 2.0, Rounding.NEAREST).isInfinite())
    }

    @Test
    fun nan() {
        assertTrue(math.mul(Double.NaN, 2.0, Rounding.NEAREST).isNaN())
    }

    @Test
    fun overflow() {
        val r = math.mul(Double.MAX_VALUE, 2.0, Rounding.NEAREST)
        assertTrue(r.isInfinite())
    }

    @Test
    fun underflow() {
        val r = math.mul(Double.MIN_VALUE, 0.5, Rounding.NEAREST)
        assertEquals(0.0, r)
    }

    @Test
    fun largeOperands() {
        val towardnegative = math.mul(1e300, 1e-300, Rounding.DOWN)
        val nearestEVEN = math.mul(1e300, 1e-300, Rounding.NEAREST)
        val towardpositive = math.mul(1e300, 1e-300, Rounding.UP)

        assertTrue(towardnegative <= nearestEVEN)
        assertTrue(nearestEVEN <= towardpositive)
    }

    @Test
    fun maxValueBoundary() {
        val towardnegative = math.mul(Double.MAX_VALUE / 2.0, 2.0, Rounding.DOWN)
        val nearestEVEN = math.mul(Double.MAX_VALUE / 2.0, 2.0, Rounding.NEAREST)
        val towardpositive = math.mul(Double.MAX_VALUE / 2.0, 2.0, Rounding.UP)

        assertTrue(towardnegative <= nearestEVEN)
        assertTrue(nearestEVEN <= towardpositive)
    }
}