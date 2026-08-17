package values.real.aa

import io.github.tukcps.aadd.values.real.aa.AcosFunction
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.linearize
import io.github.tukcps.aadd.values.real.ia.acos
import io.github.tukcps.aadd.util.Assertions.assertEquals
import testutil.ddTest
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertTrue

class AcosFunctionTests {
    @Test
    fun acosZero() = ddTest {
        assertEquals(PI / 2.0..PI / 2.0, acos(AffineForm.scalar(this, 0.0)), 1e-8)
    }

    @Test
    fun acosOne() = ddTest {
        assertEquals(0.0..0.0, acos(AffineForm.scalar(this, 1.0)), 1e-8)
    }

    @Test
    fun acosMinusOne() = ddTest {
        assertEquals(PI..PI, acos(AffineForm.scalar(this, -1.0)), 1e-8)
    }

    @Test
    fun acosHalf() = ddTest {
        assertEquals(PI / 3.0..PI / 3.0, acos(AffineForm.scalar(this, 0.5)), 1e-8)
    }

    @Test
    fun acosInterval() = ddTest {
        val y = acos(AffineForm.range(this, -0.5..0.5))
        assertEquals(PI / 3.0..2.0 * PI / 3.0, y, 1e-6)
    }

    @Test
    fun acosDomain() = ddTest {
        val y = acos(AffineForm.range(this, -1.0..1.0))
        assertEquals(0.0..PI, y, 1e-6)
    }

    @Test
    fun acosOutsideDomain() = ddTest {
        assertTrue(acos(AffineForm.scalar(this, 2.0)).isEmpty())
    }

    @Test
    fun acosPartialDomain() = ddTest {
        val y = acos(AffineForm.range(this, -2.0..0.5))
        assertEquals(PI / 3.0..PI, y, 1e-6)
    }

    @Test
    fun acosLinearizationPositive() = ddTest {
        val x = AffineForm.range(this, 0.2..0.8)
        assertTrue(AcosFunction.linearize(x)!!.noise > 0.0)
    }

    @Test
    fun acosLinearizationNegative() = ddTest {
        val x = AffineForm.range(this, -0.8..-0.2)
        assertTrue(AcosFunction.linearize(x)!!.noise > 0.0)
    }
}