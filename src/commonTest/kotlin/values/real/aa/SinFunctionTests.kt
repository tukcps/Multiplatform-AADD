package values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.minus
import io.github.tukcps.aadd.values.real.aa.sin
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.minus
import io.github.tukcps.aadd.util.Assertions.assertEquals
import testutil.ddTest
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertTrue

class SinFunctionTests {
    @Test
    fun sinScalar() = ddTest {
        assertEquals(0.0..0.0, sin(AffineForm.scalar(this, 0.0)), 1e-8)
    }

    @Test
    fun sinSmallInterval() = ddTest {
        val x = AffineForm.range(this, -0.1..0.1)
        assertTrue(0.0 in sin(x))
    }

    @Test
    fun sinAroundPi() = ddTest {
        val x = AffineForm.range(this, PI - 0.1..PI + 0.1)
        assertTrue(0.0 in sin(x))
    }

    @Test
    fun sinSmallAroundZero() = ddTest {
        val x = AffineForm.range(this, -0.1..0.1)
        val y = sin(x)
        assertTrue(sin(-0.1) in y)
        assertTrue(sin(0.1) in y)
    }

    @Test
    fun sinAroundMaximum() = ddTest {
        val x = AffineForm.range(this, PI / 2 - 0.1..PI / 2 + 0.1)
        val y = sin(x)
        assertEquals(sin(PI / 2 - 0.1) .. 1.0, y, 1e-9)
    }

    @Test
    fun sinAroundMinimum() = ddTest {
        val x = AffineForm.range(this, 3 * PI / 2 - 0.1..3 * PI / 2 + 0.1)
        val y = sin(x)
        assertTrue(-1.0 in y)
    }

    @Test
    fun sinFullPeriod() = ddTest {
        val x = AffineForm.range(this, 0.0..2 * PI)
        val y = sin(x)
        assertEquals(-1.0..1.0, y, 1e-8)
    }
    @Test
    fun sinAffineSmallInterval() = ddTest {
        val x = AffineForm.range(this, 0.0..0.1)
        val y = sin(x)
        val exact = io.github.tukcps.aadd.values.real.ia.sin(RealRange(0.0..0.1))
        assertTrue(y.radius < (exact.max - exact.min).toDouble() * 2)
    }

    @Test
    fun sinPreservesCorrelationNearZero() = ddTest {
        val x = AffineForm.range(this, -0.01..0.01)
        val y = sin(x)
        val z = y - x
        assertTrue(z.radius < 1e-4)
    }

    @Test
    fun sinNonLinearRemainder() = ddTest {
        val x = AffineForm.range(this, -0.5..0.5)

        val y = sin(x)
        val error = y - x

        assertTrue(error.radius > 0.0)
        assertTrue(0.0 in error)
    }

    @Test
    fun sinSameVariableCorrelation() = ddTest {
        val x = AffineForm.range(this, -0.1..0.1)

        val y = sin(x)
        val z = y - x

        assertTrue(z.radius < 0.01)
    }

}
