package values.real.aa

import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.cos
import testutil.ddTest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.test.Test
import kotlin.test.assertTrue

class CosFunctionTests {

    @Test
    fun cosZero() = ddTest {
        assertEquals(1.0..1.0, cos(AffineForm.scalar(this, 0.0)), 1e-8)
    }

    @Test
    fun cosHalfPi() = ddTest {
        assertEquals(0.0..0.0, cos(AffineForm.scalar(this, (PI / 2))), 1e-8)
    }

    @Test
    fun cosPi() = ddTest {
        assertEquals(-1.0..-1.0, cos(AffineForm.scalar(this, PI)), 1e-8)
    }

    @Test
    fun cosTwoPi() = ddTest {
        assertEquals(1.0..1.0, cos(AffineForm.scalar(this, 2.0 * PI)), 1e-8)
    }

    @Test
    fun cosFirstQuadrant() = ddTest {
        val y = cos(AffineForm.range(this, 0.0..PI / 2))
        assertEquals(0.0..1.0, y, 1e-8)
    }

    @Test
    fun cosSecondQuadrant() = ddTest {
        val y = cos(AffineForm.range(this, PI / 2..PI))
        assertEquals(-1.0..0.0, y, 1e-8)
    }

    @Test
    fun cosThirdQuadrant() = ddTest {
        val y = cos(AffineForm.range(this, PI..3.0 * PI / 2))
        assertEquals(-1.0..0.0, y, 1e-8)
    }

    @Test
    fun cosFourthQuadrant() = ddTest {
        val y = cos(AffineForm.range(this, 3.0 * PI / 2..2.0 * PI))
        assertEquals(0.0..1.0, y, 1e-8)
    }

    @Test
    fun cosFullPeriod() = ddTest {
        val y = cos(AffineForm.range(this, 0.0..2.0 * PI))
        assertEquals(-1.0..1.0, y, 1e-8)
    }

    @Test
    fun cosManyPeriods() = ddTest {
        val y = cos(AffineForm.range(this, -10.0 * PI..10.0 * PI))
        assertEquals(-1.0..1.0, y, 1e-8)
    }

    @Test
    fun cosAroundMaximum() = ddTest {
        val eps = 1e-12
        val y = cos(AffineForm.range(this, -eps..eps))
        assertTrue(1.0 in y)
    }

    @Test
    fun cosAroundMinimum() = ddTest {
        val eps = 1e-12
        val y = cos(AffineForm.range(this, PI - eps..PI + eps))
        assertTrue(-1.0 in y)
    }

    @Test
    fun cosSmallInterval() = ddTest {
        val x = AffineForm.range(this, -0.1..0.1)
        val y = cos(x)
        assertEquals(cos(-0.1)..1.0, y, 1e-10)
    }

    @Test
    fun cosAroundPiHalf() = ddTest {
        val x = AffineForm.range(this, PI / 2 - 0.1..PI / 2 + 0.1)
        val y = cos(x)

        assertTrue(0.0 in y)
    }
}