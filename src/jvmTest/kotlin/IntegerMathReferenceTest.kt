package org.sysmd.math

import io.github.tukcps.aadd.values.integer.Bound
import io.github.tukcps.aadd.values.integer.IntegerMath
import java.math.BigInteger
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals


class IntegerMathReferenceTest {

    private val min = BigInteger.valueOf(Long.MIN_VALUE)
    private val max = BigInteger.valueOf(Long.MAX_VALUE)

    private fun bound(value: BigInteger): Bound =
        when {
            value < min -> Bound.NegativeInfinity
            value > max -> Bound.PositiveInfinity
            else -> Bound.Finite(value.longValueExact())
        }

    @Test
    fun testAddProperty() {
        val edgeCases = longArrayOf(
            Long.MIN_VALUE,
            Long.MIN_VALUE + 1,
            -2, -1, 0, 1, 2,
            Long.MAX_VALUE - 1,
            Long.MAX_VALUE
        )

        fun check(a: Long, b: Long) =
            assertEquals(
                bound(BigInteger.valueOf(a) + BigInteger.valueOf(b)),
                IntegerMath.add(a, b),
                "a=$a, b=$b"
            )

        edgeCases.forEach { a ->
            edgeCases.forEach { b ->
                check(a, b)
            }
        }

        val rnd = Random(1)

        repeat(100_000) {
            check(rnd.nextLong(), rnd.nextLong())
        }
    }

    @Test
    fun testMulProperty() {
        val min = BigInteger.valueOf(Long.MIN_VALUE)
        val max = BigInteger.valueOf(Long.MAX_VALUE)

        fun expected(a: Long, b: Long): Bound {
            val r = BigInteger.valueOf(a) * BigInteger.valueOf(b)
            return when {
                r < min -> Bound.NegativeInfinity
                r > max -> Bound.PositiveInfinity
                else -> Bound.Finite(r.longValueExact())
            }
        }

        val edgeCases = longArrayOf(
            Long.MIN_VALUE,
            Long.MIN_VALUE + 1,
            -2, -1, 0, 1, 2,
            Long.MAX_VALUE - 1,
            Long.MAX_VALUE
        )

        for (a in edgeCases)
            for (b in edgeCases)
                assertEquals(expected(a, b), IntegerMath.mul(a, b), "a=$a, b=$b")

        val rnd = Random(1)

        repeat(100_000) {
            val a = rnd.nextLong()
            val b = rnd.nextLong()
            assertEquals(expected(a, b), IntegerMath.mul(a, b), "a=$a, b=$b")
        }
    }

    @Test
    fun testSubProperty() {

        fun check(a: Long, b: Long) =
            assertEquals(
                bound(BigInteger.valueOf(a) - BigInteger.valueOf(b)),
                IntegerMath.sub(a, b),
                "a=$a, b=$b"
            )

        val edgeCases = longArrayOf(
            Long.MIN_VALUE,
            Long.MIN_VALUE + 1,
            -2, -1, 0, 1, 2,
            Long.MAX_VALUE - 1,
            Long.MAX_VALUE
        )

        edgeCases.forEach { a ->
            edgeCases.forEach { b ->
                check(a, b)
            }
        }

        val rnd = Random(1)

        repeat(100_000) {
            check(rnd.nextLong(), rnd.nextLong())
        }
    }

    @Test
    fun testDivProperty() {
        val edgeCases = longArrayOf(
            Long.MIN_VALUE,
            Long.MIN_VALUE + 1,
            -2, -1, 0, 1, 2,
            Long.MAX_VALUE - 1,
            Long.MAX_VALUE
        )
        fun expected(a: Long, b: Long): Bound =
            when {
                b == 0L -> Bound.NaN
                a == Long.MIN_VALUE && b == -1L -> Bound.PositiveInfinity
                else -> Bound.Finite(a / b)
            }

        fun check(a: Long, b: Long) =
            assertEquals(expected(a, b), IntegerMath.div(a, b), "a=$a, b=$b")

        edgeCases.forEach { a ->
            edgeCases.forEach { b ->
                check(a, b)
            }
        }

        val rnd = Random(1)

        repeat(100_000) {
            check(rnd.nextLong(), rnd.nextLong())
        }
    }
}