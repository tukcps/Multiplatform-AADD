package examples.structuralTests

import com.github.tukcps.aadd.DDBuilder
import kotlin.test.*


class StructuralTests {
    @Test @Ignore
    fun bddSubTreeTest() {
        val builder = DDBuilder()
        val tru = builder.True
        val fal = builder.False

        val test1 = builder.Bool

        assertTrue(test1.containsSubDD(tru))
        assertTrue(test1.containsSubDD(fal))

        val test2 = builder.Bool.or(test1)

        assertTrue(test2.containsSubDD(test1))

        val infb = builder.InfeasibleB

        assertFalse(test1.containsSubDD(infb))
        assertFalse(infb.containsSubDD(tru))
        assertFalse(tru.containsSubDD(infb))
        assertFalse(infb.containsSubDD(fal))
        assertFalse(fal.containsSubDD(infb))
    }
}