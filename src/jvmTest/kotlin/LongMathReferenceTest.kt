package org.sysmd.math

import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.integer.LongMath
import java.math.BigInteger
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class LongMathReferenceTest {

    private val min = BigInteger.valueOf(Long.MIN_VALUE)
    private val max = BigInteger.valueOf(Long.MAX_VALUE)

    private fun expected(value: BigInteger): LongBound =
        when {
            value < min -> LongBound.NegativeInfinity
            value > max -> LongBound.PositiveInfinity
            else -> LongBound.Finite(value.longValueExact())
        }

    private fun expectedDiv(a: Long, b: Long): LongBound? =
        when {
            b == 0L -> null
            a == Long.MIN_VALUE && b == -1L -> LongBound.PositiveInfinity
            else -> LongBound.Finite(a / b)
        }

    private fun b(value: Long) = LongBound.Finite(value)

    private val edgeCases = longArrayOf(
        Long.MIN_VALUE,
        Long.MIN_VALUE + 1,
        -2, -1, 0, 1, 2,
        Long.MAX_VALUE - 1,
        Long.MAX_VALUE
    )

    private inline fun checkAll(crossinline check: (Long, Long) -> Unit) {
        for (a in edgeCases)
            for (b in edgeCases)
                check(a, b)

        val rnd = Random(1)

        repeat(100_000) {
            check(rnd.nextLong(), rnd.nextLong())
        }
    }

    @Test
    fun testAddProperty() =
        checkAll { a, b ->
            assertEquals(
                expected(BigInteger.valueOf(a) + BigInteger.valueOf(b)),
                LongMath.add(b(a), b(b)),
                "a=$a, b=$b"
            )
        }

    @Test
    fun testSubtractProperty() =
        checkAll { a, b ->
            assertEquals(
                expected(BigInteger.valueOf(a) - BigInteger.valueOf(b)),
                LongMath.subtract(b(a), b(b)), "a=$a, b=$b"
            )
        }

    @Test
    fun testMultiplyProperty() =
        checkAll { a, b ->
            assertEquals(
                expected(BigInteger.valueOf(a) * BigInteger.valueOf(b)),
                LongMath.multiply(b(a), b(b)),
                "a=$a, b=$b"
            )
        }

    @Test
    fun testDivideProperty() =
        checkAll { a, b ->
            assertEquals(
                expectedDiv(a, b),
                LongMath.divide(b(a), b(b)),
                "a=$a, b=$b"
            )
        }
}