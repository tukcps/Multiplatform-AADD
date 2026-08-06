package values.real.ia

import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.sin
import testutil.Assertions.assertEquals
import testutil.ddTest
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertTrue

class SinTests {
    @Test
    fun sinZero() = ddTest {
        assertEquals(0.0..0.0, sin(RealRange(0.0..0.0)), 1e-8)
    }

    @Test
    fun sinHalfPi() = ddTest {
        assertEquals(1.0..1.0, sin(RealRange(PI / 2..PI / 2)), 1e-8)
    }

    @Test
    fun sinPi() = ddTest {
        assertEquals(0.0..0.0, sin(RealRange(PI..PI)), 1e-8)
    }

    @Test
    fun sinThreeHalfPi() = ddTest {
        assertEquals(-1.0..-1.0, sin(RealRange(3.0*PI/2..3.0*PI/2)), 1e-8)
    }

    @Test
    fun sinTwoPi() = ddTest {
        assertEquals(0.0..0.0, sin(RealRange(2.0*PI..2.0*PI)), 1e-8)
    }

    @Test
    fun sinFirstQuadrant() = ddTest {
        assertEquals(0.0..1.0, sin(RealRange(0.0..PI/2)), 1e-8)
    }

    @Test
    fun sinSecondQuadrant() = ddTest {
        assertEquals(0.0..1.0, sin(RealRange(PI/2..PI)), 1e-8)
    }

    @Test
    fun sinThirdQuadrant() = ddTest {
        assertEquals(-1.0..0.0, sin(RealRange(PI..3.0*PI/2)), 1e-8)
    }

    @Test
    fun sinFourthQuadrant() = ddTest {
        assertEquals(-1.0..0.0, sin(RealRange(3.0*PI/2..2.0*PI)), 1e-8)
    }

    @Test
    fun sinZeroToPi() = ddTest {
        assertEquals(0.0..1.0, sin(RealRange(0.0..PI)), 1e-8)
    }

    @Test
    fun sinPiToTwoPi() = ddTest {
        assertEquals(-1.0..0.0, sin(RealRange(PI..2.0*PI)), 1e-8)
    }

    @Test
    fun sinFullPeriod() = ddTest {
        assertEquals(-1.0..1.0, sin(RealRange(0.0..2.0*PI)), 1e-8)
    }

    @Test
    fun sinManyPeriods() = ddTest {
        assertEquals(-1.0..1.0, sin(RealRange(-10.0*PI..10.0*PI)), 1e-8)
    }

    @Test
    fun sinAroundMaximum() = ddTest {
        val eps = 1e-12
        assertEquals(
            sin(PI/2-eps)..1.0,
            sin(RealRange(PI/2-eps..PI/2+eps)),
            1e-8
        )
    }

    @Test
    fun sinAroundMinimum() = ddTest {
        val eps = 1e-12
        assertEquals(
            -1.0..sin(3.0*PI/2+eps),
            sin(RealRange(3.0*PI/2-eps..3.0*PI/2+eps)),
            1e-8
        )
    }

    @Test
    fun sinEmpty() = ddTest {
        assertTrue(sin(RealRange.Empty).isEmpty())
    }

    @Test
    fun sinReals() = ddTest {
        assertEquals(-1.0..1.0, sin(RealRange.Reals), 1e-8)
    }
}