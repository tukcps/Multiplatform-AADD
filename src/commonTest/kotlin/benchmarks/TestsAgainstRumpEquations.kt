package benchmarks

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.RealMath.minus
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.pow
import io.github.tukcps.aadd.DDBuilder.RealMath.times
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The next 9 tests are stress tests for any and all Interval and/or Affine Form data types.
 * This stress test was developed by Dr. Siegfried Rump.
 * We use them to track progress w.r.t. over-approximation.
 *
 * The `assertEquals` is overloaded and does not permit under-approximation.
 *
 * AADD has some 1e-7 +/- relative accurracy; we use an additional uncertainty 0.01
 * to get more challenges for AA/IA/... and worse results to monitor & track.
 *
 * @author Jack Martin (based on work of S. Rump)
 */
class TestsAgainstRumpEquations {

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0 at (x=0, y = 0.999..1.001)
     */
    @Test
    fun testAADDAgainstRumpEquation0() {
        DDBuilder {
            val x = real(0.0 ..  0.0)
            val y = real(0.999 .. 1.001)
            val z : Real =
                pow(x, 4.0) * 9.0 -
                        pow(y, 4.0) +
                        pow(y, 2.0) * 2.0
            assertSafeInclusion(0.999995995999..1.0, z, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0 with x=14.999..15.001; y= 25.999..26.001
     * Result must be ideally [-190.691904643992, 192.708096644008]
     */
    @Test
    fun testAADDAgainstRumpEquation3() {
        DDBuilder {
            val x = real(14.999 ..  15.001)
            val y = real(25.999 .. 26.001)
            val z : Real =
                pow(x, 4.0) * 9.0 -
                        pow(y, 4.0) +
                        pow(y, 2.0) * 2.0
            assertTrue(1.0 in z)
            assertSafeInclusion(-190.691904643992..192.708096644008, z.getRange(), 0.1)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     */
    @Test
    fun testAADDAgainstRumpEquation1() {
        DDBuilder {
            val x = real(0.999 ..  1.001)
            val y = real(1.999 .. 2.001)
            val z : Real =
                pow(x, 4.0) * 9.0 -
                        pow(y, 4.0) +
                        pow(y, 2.0) * 2.0
            assertTrue(1.0 in z)
            //println("z = " + z)
            assertSafeInclusion(0.940031956008..1.060032044007, z, 0.0002)
        }
    }

    /**
     * `9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0` with 3.999..4.001; 6.999, 7.001
     */
    @Test
    fun testAADDAgainstRumpEquation2() {
        DDBuilder {
            val x = real(3.999 ..  4.001)
            val y = real(6.999 .. 7.001)
            val z : Real =
            pow(x, 4.0) * 9.0 -
                    pow(y, 4.0) +
                    pow(y, 2.0) * 2.0
            assertTrue(1.0 in z)
            assertSafeInclusion(-2.647428171992..4.648572172008, z, 0.01)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     *
     * Again, as in the discussion for test number 3, the result is widening here.
     * Input is: (x, y) = (56, 97)
     */
    @Test
    fun testAADDAgainstRumpEquation4() {
        DDBuilder {
            val x = real(56.0)
            val y = real(97.0)
            val z : Real =
                        pow(x, 4.0) * 9.0 -
                        pow(y, 4.0) +
                        pow(y, 2.0) * 2.0

            assertTrue(1.0 in z)
            assertSafeInclusion(1.0..1.0, z, 1e-7)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     *
     * Again, as in the discussion for test number 3, the result is widening here.
     * Input is: (x, y) = (209, 362)
     */
    @Test
    fun testAADDAgainstRumpEquation5() {
        DDBuilder {
            val x = real(208.999 ..  209.001)
            val y = real(361.999 .. 362.001)
            val z : Real =
                pow(x, 4.0) * 9.0 -
                        pow(y, 4.0) +
                        pow(y, 2.0) * 2.0
            assertTrue(1.0 in z)
            assertSafeInclusion(-518403.535496971..518408.680520973, z, 3.0)
        }
    }

    /**
     * `9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0`
     * with
     * - `x = real(779.999 ..  780.001)`and
     * - `y = real(1350.999 .. 1351.001)`
     *
     * Again, as in the discussion for test number 3, the result is widening here.
     * Input is: (x, y) = (780, 1351)
     */
    @Test
    fun testAADDAgainstRumpEquation6() {
        DDBuilder {
            val x = real(779.999 ..  780.001)
            val y = real(1350.999 .. 1351.001)
            val z : Real =
                pow(x, 4.0) * 9.0 -
                        pow(y, 4.0) +
                        pow(y, 2.0) * 2.0
            assertTrue(1.0 in z)
            assertSafeInclusion(-26_947_229.89763749..26_947_275.70242948, z, 11.0)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     * dVar("x", 2910.999 .. 2911.001)
     * dVar("y", 5041.999 .. 5042.001)
     *
     * Input is: (x, y) = (2911, 5042)
     */
    @Test
    fun testAADDAgainstRumpEquation7() {
        DDBuilder {
            val x = real(2910.999 .. 2911.001)
            val y = real(5041.999 .. 5042.001)
            val z : Real =
                pow(x, 4.0) * 9.0 -
                        pow(y, 4.0) +
                        pow(y, 2.0) * 2.0
            assertTrue(1.0 in z)
            assertSafeInclusion(-1_400_738_835.238972..1_400_739_447.361276, z, 41.0)
            assertTrue(z is AADD.Leaf)
            val radius = z.radius
            assertTrue("Regression: radius of pow got worse compared with previous version", radius < 1_400_739_751.9736900)
        }
    }

    /**
     * 9.0 * x^4 - y^4 + 2.0 * y^2 = 1.0
     * dVar("x", 10863.999 .. 10864.001)
     * dVar("y", 18816.999 .. 18817.001)
     *
     * Input is: (x, y) = (10864+-0.01, 18817+-0.01)
     *
     */
    @Test
    fun testAADDAgainstRumpEquation8() {
        DDBuilder {
            val x = real(10863.999 .. 10864.001)
            val y = real(18816.999 .. 18817.001)
            val z : Real =
                pow(x, 4.0) * 9.0 -
                        pow(y, 4.0) +
                        pow(y, 2.0) * 2.0
            assertTrue(1.0 in z)
            assertSafeInclusion(-7.281044418696875E10..7.281045273496875E10, z, 1050000.0)
            assertTrue(z is AADD.Leaf)
            val optimalRadius = 72_811_493_845.3223
            val maxRelativeOverapproximation = 1e-6
            assertTrue(
                z.radius <= optimalRadius * (1.0 + maxRelativeOverapproximation),
                "AA radius ${z.radius} exceeds expected quality"
            )
        }
    }
}