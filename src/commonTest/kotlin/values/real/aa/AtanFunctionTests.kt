package values.real.aa

import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.AtanFunction
import io.github.tukcps.aadd.values.real.aa.atan
import io.github.tukcps.aadd.values.real.aa.linearize
import testutil.Assertions.assertEquals
import testutil.ddTest
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertTrue

class AtanFunctionTests {
    @Test
    fun atanZero() = ddTest {
        assertEquals(0.0..0.0, atan(AffineForm.scalar(this, 0.0)), 1e-8)
    }

    @Test
    fun atanOne() = ddTest {
        assertEquals(PI / 4.0..PI / 4.0, atan(AffineForm.scalar(this, 1.0)), 1e-8)
    }

    @Test
    fun atanMinusOne() = ddTest {
        assertEquals(-PI / 4.0..-PI / 4.0, atan(AffineForm.scalar(this, -1.0)), 1e-8)
    }

    @Test
    fun atanInterval() = ddTest {
        val y = atan(AffineForm.range(this, -1.0..1.0))
        assertEquals(-PI / 4.0..PI / 4.0, y, 1e-6)
    }

    @Test
    fun atanPositiveInfinity() = ddTest {
        val y = atan(AffineForm.scalar(this, Double.POSITIVE_INFINITY))
        assertEquals(DoubleBound.Finite(PI/2.0), y.max, 1e-8)
    }

    @Test
    fun atanNegativeInfinity() = ddTest {
        val y = atan(AffineForm.scalar(this, Double.NEGATIVE_INFINITY))
        assertEquals(DoubleBound.Finite(-PI/2.0), y.min, 1e-8)
    }

    @Test
    fun atanReals() = ddTest {
        val y = atan(AF.All)
        assertEquals(DoubleBound.Finite(-PI/2.0), y.min)
        assertEquals(DoubleBound.Finite(PI/2.0), y.max)
    }

    @Test
    fun atanLinearizationPositive() = ddTest {
        val x = AffineForm.range(this, 1.0..2.0)
        val y = AtanFunction.linearize(x)
        assertTrue(y!!.noise > 0.0)
    }

    @Test
    fun atanLinearizationNegative() = ddTest {
        val x = AffineForm.range(this, -2.0..-1.0)
        val y = AtanFunction.linearize(x)
        assertTrue(y!!.noise > 0.0)
    }

    @Test
    fun tanAtanIdentity() = ddTest {
        val x = AffineForm.range(this, -1.0..1.0)
        // assertTrue(x in tan(atan(x)))
    }
}