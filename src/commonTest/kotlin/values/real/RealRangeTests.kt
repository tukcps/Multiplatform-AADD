package values.real
import kotlin.test.*
import io.github.tukcps.aadd.values.real.RealRange
import io.github.tukcps.aadd.values.real.relu

class RealRangeTests {

    @Test
    fun rangeEqualsTest1() {
        val realRangeA = RealRange(3.0..3.0)
        val realRangeB = RealRange(2.9999999999999987..3.0000000000000013)
        assertEquals(realRangeA,realRangeB)
    }

    @Test
    fun rangeEqualsTest2() {
        val realRangeA = RealRange(2.0..2.0)
        val realRangeB = RealRange(2.0..2.0)
        println(realRangeA == realRangeB)
    }

    @Test
    fun reluTest()
    {
        // The relu functions sets negative values to 0 and non negaitve it just passes
        // Thus for -1.0 .. 1.0 we expect a return range of 0.0 .. 1.0
        val realRange = RealRange(-1.0..1.0)
        val relu_res = realRange.relu()
        //println(relu_res)

    }

}