package aaddtests

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.Range
import kotlin.test.Test
import kotlin.test.assertEquals


class representationTests {


    /**
     * constrainTo to scalar shall not drop the dependency information.
     **/
    @Test
    fun scalarOperation() {
        val builder = DDBuilder()
        with(builder) {
            this.config.toStringVerbose = true
            val a = real(1.0 .. 5.0, "a")
            val b = a constrainTo(Range(2.0 .. 2.0))
            assertEquals(2.0, b.getRange().min)
            assertEquals(2.0, b.getRange().max)
            val c = a + b
            c.getRange()
            //println(c)
            // assertEquals(b.)
        }
    }
}