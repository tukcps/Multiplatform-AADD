package values.real.aa

import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.pow
import io.github.tukcps.aadd.values.real.aa.root
import testutil.Assertions.assertEquals
import testutil.ddTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RootFunctionTests {
    @Test
    fun squareRootScalar() = ddTest {
        val x = AffineForm.scalar(this, 9.0)

        val y = root(x, 2.0)

        assertEquals(3.0, y.central, 1e-12)
        assertEquals(0.0, y.radius, 1e-12)
    }

    @Test
    fun squareRootInterval() = ddTest {
        val x = AffineForm.range(this, 4.0..9.0)

        val y = root(x, 2.0)

        assertEquals(2.5, y.central, 0.1)
        assertTrue(y.radius < 0.6)

        assertTrue(2.0 in y)
        assertTrue(3.0 in y)
    }

    @Test
    fun variableRoot() = ddTest {
        val x = AffineForm.scalar(this, 16.0)
        val n = AffineForm.range(this, 2.0..4.0)

        val y = root(x, n)

        assertEquals(3.0, y.central, 0.3)
        assertTrue(y.radius < 1.2)

        assertTrue(2.0 in y)
        assertTrue(4.0 in y)
    }

    @Test
    fun rootPowInverse() = ddTest {
        val x = AffineForm.range(this, 2.0..8.0)
        val y = root(x, 3.0)
        val z = pow(y, 3.0)
        assertEquals(x, z, 100.0)
    }
}