package values.real.ia

import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.sin
import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import testutil.ddTest
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertTrue

class SinTests {
    @Test
    fun sinZero() = ddTest {
        assertSafeInclusion(0.0..0.0, sin(RealRange(0.0..0.0)), 1e-8)
    }

    @Test
    fun sinHalfPi() = ddTest {
        assertSafeInclusion(1.0..1.0, sin(RealRange(PI / 2..PI / 2)), 1e-8)
    }

    @Test
    fun sinPi() = ddTest {
        assertSafeInclusion(0.0..0.0, sin(RealRange(PI..PI)), 1e-8)
    }

    @Test
    fun sinThreeHalfPi() = ddTest {
        assertSafeInclusion(-1.0..-1.0, sin(RealRange(3.0 * PI / 2..3.0 * PI / 2)), 1e-8)
    }

    @Test
    fun sinTwoPi() = ddTest {
        assertSafeInclusion(0.0..0.0, sin(RealRange(2.0 * PI..2.0 * PI)), 1e-8)
    }

    @Test
    fun sinFirstQuadrant() = ddTest {
        assertSafeInclusion(0.0..1.0, sin(RealRange(0.0..PI / 2)), 1e-8)
    }

    @Test
    fun sinSecondQuadrant() = ddTest {
        assertSafeInclusion(0.0..1.0, sin(RealRange(PI / 2..PI)), 1e-8)
    }

    @Test
    fun sinThirdQuadrant() = ddTest {
        assertSafeInclusion(-1.0..0.0, sin(RealRange(PI..3.0 * PI / 2)), 1e-8)
    }

    @Test
    fun sinFourthQuadrant() = ddTest {
        assertSafeInclusion(-1.0..0.0, sin(RealRange(3.0 * PI / 2..2.0 * PI)), 1e-8)
    }

    @Test
    fun sinZeroToPi() = ddTest {
        assertSafeInclusion(0.0..1.0, sin(RealRange(0.0..PI)), 1e-8)
    }

    @Test
    fun sinPiToTwoPi() = ddTest {
        assertSafeInclusion(-1.0..0.0, sin(RealRange(PI..2.0 * PI)), 1e-8)
    }

    @Test
    fun sinFullPeriod() = ddTest {
        assertSafeInclusion(-1.0..1.0, sin(RealRange(0.0..2.0 * PI)), 1e-8)
    }

    @Test
    fun sinManyPeriods() = ddTest {
        assertSafeInclusion(-1.0..1.0, sin(RealRange(-10.0 * PI..10.0 * PI)), 1e-8)
    }

    @Test
    fun sinAroundMaximum() = ddTest {
        val eps = 1e-12
        assertSafeInclusion(
            sin(PI / 2 - eps)..1.0,
            sin(RealRange(PI / 2 - eps..PI / 2 + eps)),
            1e-8
        )
    }

    @Test
    fun sinAroundMinimum() = ddTest {
        val eps = 1e-12
        assertSafeInclusion(
            -1.0..sin(3.0 * PI / 2 + eps),
            sin(RealRange(3.0 * PI / 2 - eps..3.0 * PI / 2 + eps)),
            1e-8
        )
    }

    @Test
    fun sinEmpty() = ddTest {
        assertTrue(sin(RealRange.Empty).isEmpty())
    }

    @Test
    fun sinReals() = ddTest {
        assertSafeInclusion(-1.0..1.0, sin(RealRange.Reals), 1e-8)
    }
}