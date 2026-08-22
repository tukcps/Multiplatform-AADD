package dd.aaddtests

import io.github.tukcps.aadd.DDBuilder.RealMath.log2
import io.github.tukcps.aadd.DDBuilder.RealMath.pow2
import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import testutil.ddTest
import kotlin.test.Test

class AADDMathTests {

    @Test
    fun testPow2Log2() = ddTest {
        val x = real(1.0 .. 2.0)
        val y = pow2(x)
        assertSafeInclusion(2.0 .. 4.0, y, 1e-8)

        val z = log2(y)
        assertSafeInclusion(1.0 .. 2.0, z, 1e-8)
    }
}