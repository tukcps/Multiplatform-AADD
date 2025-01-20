package examples.values
import kotlin.test.*
import com.github.tukcps.aadd.values.*

class NumberRangeTests {

    @Test
    fun rangeEqualsTest1() {
        val rangeA = Range(3.0 .. 3.0)
        val rangeB = Range(2.9999999999999987 .. 3.0000000000000013)
        assertEquals(rangeA,rangeB)
    }

    @Test
    fun rangeEqualsTest2() {
        val rangeA = Range(2.0 .. 2.0)
        val rangeB = Range(2.0 .. 2.0)
        println(rangeA == rangeB)
    }


}