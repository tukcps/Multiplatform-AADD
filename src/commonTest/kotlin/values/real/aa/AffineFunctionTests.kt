package values.real.aa

import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.affine
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.affine
import io.github.tukcps.aadd.util.Assertions.assertEquals
import testutil.ddTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AffineFunctionTests {
    @Test
    fun affineIdentity() = ddTest {
        val x = AffineForm.range(this, 1.0..2.0)
        val identity = affine(x, x, 1.0, 0.0, 0.0)
        assertSafeInclusion(x, identity)
    }

    @Test
    fun affineScaling() = ddTest {
        val x = AffineForm.range(this, 1.0..2.0)
        assertSafeInclusion(2.0..4.0, affine(x, RealRange(2.0..4.0), 2.0, 0.0, 0.0), 0.0)
    }

    @Test
    fun affineTranslation() = ddTest {
        val x = AffineForm.range(this, 1.0..2.0)
        assertSafeInclusion(4.0..5.0, affine(x, RealRange(4.0..5.0), 1.0, 3.0, 0.0), 0.0)
    }

    @Test
    fun affineNegation() = ddTest {
        val x = AffineForm.range(this, 1.0..2.0)
        assertSafeInclusion(-2.0..-1.0, affine(x, RealRange(-2.0..-1.0), -1.0, 0.0, 0.0), 0.0)
    }

    @Test
    fun affineNoise() = ddTest {
        val x = AffineForm.scalar(this, 1.0)
        val y = affine(x, RealRange(0.9..1.1), 1.0, 0.0, 0.1)
        assertTrue(y.radius >= 0.1)
        // Depends on handling of r
        // assertEquals(2, y.xi.size)
    }

    @Test
    fun affineLinear() = ddTest {
        val x = AffineForm.range(this, 1.0..2.0)
        val y = affine(x, RealRange(5.0..7.0), 2.0, 3.0, 0.0)
        assertSafeInclusion(5.0..7.0, y, 0.0)
        // assertEquals(x.xi.size, y.xi.size)
    }

    //
    // -------------------- Corner cases, Inf, etc. ----------------------
    //
    @Test
    fun affineEmpty() = ddTest {
        assertTrue(affine(AF.Empty, AF.Empty, 2.0, 3.0, 1.0).isEmpty())
    }

    @Test
    fun affineReals() = ddTest {
        assertTrue(affine(AF.All, AF.All, 2.0, 3.0, 1.0).isReals())
    }

    @Test
    fun affinePositiveInfinity() = ddTest {
        val x = AffineForm.scalar(this, Double.POSITIVE_INFINITY)
        val y = affine(x, RealRange(DoubleBound.PositiveInfinity, DoubleBound.PositiveInfinity), 2.0, 3.0, 0.0)
        assertEquals(DoubleBound.PositiveInfinity, y.min)
        assertEquals(DoubleBound.PositiveInfinity, y.max)
    }

    @Test
    fun affineNegativeInfinity() = ddTest {
        val x = AffineForm.scalar(this, DoubleBound.NegativeInfinity)
        val y = affine(x, affine(RealRange(x), -2.0, 3.0), -2.0, 3.0, 0.0)
        assertEquals(DoubleBound.PositiveInfinity, y.min)
        assertEquals(DoubleBound.PositiveInfinity, y.max)
    }

    @Test
    fun affineOverflow() = ddTest {
        val x = AffineForm.scalar(this, Double.MAX_VALUE)
        val y = affine(x, RealRange(DoubleBound.PositiveInfinity, DoubleBound.PositiveInfinity), 2.0, 0.0, 0.0)
        assertEquals(DoubleBound.PositiveInfinity, y.max)
    }
}