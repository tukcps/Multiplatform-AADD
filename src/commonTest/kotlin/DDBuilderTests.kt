import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.util.Assertions
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.util.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DDBuilderTests {
    @Test
    fun testRealCornerCases() {
        DDBuilder {
            // If a bound cannot be computed, use Infinity as over-approximation.
            val n1 = real(Double.NaN..2.0)
            Assertions.assertSafeInclusion(Double.NEGATIVE_INFINITY..2.0, n1, 0.0)

            // Maintain infinities ...
            val n2 = real(Double.NEGATIVE_INFINITY..2.0)
            Assertions.assertSafeInclusion(Double.NEGATIVE_INFINITY..2.0, n2, 0.0)

            // If both bounds are not computable, return all Reals
            val n3 = real(Double.NaN..Double.NaN)
            assertSame(Reals.All, n3)

            // Map empty to singleton
            val n4 = real(5.0..4.0)
            assertSame(Reals.Empty, n4)
        }
    }

    @Test
    fun testIntegerCornerCases() {
        DDBuilder {
            // Maintain infinities ...
            val n2 = integer(IntegerRange(LongBound.NegativeInfinity..LongBound.Finite(2L)))
            assertEquals(LongBound.NegativeInfinity ..LongBound.Finite(2L), n2.min .. n2.max)
        }
    }

    @Test
    fun testNumberToAADD() {
        DDBuilder {
            val n1 = number(1.0..2.0)
            val n2 = number(3.0)
            assertTrue(n1 is Real)
            assertTrue(n2 is Real)
            Assertions.assertSafeInclusion(1.0..2.0, n1, 0.0)
            Assertions.assertSafeInclusion(3.0..3.0, n2, 0.0)
        }
    }

    @Test
    fun testNumberToIDD() {
        DDBuilder {
            val n1 = number(1 .. 2)
            val n2 = number(3)
            assertTrue(n1 is IDD)
            assertTrue(n2 is IDD)
            assertEquals<ClosedRange<LongBound>>(LongBound.Finite(1L) ..LongBound.Finite(2L), n1.min .. n1.max)
            assertEquals<ClosedRange<LongBound>>(LongBound.Finite(3L) ..LongBound.Finite(3L), n2.min .. n2.max)
        }
    }
}