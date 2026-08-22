package dd.iddtests

import io.github.tukcps.aadd.DDBuilder.IntMath.log2
import io.github.tukcps.aadd.DDBuilder.IntMath.pow2
import io.github.tukcps.aadd.DDBuilder.IntMath.sqrt
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.integer.sqr
import testutil.ddTest
import kotlin.test.Test
import kotlin.test.assertTrue

class IDDMathTests {
    @Test
    fun sqrTest() = ddTest {
        assertEquals(0L..9L, sqr(IntegerRange(-3L, 2L)))
    }

    @Test
    fun sqrtTestSplit() = ddTest {
        val s = sqrt(integer(2L .. 3L))
        assertEquals(-2L..2L, s)
        assertTrue(s is IDD.Internal)
    }

    @Test
    fun sqrtTestJoin() = ddTest {
        val s = sqrt(integer(0L .. 0L))
        assertEquals(-0L..0L, s)
        assertTrue(s is IDD.Leaf)
    }


    @Test
    fun testPow2Log2() = ddTest {
        val x = integer(1L .. 2L)
        val y = pow2(x)
        assertEquals(2L .. 4L, y)

        val z = log2(y)
        assertEquals(1L .. 2L, z)
    }
}