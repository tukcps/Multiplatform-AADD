package values

import com.github.tukcps.aadd.values.Range
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

import kotlin.math.max
import kotlin.math.min


class RangeTest {
    val a = Range(-1.0, 2.0)
    val b = Range(2.0, 3.0)

    /** A real is the range +-MAX_VALUE. It is identified by isReal() */
    @Test fun realTest() {
        val real = Range(Range.Reals)
        assertEquals(Double.NEGATIVE_INFINITY, real.min)
        assertEquals(Double.POSITIVE_INFINITY, real.max)
        assertTrue(real.isReals())
    }

    @Test
    fun testPlus() {
        val c = a+b
        assertEquals(c.min, a.min+b.min, 0.00000001)
        assertEquals(c.max, a.max+b.max, 0.00000001)
    }

    @Test
    fun testMinus() {
        val c = a-b
        assertEquals(c.max, max(a.min-b.max, b.min-a.max), 0.000000001)
        assertEquals(c.min, min(a.min-b.max, b.min-a.max), 0.000000001)
    }

    // TODO currently commented out due to string problematic in MP
    /*
    @Test
    fun rangeToString() {
        val locale = Locale.getDefault()
        val nf = NumberFormat.getInstance() // current locale formatter
        val userPi = nf.format(Math.PI*10000)     // user input

        println("current locale: ${locale.displayName}")
        println("user input in current locale: $userPi")

        val parsedPi = nf.parse(userPi).toDouble() // now you have double

        println("parsed input: $parsedPi")

    }*/
}