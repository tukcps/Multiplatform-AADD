package examples.buildertests

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.IDD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorTests {
    @Test
    fun testNumberToAADD() {
        DDBuilder {
            val n1 = number(1.0 .. 2.0)
            val n2 = number(3.0)
            assertTrue(n1 is AADD)
            assertTrue(n2 is AADD)
            assertEquals(1.0 .. 2.0, n1.min .. n1.max)
            assertEquals(3.0 .. 3.0, n2.min .. n2.max)
        }
    }

    @Test
    fun testNumberToIDD() {
        DDBuilder {
            val n1 = number(1 .. 2)
            val n2 = number(3)
            assertTrue(n1 is IDD)
            assertTrue(n2 is IDD)
            assertEquals(1L .. 2L, n1.min .. n1.max)
            assertEquals(3L .. 3L, n2.min .. n2.max)
        }
    }
}