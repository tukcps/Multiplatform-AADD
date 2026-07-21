package values.integer
import io.github.tukcps.aadd.values.integer.Bound
import io.github.tukcps.aadd.values.integer.IntegerMath
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("KotlinMisorderedAssertEqualsArguments")
class IntegerMathTest {

    @Test
    fun testAdd() {
        assertEquals(Bound.Finite(0), IntegerMath.add(0, 0))
        assertEquals(Bound.Finite(3), IntegerMath.add(1, 2))
        assertEquals(Bound.Finite(-3), IntegerMath.add(-1, -2))
        assertEquals(Bound.PositiveInfinity, IntegerMath.add(Long.MAX_VALUE, 1))
        assertEquals(Bound.NegativeInfinity, IntegerMath.add(Long.MIN_VALUE, -1))
    }


    @Test
    fun testMul() {
        // Basic cases
        assertEquals(Bound.Finite(0), IntegerMath.mul(0, 0))
        assertEquals(Bound.Finite(0), IntegerMath.mul(0, 42))
        assertEquals(Bound.Finite(0), IntegerMath.mul(42, 0))

        assertEquals(Bound.Finite(42), IntegerMath.mul(6, 7))
        assertEquals(Bound.Finite(-42), IntegerMath.mul(-6, 7))
        assertEquals(Bound.Finite(-42), IntegerMath.mul(6, -7))
        assertEquals(Bound.Finite(42), IntegerMath.mul(-6, -7))

        // Identity
        assertEquals(Bound.Finite(Long.MAX_VALUE), IntegerMath.mul(Long.MAX_VALUE, 1))
        assertEquals(Bound.Finite(Long.MIN_VALUE), IntegerMath.mul(Long.MIN_VALUE, 1))
        assertEquals(Bound.Finite(-Long.MAX_VALUE), IntegerMath.mul(Long.MAX_VALUE, -1))
        assertEquals(Bound.PositiveInfinity, IntegerMath.mul(Long.MIN_VALUE, -1))

        // Overflow
        assertEquals(Bound.PositiveInfinity, IntegerMath.mul(Long.MAX_VALUE, 2))
        assertEquals(Bound.PositiveInfinity, IntegerMath.mul(2, Long.MAX_VALUE))

        assertEquals(Bound.NegativeInfinity, IntegerMath.mul(Long.MAX_VALUE, -2))
        assertEquals(Bound.NegativeInfinity, IntegerMath.mul(-2, Long.MAX_VALUE))

        assertEquals(Bound.NegativeInfinity, IntegerMath.mul(Long.MIN_VALUE, 2))
        assertEquals(Bound.NegativeInfinity, IntegerMath.mul(2, Long.MIN_VALUE))

        assertEquals(Bound.PositiveInfinity, IntegerMath.mul(Long.MIN_VALUE, Long.MIN_VALUE))
    }

    @Test
    fun testSub() {
        // Basic
        assertEquals(Bound.Finite(0), IntegerMath.sub(0, 0))
        assertEquals(Bound.Finite(-1), IntegerMath.sub(1, 2))
        assertEquals(Bound.Finite(1), IntegerMath.sub(2, 1))
        assertEquals(Bound.Finite(-13), IntegerMath.sub(-6, 7))
        assertEquals(Bound.Finite(13), IntegerMath.sub(7, -6))

        // Overflow
        assertEquals(Bound.PositiveInfinity, IntegerMath.sub(Long.MAX_VALUE, -1))
        assertEquals(Bound.PositiveInfinity, IntegerMath.sub(Long.MAX_VALUE, Long.MIN_VALUE))

        assertEquals(Bound.NegativeInfinity, IntegerMath.sub(Long.MIN_VALUE, 1))
        assertEquals(Bound.NegativeInfinity, IntegerMath.sub(Long.MIN_VALUE, Long.MAX_VALUE))
    }

    @Test
    fun testDiv() {
        // Basic
        assertEquals(Bound.Finite(0), IntegerMath.div(0, 1))
        assertEquals(Bound.Finite(3), IntegerMath.div(6, 2))
        assertEquals(Bound.Finite(-3), IntegerMath.div(-6, 2))
        assertEquals(Bound.Finite(-3), IntegerMath.div(6, -2))
        assertEquals(Bound.Finite(3), IntegerMath.div(-6, -2))

        // Undefined
        assertEquals(Bound.NaN, IntegerMath.div(1, 0))
        assertEquals(Bound.NaN, IntegerMath.div(0, 0))

        // Overflow
        assertEquals(Bound.PositiveInfinity, IntegerMath.div(Long.MIN_VALUE, -1))
    }

    @Test
    fun testRem() {
        // Basic
        assertEquals(Bound.Finite(0), IntegerMath.rem(6, 2))
        assertEquals(Bound.Finite(1), IntegerMath.rem(7, 2))
        assertEquals(Bound.Finite(-1), IntegerMath.rem(-7, 2))
        assertEquals(Bound.Finite(1), IntegerMath.rem(7, -2))
        assertEquals(Bound.Finite(-1), IntegerMath.rem(-7, -2))

        // Undefined
        assertEquals(Bound.NaN, IntegerMath.rem(1, 0))
        assertEquals(Bound.NaN, IntegerMath.rem(0, 0))
    }

    @Test
    fun testNeg() {
        assertEquals(Bound.Finite(0), IntegerMath.neg(0))
        assertEquals(Bound.Finite(-42), IntegerMath.neg(42))
        assertEquals(Bound.Finite(42), IntegerMath.neg(-42))
        assertEquals(Bound.Finite(Long.MAX_VALUE), IntegerMath.neg(Long.MIN_VALUE + 1))

        assertEquals(Bound.PositiveInfinity, IntegerMath.neg(Long.MIN_VALUE))
    }

    @Test
    fun testAbs() {
        assertEquals(Bound.Finite(0), IntegerMath.abs(0))
        assertEquals(Bound.Finite(42), IntegerMath.abs(42))
        assertEquals(Bound.Finite(42), IntegerMath.abs(-42))
        assertEquals(Bound.Finite(Long.MAX_VALUE), IntegerMath.abs(Long.MAX_VALUE))

        assertEquals(Bound.PositiveInfinity, IntegerMath.abs(Long.MIN_VALUE))
    }

    @Test
    fun testInc() {
        assertEquals(Bound.Finite(1), IntegerMath.inc(0))
        assertEquals(Bound.Finite(0), IntegerMath.inc(-1))
        assertEquals(Bound.Finite(Long.MAX_VALUE), IntegerMath.inc(Long.MAX_VALUE - 1))

        assertEquals(Bound.PositiveInfinity, IntegerMath.inc(Long.MAX_VALUE))
    }

    @Test
    fun testDec() {
        assertEquals(Bound.Finite(-1), IntegerMath.dec(0))
        assertEquals(Bound.Finite(0), IntegerMath.dec(1))
        assertEquals(Bound.Finite(Long.MIN_VALUE), IntegerMath.dec(Long.MIN_VALUE + 1))

        assertEquals(Bound.NegativeInfinity, IntegerMath.dec(Long.MIN_VALUE))
    }
}
