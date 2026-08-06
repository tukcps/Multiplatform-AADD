package values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.exp
import testutil.Assertions.assertEquals
import testutil.ddTest
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ExpFunctionTests {

    @Test
    fun expTest1() = ddTest {
        val af1to2 = AffineForm.range(this, 1.0 .. 2.0 )
        val af = exp(af1to2)
        assertEquals(exp(1.5), af.central, 1e-15)
        assertEquals(exp(1.0)..exp(2.0), af, 1e-15)
        assertEquals(2, af.xi.size)
        assertEquals(0.5 * exp(1.5), af.xi[1]!!, 1e-15)
        assertEquals(exp(2.0) * 0.5 * 0.5 / 2.0, af.xi[-1L]!!, 1e-14)
    }

    @Test
    fun expZero() = ddTest {
        val x = AffineForm.scalar(this, 0.0)
        val y = exp(x)
        assertTrue(1.0 in y)
    }

    @Test
    fun expOne() = ddTest {
        val x = AffineForm.scalar(this, 1.0)
        val y = exp(x)
        assertTrue(kotlin.math.E in y)
    }

    @Test
    fun expMinusOne() = ddTest {
        val x = AffineForm.scalar(this, -1.0)
        val y = exp(x)
        assertTrue(exp(-1.0) in y)
    }

    @Test
    fun expInterval() = ddTest {
        val x = real(1.0..2.0).value
        val y = exp(x)
        assertEquals(exp(1.0)..exp(2.0), y, 0.000000001)
    }

    @Test
    fun expOverflow() = ddTest {
        val y = exp(AffineForm.scalar(this, 1000.0))
        assertEquals(DoubleBound.PositiveInfinity, y.max)
    }

    @Test
    fun expUnderflow() = ddTest {
        val y = exp(AffineForm.scalar(this, -1000.0))
        assertTrue(0.0 in y)
    }

    @Test
    fun expInfinity() = ddTest {
        assertEquals(
            AffineForm.scalar(this, Double.POSITIVE_INFINITY),
            exp(AffineForm.scalar(this, Double.POSITIVE_INFINITY))
        )

        assertEquals(
            AffineForm.scalar(this, 0.0),
            exp(AffineForm.scalar(this, Double.NEGATIVE_INFINITY))
        )

        assertEquals(
            RealRange(DoubleBound.Finite(0.0), DoubleBound.PositiveInfinity),
            exp(AF.All)
        )

        assertEquals(AF.Empty, exp(AF.Empty))
    }
}