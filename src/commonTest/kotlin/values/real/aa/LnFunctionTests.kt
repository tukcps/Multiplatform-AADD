package values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.exp
import io.github.tukcps.aadd.values.real.aa.ln
import io.github.tukcps.aadd.util.Assertions.assertEquals
import testutil.ddTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LnFunctionTests {

    @Test
    fun lnOne() = ddTest {
        val x = AffineForm.scalar(this, 1.0)
        val y = ln(x)
        assertEquals(0.0..0.0, y, 0.00000001)
    }

    @Test
    fun lnInterval() = ddTest {
        val x = AffineForm.range(this, 1.0..2.0)
        val y = ln(x)
        assertEquals(0.0 .. kotlin.math.ln(2.0), y, 0.000001)
    }

    @Test
    fun lnZero() = ddTest {
        val y = ln(AffineForm.scalar(this, 0.0))
        assertEquals(DoubleBound.NegativeInfinity, y.min)
    }

    @Test
    fun lnNegative() = ddTest {
        assertTrue(ln(AffineForm.scalar(this,-1.0)).isEmpty())
    }

    @Test
    fun lnInfinity() = ddTest {
        val y = ln(AffineForm.scalar(this, Double.POSITIVE_INFINITY))
        assertEquals(DoubleBound.PositiveInfinity, y.max)
    }

    @Test
    fun lnMinusInfinity() = ddTest {
        assertTrue(ln(AffineForm.scalar(this, Double.NEGATIVE_INFINITY)).isEmpty())
    }

    @Test
    fun lnMonotone() = ddTest {
        val a = ln(AffineForm.scalar(this, 2.0))
        val b = ln(AffineForm.scalar(this, 3.0))
        assertTrue(a.max <= b.min)
    }

    @Test
    fun expLnIdentity() = ddTest {
        val x = AffineForm.range(this, 1.0..2.0)
        val y = exp(ln(x))
        assertEquals(1.0..2.0, y, 1e-6)
    }

    @Test
    fun lnExpIdentity() = ddTest {
        val x = AffineForm.range(this, -1.0..2.0)
        val y = ln(exp(x))
        assertEquals(-1.0..2.0, y, 1e-6)
    }

}