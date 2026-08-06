package values.real.ia

import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.pow
import testutil.Assertions.assertEquals
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class PowTests {
    @Test
    fun powRangeRandomized() {

        val random = Random(1)

        repeat(10_000) {
            val a = random.nextDouble(-5.0, 10.0)
            val b = random.nextDouble(-5.0, 10.0)
            val c = random.nextDouble(-3.0, 3.0)
            val d = random.nextDouble(-3.0, 3.0)

            val x = RealRange(min(a, b)..max(a, b))
            val y = RealRange(min(c, d)..max(c, d))

            val ia = pow(x, y)

            repeat(200) {

                val xx = random.nextDouble(x.min.finiteValue, x.max.finiteValue)
                val yy = random.nextDouble(y.min.finiteValue, y.max.finiteValue)

                if (xx < 0.0)
                    return@repeat

                if (xx == 0.0 && yy < 0.0)
                    return@repeat

                val value = xx.pow(yy)

                assertTrue(
                    value in ia,
                    "pow($x,$y) = $ia does not contain $value for ($xx,$yy)"
                )
            }
        }
    }
    @Test
    fun powZeroExponent() =
        assertEquals(RealRange.One, pow(RealRange(-5.0..5.0), RealRange.Zero))

    @Test
    fun powOneExponent() =
        assertEquals(2.0..4.0, pow(RealRange(2.0..4.0), RealRange.One))

    @Test
    fun powOneBase() =
        assertEquals(RealRange.One, pow(RealRange.One, RealRange(-10.0..10.0)))

    @Test
    fun powNegativeBase() =
        assertTrue(pow(RealRange(-5.0..-1.0), RealRange(0.5..2.0)).isEmpty())

    @Test
    fun powCrossingZeroBase() {
        val r = pow(RealRange(-2.0..4.0), RealRange(0.5..2.0))
        assertTrue(2.0 in r)
        assertTrue(4.0 in r)
    }

    @Test
    fun powNegativeExponentThroughZero() =
        assertTrue(pow(RealRange(0.0..2.0), RealRange(-2.0..-1.0)).isReals())
}