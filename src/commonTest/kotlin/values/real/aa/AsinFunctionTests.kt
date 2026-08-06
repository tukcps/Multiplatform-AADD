package values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.AsinFunction
import io.github.tukcps.aadd.values.real.aa.asin
import io.github.tukcps.aadd.values.real.aa.linearize
import io.github.tukcps.aadd.values.real.aa.minus
import io.github.tukcps.aadd.values.real.aa.sin
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.sin
import io.github.tukcps.aadd.values.real.minus
import testutil.ddTest
import testutil.Assertions.assertEquals
import kotlin.math.PI
import kotlin.ranges.rangeTo
import kotlin.test.Test
import kotlin.test.assertTrue

class AsinFunctionTests {
    @Test
    fun asinZero() = ddTest {
        assertEquals(0.0..0.0, asin(AffineForm.scalar(this, 0.0)), 1e-8)
    }

    @Test
    fun asinOne() = ddTest {
        assertEquals(PI / 2.0..PI / 2.0, asin(AffineForm.scalar(this, 1.0)), 1e-8)
    }

    @Test
    fun asinMinusOne() = ddTest {
        assertEquals(-PI / 2.0..-PI / 2.0, asin(AffineForm.scalar(this, -1.0)), 1e-8)
    }

    @Test
    fun asinHalf() = ddTest {
        assertEquals(PI / 6.0..PI / 6.0, asin(AffineForm.scalar(this, 0.5)), 1e-8)
    }

    @Test
    fun asinInterval() = ddTest {
        val y = asin(AffineForm.range(this, -0.5..0.5))
        assertEquals(-PI / 6.0..PI / 6.0, y, 1e-6)
    }

    @Test
    fun asinDomain() = ddTest {
        val y = asin(AffineForm.range(this, -1.0..1.0))
        assertEquals(-PI / 2.0..PI / 2.0, y, 1e-6)
    }

    @Test
    fun asinOutsideDomain() = ddTest {
        assertTrue(asin(AffineForm.scalar(this, 2.0)).isEmpty())
    }

    @Test
    fun asinPartialDomain() = ddTest {
        val y = asin(AffineForm.range(this, -2.0..0.5))
        assertEquals(-PI / 2.0..PI / 6.0, y, 1e-6)
    }

    @Test
    fun asinLinearizationPositive() = ddTest {
        val x = AffineForm.range(this, 0.2..0.8)
        assertTrue(AsinFunction.linearize(x)!!.noise > 0.0)
    }

    @Test
    fun asinLinearizationNegative() = ddTest {
        val x = AffineForm.range(this, -0.8..-0.2)
        assertTrue(AsinFunction.linearize(x)!!.noise > 0.0)
    }
}