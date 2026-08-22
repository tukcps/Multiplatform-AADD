package values.real.ia

import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.tan
import testutil.ddTest
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertTrue

class TanTests {

    @Test
    fun tanQuarterPi() = ddTest {
        assertSafeInclusion(0.0..1.0, tan(RealRange(0.0..PI / 4)), 1e-8)
    }

    @Test
    fun tanNegativeQuarterPi() = ddTest {
        assertSafeInclusion(-1.0..0.0, tan(RealRange(-PI / 4..0.0)), 1e-8)
    }

    @Test
    fun tanCrossesPole() = ddTest {
        assertSafeInclusion(
            RealRange.Reals,
            tan(RealRange(PI / 2 - 0.1..PI / 2 + 0.1))
        )
    }

    @Test
    fun tanFullPeriod() = ddTest {
        assertSafeInclusion(
            RealRange.Reals,
            tan(RealRange(0.0..PI))
        )
    }

    @Test
    fun tanPoleAtBoundary() = ddTest {
        assertSafeInclusion(
            RealRange.Reals,
            tan(RealRange(PI / 2..PI / 2))
        )
    }

    @Test
    fun tanLargeInterval() = ddTest {
        assertSafeInclusion(
            RealRange.Reals,
            tan(RealRange(-100.0..100.0))
        )
    }
    @Test
    fun tanZero() = ddTest {
        assertSafeInclusion(0.0..0.0, tan(RealRange(0.0..0.0)), 1e-8)
    }

    @Test
    fun tanPositive() = ddTest {
        assertSafeInclusion(0.0..1.0, tan(RealRange(0.0..PI / 4)), 1e-8)
    }

    @Test
    fun tanNegative() = ddTest {
        assertSafeInclusion(-1.0..0.0, tan(RealRange(-PI / 4..0.0)), 1e-8)
    }

    @Test
    fun tanPole() = ddTest {
        assertSafeInclusion(RealRange.Reals, tan(RealRange(-PI / 2..PI / 2)))
    }

    @Test
    fun tanPeriod() = ddTest {
        assertSafeInclusion(RealRange.Reals, tan(RealRange(0.0..PI)))
    }

    @Test
    fun tanNearPole() = ddTest {
        val eps = 1e-6
        val y = tan(RealRange(PI / 2 - eps..PI / 2 - eps))

        assertTrue(y.max.isFinite)
    }
}