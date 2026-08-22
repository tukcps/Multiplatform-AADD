package values.real.ia

import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.cos
import testutil.ddTest
import kotlin.math.PI
import kotlin.test.Test

class CosTests {
    @Test
    fun cosZero() = ddTest {
        assertSafeInclusion(1.0..1.0, cos(RealRange(0.0..0.0)), 1e-8)
    }

    @Test
    fun cosHalfPi() = ddTest {
        assertSafeInclusion(0.0..0.0, cos(RealRange(PI / 2..PI / 2)), 1e-8)
    }

    @Test
    fun cosPi() = ddTest {
        assertSafeInclusion(-1.0..-1.0, cos(RealRange(PI..PI)), 1e-8)
    }

    @Test
    fun cosThreeHalfPi() = ddTest {
        assertSafeInclusion(0.0..0.0, cos(RealRange(3.0 * PI / 2..3.0 * PI / 2)), 1e-8)
    }

    @Test
    fun cosTwoPi() = ddTest {
        assertSafeInclusion(1.0..1.0, cos(RealRange(2.0 * PI..2.0 * PI)), 1e-8)
    }

    @Test
    fun cosFullPeriod() = ddTest {
        assertSafeInclusion(-1.0..1.0, cos(RealRange(0.0..2.0 * PI)), 1e-8)
    }

    @Test
    fun cosZeroToPi() = ddTest {
        assertSafeInclusion(-1.0..1.0, cos(RealRange(0.0..PI)), 1e-8)
    }
}