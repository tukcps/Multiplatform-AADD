package values.real.aa

import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.pow
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.pow
import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import testutil.ddTest
import kotlin.test.Test
import kotlin.test.assertTrue

class PowFunctionTests {

    @Test
    fun powZero() = ddTest {
        val x = AffineForm.scalar(this, 5.0)
        val y = pow(x, 0.0)
        assertSafeInclusion(1.0..1.0, y, 1e-9)
    }

    @Test
    fun powOne() = ddTest {
        val x = AffineForm.scalar(this, 5.0)
        val y = pow(x, 1.0)
        assertSafeInclusion(5.0..5.0, y, 1e-8)
    }

    @Test
    fun powSquare() = ddTest {
        val x = AffineForm.scalar(this, 3.0)
        val y = pow(x, 2.0)
        assertSafeInclusion(9.0..9.0, y, 1e-8)
    }

    @Test
    fun powCube() = ddTest {
        val x = AffineForm.scalar(this, 2.0)
        val y = pow(x, 3.0)
        assertSafeInclusion(8.0..8.0, y, 1e-8)
    }

    @Test
    fun powAffineRadiusNearOne() = ddTest {
        val x = AffineForm.range(this, 0.9..1.1)
        val y = AffineForm.range(this, -2.0..2.0)

        val z = pow(x, y)

        assertTrue(z.radius > 0.0)
        assertSafeInclusion(pow(RealRange(x), RealRange(y)), z, 1e-9)
    }

    @Test
    fun powAffineRadiusPositive() = ddTest {
        val x = AffineForm.range(this, 2.0..4.0)
        val y = AffineForm.range(this, 0.5..1.5)

        val z = pow(x, y)

        assertSafeInclusion(pow(RealRange(x), RealRange(y)), z, 1e-9)
    }
}