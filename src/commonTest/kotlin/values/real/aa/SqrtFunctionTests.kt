package values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.SqrtFunction
import io.github.tukcps.aadd.values.real.aa.minus
import io.github.tukcps.aadd.values.real.aa.plus
import io.github.tukcps.aadd.values.real.aa.sqrt
import io.github.tukcps.aadd.values.real.aa.times
import io.github.tukcps.aadd.util.Assertions.assertEquals
import testutil.ddTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SqrtFunctionTests {
    @Test
    fun sqrtZero() = ddTest {
        val x = AffineForm.scalar(this, 0.0)
        val y = sqrt(x)
        assertEquals(0.0..0.0, y)
    }

    @Test
    fun sqrtOne() = ddTest {
        val x = AffineForm.scalar(this, 1.0)
        val y = sqrt(x)
        assertEquals(1.0..1.0, y)
    }

    @Test
    fun sqrtFour() = ddTest {
        val x = AffineForm.scalar(this, 4.0)
        val y = sqrt(x)
        assertEquals(2.0..2.0, y, 1e-6)
    }

    @Test
    fun sqrtInterval() = ddTest {
        val x = AffineForm.range(this, 1.0..4.0)
        val y = sqrt(x)
        assertEquals(1.0..2.0, y, 1e-6)
    }

    @Test
    fun sqrtInfinity() = ddTest {
        val y = sqrt(AffineForm.scalar(this, Double.POSITIVE_INFINITY))
        assertEquals(DoubleBound.PositiveInfinity, y.max)
    }

    @Test
    fun sqrtNegative() = ddTest {
        assertTrue(sqrt(AffineForm.scalar(this, -1.0)).isEmpty())
    }

    @Test
    fun sqrtNegativeInterval() = ddTest {
        val x = AffineForm.range(this, -4.0..-1.0)
        assertTrue(sqrt(x).isEmpty())
    }

    @Test
    fun sqrtCrossingZero() = ddTest {
        val x = AffineForm.range(this, -1.0..4.0)
        val sqrt = sqrt(x)
        assertEquals(0.0..2.0, sqrt, 1e-6)
        assertTrue(sqrt in SqrtFunction.image)
    }

    @Test
    fun sqrtSquare() = ddTest {
        val x = AffineForm.range(this, 1.0..4.0)
        val y = sqrt(x * x)
        assertEquals(1.0..4.0, y, 1e-6)
    }

    @Test
    fun squareSqrt() = ddTest {
        val x = AffineForm.range(this, 1.0..4.0)
        val y = sqrt(x)
        assertEquals(1.0..4.0, y * y, 1e-6)
    }

    @Test
    fun sqrtMinusSelf() = ddTest {
        val x = AffineForm.range(this, 1.0..4.0)
        val a = sqrt(x)
        val y = a - a
        assertTrue(0.0 in y)
        assertTrue(y.radius < a.radius)
    }

    @Test
    fun sqrtLinearCombination() = ddTest {
        val x = AffineForm.range(this, 1.0..4.0)
        val a = sqrt(x)
        val p = 2.0 * a
        val y = p - a
        assertEquals(1.0..2.0, y, 0.4)
        assertTrue(y.radius <= a.radius)
    }

    @Test
    fun sqrtDifference() = ddTest {
        val x = AffineForm.range(this, 100.0..101.0)
        val y = sqrt(x + 1.0) - sqrt(x)
        assertEquals(kotlin.math.sqrt(101.0) - kotlin.math.sqrt(100.0) ..kotlin.math.sqrt(102.0) - kotlin.math.sqrt(101.0),
            y, 0.1)
        assertTrue(y.radius < 0.05)
    }

    @Test
    fun sqrtRationalization() = ddTest {
        val x = AffineForm.range(this, 100.0..101.0)
        val a = sqrt(x + 1.0)
        val b = sqrt(x)
        val y = (a - b) * (a + b)
        assertTrue(1.0 in y)
        assertTrue(y.radius < 0.02)
    }
}