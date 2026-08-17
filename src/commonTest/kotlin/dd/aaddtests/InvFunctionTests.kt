package dd.aaddtests

import io.github.tukcps.aadd.DDBuilder.RealMath.inv
import io.github.tukcps.aadd.values.real.aa.InvFunction
import io.github.tukcps.aadd.util.Assertions.assertEquals
import testutil.ddTest
import kotlin.test.Test

class InvFunctionTests {

    @Test
    fun invPositive() = ddTest {
        val x = real(1.0..2.0)
        val z = inv(x)
        assertEquals(0.5..1.0, z ,1e-8)
    }

    @Test
    fun invNegative() = ddTest {
        val x = real(-2.0..-1.0)
        val z = inv(x)
        assertEquals(-1.0..-0.5, z, 1e-8)
    }

    @Test
    fun invCrossingZero() = ddTest {
        val x = real(-2.0..2.0)
        val z = inv(x)
    }

    @Test
    fun i() = ddTest {
        val x = real(1.0..2.0)

        println("x       = $x")
        println("min/max = ${x.min} .. ${x.max}")
        println("curv    = ${InvFunction.curvature(x.getRange())}")
    }
}