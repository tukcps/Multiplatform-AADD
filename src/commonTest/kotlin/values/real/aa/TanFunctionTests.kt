package values.real.aa

import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.aa.minus
import io.github.tukcps.aadd.values.real.aa.tan
import testutil.ddTest
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertTrue

class TanFunctionTests {
    @Test
    fun tanAffineSmall() = ddTest {
        val x = AffineForm.range(this, -0.1..0.1)
        val y = tan(x)
        val error = y - x
        assertTrue(error.radius < 0.01)
    }

    @Test
    fun tanAffinePoleFallback() = ddTest {
        val x = AffineForm.range(this, PI/2 - 0.1..PI/2 + 0.1)
        val y = tan(x)
        assertTrue(y.isReals())
    }
}