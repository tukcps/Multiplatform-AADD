package values.real.ia

import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.cos
import io.github.tukcps.aadd.util.Assertions.assertEquals
import testutil.ddTest
import kotlin.math.PI
import kotlin.test.Test

class CosTests {
    @Test
    fun cosZero() = ddTest {
        assertEquals(1.0..1.0, cos(RealRange(0.0..0.0)), 1e-8)
    }

    @Test
    fun cosHalfPi() = ddTest {
        assertEquals(0.0..0.0, cos(RealRange(PI/2..PI/2)), 1e-8)
    }

    @Test
    fun cosPi() = ddTest {
        assertEquals(-1.0..-1.0, cos(RealRange(PI..PI)), 1e-8)
    }

    @Test
    fun cosThreeHalfPi() = ddTest {
        assertEquals(0.0..0.0, cos(RealRange(3.0*PI/2..3.0*PI/2)), 1e-8)
    }

    @Test
    fun cosTwoPi() = ddTest {
        assertEquals(1.0..1.0, cos(RealRange(2.0*PI..2.0*PI)), 1e-8)
    }

    @Test
    fun cosFullPeriod() = ddTest {
        assertEquals(-1.0..1.0, cos(RealRange(0.0..2.0*PI)), 1e-8)
    }

    @Test
    fun cosZeroToPi() = ddTest {
        assertEquals(-1.0..1.0, cos(RealRange(0.0..PI)), 1e-8)
    }
}