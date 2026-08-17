package values.integer

import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.integer.LongBound
import io.github.tukcps.aadd.values.integer.pow2
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import io.github.tukcps.aadd.util.Assertions.assertEquals

class IntegerRangeTests {
    @Test
    fun pow2Test() {
        assertEquals(IntegerRange(1L, 1L), pow2(IntegerRange(0L)))
        assertEquals(IntegerRange(1L, 2L), pow2(IntegerRange(0L, 1L)))
        assertEquals(IntegerRange(4L, 16L), pow2(IntegerRange(2L, 4L)))
        assertEquals(IntegerRange(1024L, 1_048_576L), pow2(IntegerRange(10L, 20L)))

        assertFailsWith<IllegalArgumentException> { pow2(IntegerRange(-1L,2L)) }
    }

    @Test
    fun parseTest() {
        val i = IntegerRange.parse("-* .. *")
        assertTrue(i.isAll())
        val j = IntegerRange.parse("*..*")
        assertEquals(LongBound.PositiveInfinity..LongBound.PositiveInfinity, j)
        val k = IntegerRange.parse("-*..-*")
        assertEquals(LongBound.NegativeInfinity..LongBound.NegativeInfinity, k)
        val l = IntegerRange.parse("-3 .. 4")
        assertEquals(-3L..4L, l)
        val m = IntegerRange.parse("3 .. *")
        assertEquals(LongBound.Finite(3)..LongBound.PositiveInfinity, m)
    }
}