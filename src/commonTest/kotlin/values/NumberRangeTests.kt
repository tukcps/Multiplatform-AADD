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

    @Test
    fun reluTest()
    {
        // The relu functions sets negative values to 0 and non negaitve it just passes
        // Thus for -1.0 .. 1.0 we expect a return range of 0.0 .. 1.0
        val range = Range(-1.0 .. 1.0)
        val relu_res = range.relu()
        //println(relu_res)

    }

}